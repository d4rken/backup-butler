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

                    override fun onError(t: Throwable): Boolean {
                        Timber.tag(tag).d("onError: %s", t)
                        return internalEmitter.tryOnError(t)
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
                Timber.tag(tag).d("doFinally()")
                synchronized(this@SharedResource) {
                    activeTokens.dispose()
                    childTokens.dispose()
                }
            }
            .replay(1)
            .refCount()

    @Throws(IOException::class)
    fun getResource(): Token<T> {
        val keepAlive = resourceHolder.subscribe({

        }, {
            Timber.tag(tag).d("getResource().onError(): %s", it)
        })

        if (activeTokens.add(keepAlive)) {
            Timber.tag(tag).d("Adding token, now: %d", activeTokens.size())
        } else {
            Timber.tag(tag).d("Can't add token, already disposed!")
        }

        if (BBDebug.isDebug()) {
            Timber.tag(tag).v("getResource() Caller: %s", Throwable().getStackTraceString())
        }

        val resource = try {
            resourceHolder.blockingFirst()
        } catch (e: Exception) {
            keepAlive.dispose()
            throw e.unwrapIf(RuntimeException::class)
        }

        val wrappedKeepAlive = object : Disposable {
            override fun isDisposed(): Boolean {
                return keepAlive.isDisposed
            }

            override fun dispose() {
                if (keepAlive.isDisposed) Timber.tag(tag).v("Already disposed!")
                activeTokens.remove(keepAlive)
                Timber.tag(tag).d("Removing token, now: %d", activeTokens.size())
                keepAlive.dispose()
            }
        }

        return Token(resource, wrappedKeepAlive)
    }

    fun addChildResource(token: Token<out Any>) {
        synchronized(this@SharedResource) {
            childTokens.add(object : Disposable {
                var disposed: Boolean = false
                override fun isDisposed(): Boolean = disposed

                override fun dispose() {
                    token.close()
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

    data class Token<T>(
            private val internalResource: T,
            private val keepAlive: Disposable
    ) : Closeable, AutoCloseable {
        val resource: T
            get() {
                check(!keepAlive.isDisposed) { "Trying to access closed resource!" }
                return internalResource
            }

        override fun close() = keepAlive.dispose()
    }

    interface ResourceEmitter<T> {
        fun onAvailable(t: T)

        fun onEnd()

        fun isDisposed(): Boolean

        fun onError(t: Throwable): Boolean

        fun setCancellable(c: () -> Unit?)
    }

}