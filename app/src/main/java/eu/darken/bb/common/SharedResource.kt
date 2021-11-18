package eu.darken.bb.common

import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.bb.common.debug.logging.Logging.Priority.WARN
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.error.getStackTraceString
import eu.darken.bb.common.error.tryUnwrap
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.Closeable


@Suppress("ProtectedInFinal")
open class SharedResource<T : Any> constructor(
    private val tag: String,
    private val parentScope: CoroutineScope,
    private val source: Flow<T>,
) : KeepAlive {
    private val childScope = CoroutineScope(
        parentScope.newCoroutineContext(SupervisorJob())
    )

    private val borrowedResources = mutableSetOf<Job>()

    private val childResources = mutableSetOf<Resource<*>>()
    private val childsWeKeepAlive = mutableSetOf<KeepAlive>()
    private val parentsThatKeepUsAlive = mutableMapOf<SharedResource<*>, Resource<T>>()

    override val isClosed: Boolean
        get() = !isAlive

    var isAlive: Boolean = false
        private set

    private val resourceHolder: Flow<T> = source
        .onStart {
            isAlive = true
            log(tag, VERBOSE) { "resourceHolder.doOnSubscribe()" }

            if (borrowedResources.isNotEmpty()) {
                log(tag, WARN) { "Borrowed resources are not empty: $borrowedResources" }
            }
            borrowedResources.forEach { it.cancel() }
            borrowedResources.clear()

            if (childResources.isNotEmpty()) {
                log(tag, WARN) { "Child resources were not empty: $childResources" }
            }

            childResources.clear()

            parentsThatKeepUsAlive.clear()
        }
        .onCompletion {
            isAlive = false
            log(tag, VERBOSE) { "resourceHolder.onCompletion()" }

            // Cancel all borrowed resources
            childScope.coroutineContext.cancelChildren()

            if (borrowedResources.any { it.isActive }) {
                log(tag, WARN) { "Borrowed resources were still alive after cancel: $borrowedResources" }
            }
            borrowedResources.clear()

            childResources.forEach { it.close() }
            childResources.clear()
            parentsThatKeepUsAlive.clear()
        }
        .catch {
            log(tag, WARN) { "resourceHolder.onError(): $it" }
            throw it
        }
        .onEach { log(tag) { "resourceHolder.onNext(): $it" } }
        .shareIn(parentScope, SharingStarted.WhileSubscribed(), replay = 1)

    suspend fun get(): Resource<T> {
        if (BBDebug.isDebug() && !isAlive) {
            log(tag, VERBOSE) { "get() Reviving resource: ${Throwable().getStackTraceString()}" }
        }

        val job = resourceHolder.launchIn(childScope)
        job.invokeOnCompletion { log(tag, VERBOSE) { "get() job completed" } }

        if (job.isActive) {
            borrowedResources.add(job)
        } else {
            log(tag) { "ResourceHolder fails to provide active job, error during init?" }
        }

        val resource = try {
            if (BBDebug.isDebug()) log(tag, VERBOSE) { "get(): Waiting for resource" }
            resourceHolder.first()
        } catch (e: Exception) {
            job.cancel()
            throw e.tryUnwrap()
        }

        val keepAlive = object : KeepAlive {
            override val isClosed: Boolean
                get() = !job.isActive

            override fun close() {
                log(tag, VERBOSE) { "Closing keep alive" }
                if (!job.isActive) {
                    Timber.tag(tag).v("Already closed!")
                } else {
                    job.cancel()
                    borrowedResources.remove(job)
                }
            }

        }

        return Resource(resource, keepAlive)
    }

    override fun close() {
        childScope.coroutineContext.cancelChildren()
    }

    /**
     * The backup module is a child resource of the root shell
     * When the root shell closes, the backup module needs to "close" too.
     * But the backupmodule, while open, keeps the root shell alive.
     */
    fun addChildResource(resource: Resource<*>) {
        if (!isAlive) {
            Timber.tag(tag).w("Adding child resource to an already closed holder: %s", resource)
            resource.close()
            return
        }

        if (!childResources.add(resource)) {
            Timber.tag(tag).w("Child resource has already been added: %s", resource)
            return
        }

        val wrapped = object : KeepAlive {
            var closed: Boolean = false

            override val isClosed: Boolean
                get() = closed || resource.isClosed

            override fun close() {
                resource.close()
                closed = true
                childsWeKeepAlive.remove(this)
                childResources.remove(resource)
            }

        }
        childsWeKeepAlive.add(wrapped)
    }

    suspend fun keepAliveWith(parent: HasSharedResource<*>): SharedResource<T> = keepAliveWith(parent.sharedResource)

    /**
     * Keep a shared resource alive.
     * rootResource.keepAliveWith(appBackupModule)
     * While the app backup module is active, don't release the root resource so it can be re-used
     */
    suspend fun keepAliveWith(parent: SharedResource<*>): SharedResource<T> {
        if (!parent.isAlive) {
            log(tag, WARN) { "Parent(${parent.tag}) is closed, not adding keep alive." }
            return this
        }

        if (parentsThatKeepUsAlive.contains(parent)) {
            log(tag, WARN) { "Parent already contains us as keep-alive" }
            return this
        }


        Timber.tag(tag).v("Adding us as new keep-alive to parent $parent")

        val ourself = get()

        synchronized(parentsThatKeepUsAlive) {
            if (parentsThatKeepUsAlive.contains(parent)) {
                // Race condition, synchronizing on get() would lead to dead-lock
                ourself.close()
            } else {
                // Store this so we can detect duplicate calls to `keepAliveWith`
                parentsThatKeepUsAlive[parent] = ourself
                // Add our self as child to the parent, so if the parent is cancelled, we can be cancelled too
                parent.addChildResource(ourself)
            }
        }

        return this
    }

    data class Resource<T>(
        private val _item: T,
        val keepAlive: KeepAlive
    ) : KeepAlive by keepAlive {
        val item: T
            get() {
                check(!keepAlive.isClosed) { "Trying to access closed resource!" }
                return _item
            }
    }

    companion object {
        fun createKeepAlive(
            tag: String,
            scope: CoroutineScope,
        ): SharedResource<Any> = SharedResource(
            tag,
            scope,
            callbackFlow {
                send(Any())
                awaitClose()
            }
        )
    }

}

interface HasSharedResource<T : Any> {
    val sharedResource: SharedResource<T>

    suspend fun keepAliveWith(parent: SharedResource<*>) {
        sharedResource.keepAliveWith(parent)
    }

    suspend fun <C : HasSharedResource<T>> C.keepAliveWith(parent: HasSharedResource<*>) = apply {
        sharedResource.keepAliveWith(parent.sharedResource)
    }

}

interface KeepAlive : Closeable {
    val isClosed: Boolean

    override fun close()
}