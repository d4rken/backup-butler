package eu.darken.bb.common.root

import android.content.Context
import com.jakewharton.rx.replayingShare
import eu.darken.bb.App
import eu.darken.bb.AppComponent
import eu.darken.bb.CoreSettings
import eu.darken.bb.common.dagger.AppContext
import eu.darken.rxshell.root.RootContext
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AppComponent.Scope
class RootManager @Inject constructor(
        @AppContext private val context: Context,
        private val coreSettings: CoreSettings
) {
    companion object {
        internal val TAG = App.logTag("RootManager")
    }

    private val rootContextPub: BehaviorSubject<RootContext> = BehaviorSubject.create()

    val rootContext: Observable<RootContext> = rootContextPub
            .subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
            .doOnSubscribe {
                Timber.tag(TAG).d("Acquiring RootContext...")

                Single
                        .fromCallable {
                            return@fromCallable when {
                                coreSettings.isRootDisabled -> {
                                    Timber.tag(TAG).w("Rootcheck is disabled!")
                                    RootContext.EMPTY
                                }
                                else -> {
                                    RootContext.Builder(context).build()
                                            .timeout(15, TimeUnit.SECONDS)
                                            .blockingGet()
                                }
                            }
                        }
                        .subscribeOn(Schedulers.io())
                        .doOnError { Timber.tag(TAG).e(it, "Failed to setup RootContext.") }
                        .onErrorReturnItem(RootContext.EMPTY)
            }
            .doOnNext { Timber.tag(TAG).i("RootContext: %s", it) }
            .replayingShare()
            .doOnSubscribe { Timber.tag(TAG).v("Client sub: %s", Thread.currentThread()) }
            .doOnDispose { Timber.tag(TAG).v("Client disp") }


    fun peekRoot(): RootContext? {
        return rootContextPub.value
    }
}
