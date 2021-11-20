package eu.darken.bb.common.sharedresource

import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.logging.Logging.Priority.*
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.error.tryUnwrap
import eu.darken.bb.common.flow.onError
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A utility class to create child/parent dependencies for expensive resources.
 * Allows keeping reusable resources alive until it is no longer needed by anyone.
 */
@Suppress("ProtectedInFinal")
open class SharedResource<T : Any> constructor(
    private val tag: String,
    parentScope: CoroutineScope,
    source: Flow<T>,
) : KeepAlive {

    private val childScope = CoroutineScope(parentScope.newCoroutineContext(SupervisorJob()))

    private val lock = Mutex()

    private val activeLeases = mutableSetOf<ActiveLease>()
    private val children = mutableSetOf<KeepAlive>()
    private val parents = mutableMapOf<SharedResource<*>, Resource<T>>()

    override val isClosed: Boolean
        get() = !isAlive

    var isAlive: Boolean = false
        private set

    private val resourceHolder: Flow<T> = source
        .onStart {
            lock.withLock {
                isAlive = true
                log(tag, DEBUG) { "Acquiring shared resource..." }

                if (activeLeases.isNotEmpty()) {
                    log(tag, WARN) { "Non-empty activeLeases: $activeLeases" }
                    if (BBDebug.isDebug()) throw IllegalStateException("Non-empty activeLeases: $activeLeases")
                    activeLeases.forEach { it.close() }
                    activeLeases.clear()
                }

                if (children.isNotEmpty()) {
                    log(tag, WARN) { "Non-empty child references: $children" }
                    if (BBDebug.isDebug()) throw IllegalStateException("Non-empty child references: $children")
                    children.clear()
                }

                if (parents.isNotEmpty()) {
                    log(tag, WARN) { "Non-empty parent references: $parents" }
                    parents.clear()
                }
            }
        }
        .onCompletion {
            lock.withLock {
                isAlive = false
                log(tag) { "Releasing shared resource..." }

                // Cancel all borrowed resources
                childScope.coroutineContext.cancelChildren()

                activeLeases.forEach {
                    log(tag, WARN) { "Shared resource released with despite active lease: $it" }
                    if (BBDebug.isDebug()) throw IllegalStateException("Shared resource released with despite active leases: $activeLeases")
                }
                activeLeases.clear()

                children.forEach {
                    log(tag, VERBOSE) { "Closing child resource: $it" }
                    it.close()
                }
                children.clear()

                parents.clear()
            }
        }
        .onError { log(tag, WARN) { "Failed to provide resource: ${it.asLog()}" } }
        .onEach { log(tag) { "Resource ready: $it" } }
        .shareIn(parentScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = 1)

    suspend fun get(): Resource<T> {
        if (BBDebug.isDebug() && !isAlive) {
//            log(tag, VERBOSE) { "get() Reviving SharedResource: ${Throwable().getStackTraceString()}" }
            log(tag, VERBOSE) { "get() Reviving SharedResource" }
        }

        val activeLease = lock.withLock {
            val job = resourceHolder.launchIn(childScope).apply {
                invokeOnCompletion {
                    log(tag, VERBOSE) { "get(): Resource lease completed (activeLeases=${activeLeases.size})" }
                }
            }

            ActiveLease(job)
        }

        val resource = try {
            log(tag, VERBOSE) { "get(): Retrieving resource" }
            resourceHolder.first()
        } catch (e: Exception) {
            activeLease.close()
            throw e.tryUnwrap()
        }

        lock.withLock {
            if (activeLease.isClosed) {
                log(tag, ERROR) { "We got a resource, but the lease is already closed???" }
            } else {
                log(tag, VERBOSE) { "Adding new lease: $activeLease" }
                activeLeases.add(activeLease)
                log(tag, VERBOSE) { "Now holding ${activeLeases.size} lease(s)" }
            }
        }

        return Resource(resource, activeLease)
    }

    inner class ActiveLease(private val job: Job) : KeepAlive {
        override val isClosed: Boolean
            get() = !job.isActive

        override fun close() {
            log(tag, VERBOSE) { "Closing keep alive" }
            if (!job.isActive) {
                log(tag, WARN) { "Already closed!" }
            } else {
                job.cancel()
                activeLeases.remove(this)
            }
        }

        override fun toString(): String = "ActiveLease(job=$job)"
    }

    /**
     * A child resource will be kept alive by this resource, and will be closed once this resource is closed.
     *
     * The backup module is a child resource of the root shell
     * When the root shell closes, the backup module needs to "close" too.
     * But the backupmodule, while open, keeps the root shell alive.
     */
    suspend fun addChild(resource: Resource<*>) = lock.withLock {
        if (!isAlive) {
            log(tag, WARN) { "Adding child resource to an already closed holder: $resource" }
            resource.close()
            return@withLock
        }

        val wrapped = ChildResource(resource)

        if (!children.add(wrapped)) {
            log(tag, WARN) { "Child resource has already been added: $resource" }
        } else {
            log(tag, VERBOSE) { "Resource now has ${children.size} children" }
        }
    }

    private inner class ChildResource(private val resource: Resource<*>) : KeepAlive {
        var closed: Boolean = false

        override val isClosed: Boolean
            get() = closed || resource.isClosed

        override fun close() {
            closed = true
            resource.close()
            children.remove(resource)
        }

        override fun toString(): String = "ChildResource(isClosed=$isClosed, resource=$resource)"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SharedResource<*>.ChildResource) return false

            if (resource != other.resource) return false

            return true
        }

        override fun hashCode(): Int = resource.hashCode()
    }

    suspend fun addParent(parent: HasSharedResource<*>): SharedResource<T> = addParent(parent.sharedResource)

    /**
     * Convenience method to add us as child to a parent. See [addChild]
     * While the parent is alive, we will be kept alive too.
     *
     * rootResource.keepAliveWith(appBackupModule)
     * While the app backup module is active, don't release the root resource so it can be re-used
     */
    suspend fun addParent(parent: SharedResource<*>): SharedResource<T> {
        if (!parent.isAlive) {
            log(tag, WARN) { "Parent(${parent.tag}) is closed, not adding keep alive." }
            return this
        }

        if (parents.contains(parent)) {
            log(tag, WARN) { "Parent already contains us as keep-alive" }
            return this
        }

        log(tag, VERBOSE) { "Adding us as new keep-alive to parent $parent" }

        val ourself = get()

        lock.withLock {
            if (parents.contains(parent)) {
                // Race condition, synchronizing on get() would lead to dead-lock
                ourself.close()
            } else {
                // Store this so we can detect duplicate calls to `keepAliveWith`
                parents[parent] = ourself
                // Add our self as child to the parent, so if the parent is cancelled, we can be cancelled too
                parent.addChild(ourself)
            }
        }

        return this
    }

    override fun close() {
        log(tag) { "close()" }
        childScope.coroutineContext.cancelChildren()
    }

    override fun toString(): String =
        "SharedResource(tag=$tag, leases=${activeLeases.size}, children=${children.size}, parents=${parents.size})"

    companion object {
        fun createKeepAlive(tag: String, scope: CoroutineScope): SharedResource<Any> = SharedResource(
            tag,
            scope,
            callbackFlow {
                send(Any())
                awaitClose()
            }
        )
    }

}
