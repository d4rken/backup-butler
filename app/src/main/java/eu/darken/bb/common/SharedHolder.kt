package eu.darken.bb.common

import eu.darken.bb.common.debug.BBDebug
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.Closeable
import java.io.IOException


@Suppress("ProtectedInFinal")
open class SharedHolder<T> constructor(
        private val tag: String,
        private val sourcer: (ResourceEmitter<T>) -> Unit
) {
    protected var activeTokens = CompositeDisposable().apply { dispose() }
    protected var childsWeKeepAlive = CompositeDisposable().apply { dispose() }
    protected var childResources = mutableSetOf<Resource<*>>()
    protected var parentsThatKeepUsAlive = mutableMapOf<SharedHolder<*>, Resource<T>>()

    val isAlive: Boolean
        get() = !activeTokens.isDisposed

    private val resourceHolder: Observable<T> = Observable
            .create<T> { internalEmitter ->
                val resourceEmitter = object : ResourceEmitter<T> {
                    override fun onAvailable(t: T) {
                        internalEmitter.onNext(t)
                    }

                    override fun onEnd() {
                        internalEmitter.onComplete()
                    }

                    override fun isDisposed(): Boolean {
                        return internalEmitter.isDisposed
                    }

                    override fun onError(t: Throwable) {
                        internalEmitter.tryOnError(t)
                    }

                    override fun setCancellable(c: () -> Unit) {
                        internalEmitter.setCancellable { c() }
                    }
                }
                try {
                    sourcer(resourceEmitter)
                } catch (e: Throwable) {
                    internalEmitter.tryOnError(e)
                    return@create
                }
            }
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                Timber.tag(tag).v("resourceHolder.doOnSubscribe()")
                require(activeTokens.isDisposed) { "Previous active tokens were not disposed!" }
                activeTokens.dispose()
                activeTokens = CompositeDisposable()

                require(childsWeKeepAlive.isDisposed) { "Previous child tokens were not disposed!" }
                childsWeKeepAlive.dispose()
                childResources.clear()
                parentsThatKeepUsAlive.clear()
                childsWeKeepAlive = CompositeDisposable()
            }
            .doFinally {
                Timber.tag(tag).v("resourceHolder.doFinally()")
                activeTokens.dispose()
                childsWeKeepAlive.dispose()

                childResources.clear()
                parentsThatKeepUsAlive.clear()
            }
            .doOnError { Timber.tag(tag).v("resourceHolder.onError(): %s", it) }
            .doOnComplete { Timber.tag(tag).v("resourceHolder.onComplete()") }
            .doOnNext { Timber.tag(tag).v("resourceHolder.onNext(): %s", it) }
            .replay(1)
            .refCount()

    @Throws(IOException::class)
    fun get(): Resource<T> {
        if (BBDebug.isDebug() && !isAlive) {
            Timber.tag(tag).v("get() Reviving resource: %s", Throwable().getStackTraceString())
        }

        val keepAlive = resourceHolder.subscribe({ }, { })

        if (!activeTokens.add(keepAlive)) {
            Timber.tag(tag).d("Can't add token, already disposed!")
        }

        val resource = try {
            if (BBDebug.isDebug()) Timber.tag(tag).v("get(): Waiting for resource")
            resourceHolder.blockingFirst()
        } catch (e: Exception) {
            keepAlive.dispose()
            throw e
        }

        val wrappedKeepAlive = object : Disposable {
            override fun isDisposed(): Boolean = keepAlive.isDisposed

            override fun dispose() {
                if (keepAlive.isDisposed) {
                    Timber.tag(tag).v("Already disposed!")
                } else {
                    activeTokens.remove(keepAlive)
                    keepAlive.dispose()
                }
            }
        }

        return Resource(resource, wrappedKeepAlive)
    }

    fun closeAll() {
        activeTokens.dispose()
    }

    fun addChildResource(resource: Resource<*>) {
        if (!isAlive) {
            Timber.tag(tag).w("Adding child resource to an already closed holder: %s", resource)
        }

        if (!childResources.add(resource)) {
            Timber.tag(tag).w("Child resource has already been added: %s", resource)
            return
        }

        val wrapped = object : Disposable {
            var disposed: Boolean = false
            override fun isDisposed(): Boolean = disposed

            override fun dispose() {
                resource.close()
                disposed = true
                childsWeKeepAlive.remove(this)
                childResources.remove(resource)
            }

        }
        childsWeKeepAlive.add(wrapped)
    }

    fun keepAliveWith(parent: HasKeepAlive<*>): SharedHolder<T> {
        return keepAliveWith(parent.keepAlive)
    }

    fun keepAliveWith(parent: SharedHolder<*>): SharedHolder<T> {
        if (!parent.isAlive) {
            Timber.tag(tag).w("Parent(%s) is closed, not adding keep alive.", parent.tag)
            return this
        }

        if (!parentsThatKeepUsAlive.contains(parent)) {
            Timber.tag(tag).v("Adding us as new keep-alive to parent %s", parent)
            val ourself = get()
            synchronized(parentsThatKeepUsAlive) {
                if (parentsThatKeepUsAlive.contains(parent)) {
                    // Race condition, synchronizing on get() would lead to dead-lock
                    ourself.close()
                } else {
                    parentsThatKeepUsAlive[parent] = ourself
                    parent.addChildResource(ourself)
                }
            }
        }

        return this
    }

    data class Resource<T>(
            private val _item: T,
            private val keepAlive: Disposable
    ) : Closeable, AutoCloseable {
        val item: T
            get() {
                check(!keepAlive.isDisposed) { "Trying to access closed resource!" }
                return _item
            }

        override fun close() = keepAlive.dispose()
    }

    interface ResourceEmitter<T> {
        fun onAvailable(t: T)

        fun onEnd()

        fun isDisposed(): Boolean

        fun onError(t: Throwable)

        fun setCancellable(c: () -> Unit)
    }

    interface HasKeepAlive<T> {
        val keepAlive: SharedHolder<T>

        fun keepAliveWith(parent: HasKeepAlive<*>) {
            keepAlive.keepAliveWith(parent)
        }

        fun <C : HasKeepAlive<T>> C.keepAliveWIth(parent: HasKeepAlive<*>) = apply {
            keepAlive.keepAliveWith(parent)
        }

    }

    companion object {
        fun createKeepAlive(tag: String): SharedHolder<Any> {
            return SharedHolder(tag) {
                it.onAvailable(Any())
            }
        }
    }

}