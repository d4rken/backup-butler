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
class SharedResource<T> constructor(
        private val tag: String,
        private val sourcer: (ResourceEmitter<T>) -> Unit
) {
    protected var activeTokens = CompositeDisposable().apply { dispose() }
    protected var childTokens = CompositeDisposable().apply { dispose() }

    val isOpen: Boolean
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

                    override fun setCancellable(c: () -> Unit?) {
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
                synchronized(this@SharedResource) {
                    if (!activeTokens.isDisposed) Timber.tag(tag).e("Previous active tokens were not disposed!")
                    activeTokens.dispose()
                    activeTokens = CompositeDisposable()

                    if (!childTokens.isDisposed) Timber.tag(tag).e("Previous child tokens were not disposed!")
                    childTokens.dispose()
                    childTokens = CompositeDisposable()
                }
            }
            .doFinally {
                Timber.tag(tag).v("resourceHolder.doFinally()")
                synchronized(this@SharedResource) {
                    activeTokens.dispose()
                    childTokens.dispose()
                }
            }
            .doOnError { Timber.tag(tag).v("resourceHolder.onError(): %s", it) }
            .doOnComplete { Timber.tag(tag).v("resourceHolder.onComplete()") }
            .doOnNext { Timber.tag(tag).v("resourceHolder.onNext(): %s", it) }
            .replay(1)
            .refCount()

    @Throws(IOException::class)
    fun get(): Resource<T> {
        val keepAlive = resourceHolder.subscribe({ }, { })

        if (activeTokens.add(keepAlive)) {
            Timber.tag(tag).v("Adding token, now: %d", activeTokens.size())
        } else {
            Timber.tag(tag).d("Can't add token, already disposed!")
        }

        if (BBDebug.isDebug()) {
            Timber.tag(tag).v("get() Caller: %s", Throwable().getStackTraceString())
        }

        val resource = try {
            Timber.tag(tag).v("get(): Waiting for resource")
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
                    Timber.tag(tag).v("Removing token, now: %d", activeTokens.size())
                    keepAlive.dispose()
                }
            }
        }

        return Resource(resource, wrappedKeepAlive)
    }

    fun addChildResource(resource: Resource<out Any>) {
        synchronized(this@SharedResource) {
            if (!isOpen) {
                Timber.tag(tag).w("Adding child to an already closed resource: %s", resource)
            }
            childTokens.add(object : Disposable {
                var disposed: Boolean = false
                override fun isDisposed(): Boolean = disposed

                override fun dispose() {
                    resource.close()
                    disposed = true
                }

            })
        }
    }

    fun closeAll() {
        synchronized(this@SharedResource) {
            activeTokens.dispose()
        }
    }

    data class Resource<T>(
            private val _data: T,
            private val keepAlive: Disposable
    ) : Closeable, AutoCloseable {
        val data: T
            get() {
                check(!keepAlive.isDisposed) { "Trying to access closed resource!" }
                return _data
            }

        override fun close() = keepAlive.dispose()
    }

    interface ResourceEmitter<T> {
        fun onAvailable(t: T)

        fun onEnd()

        fun isDisposed(): Boolean

        fun onError(t: Throwable)

        fun setCancellable(c: () -> Unit?)
    }

}