package eu.darken.bb.common.root

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.GeneralSettings
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.rxshell.root.RootContext
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RootManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val generalSettings: GeneralSettings
) {
    companion object {
        internal val TAG = logTag("RootManager")
    }

    val rootContext: Single<RootContext> = Single.just(generalSettings)
        .flatMap {
            when {
                generalSettings.isRootDisabled -> {
                    Timber.tag(TAG).w("Rootcheck is disabled!")
                    Single.just(RootContext.EMPTY)
                }
                else -> {
                    RootContext.Builder(context).build()
                        .timeout(15, TimeUnit.SECONDS)
                }
            }
        }
        .subscribeOn(Schedulers.io())
        .doOnSubscribe { Timber.tag(TAG).d("Acquiring RootContext...") }
        .doOnSuccess { Timber.tag(TAG).i("RootContext: %s", it) }
        .onErrorReturnItem(RootContext.EMPTY)
        .cache()
        .doOnSubscribe { Timber.tag(TAG).v("Client sub: %s", Thread.currentThread()) }
        .doOnDispose { Timber.tag(TAG).v("Client disp") }

}
