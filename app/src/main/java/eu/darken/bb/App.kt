package eu.darken.bb

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import androidx.work.Configuration
import com.uber.rxdogtag.RxDogTag
import dagger.Lazy
import dagger.android.*
import eu.darken.bb.common.dagger.AppInjector
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.workers.InjectionWorkerFactory
import timber.log.Timber
import javax.inject.Inject


open class App
    : Application(), Configuration.Provider, HasActivityInjector, HasServiceInjector, HasBroadcastReceiverInjector {

    companion object {
        internal val TAG = logTag("App")

        fun logTag(vararg tags: String): String {
            val sb = StringBuilder("BB:")
            for (i in tags.indices) {
                sb.append(tags[i])
                if (i < tags.size - 1) sb.append(":")
            }
            return sb.toString()
        }
    }

    @Inject lateinit var appComponent: AppComponent
    @Inject lateinit var activityInjector: DispatchingAndroidInjector<Activity>
    @Inject lateinit var receiverInjector: DispatchingAndroidInjector<BroadcastReceiver>
    @Inject lateinit var serviceInjector: DispatchingAndroidInjector<Service>

    @Inject lateinit var workerFactory: InjectionWorkerFactory

    @Inject lateinit var uiSettings: UISettings
    @Inject lateinit var bbDebug: Lazy<BBDebug>


    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            RxDogTag.builder().install()
        }

        AppInjector.init(this)

        bbDebug.get()

        // Sets theme mode
        uiSettings.theme = uiSettings.theme

        Timber.tag(TAG).d("onCreate() done!")
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun activityInjector(): AndroidInjector<Activity> = activityInjector

    override fun serviceInjector(): AndroidInjector<Service> = serviceInjector

    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> = receiverInjector
}
