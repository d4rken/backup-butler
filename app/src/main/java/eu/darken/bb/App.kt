package eu.darken.bb

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import androidx.work.Configuration
import androidx.work.WorkManager
import com.uber.rxdogtag.RxDogTag
import dagger.android.*
import eu.darken.bb.common.dagger.AppInjector
import eu.darken.bb.debug.BBDebug
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.workers.InjectionWorkerFactory
import timber.log.Timber
import javax.inject.Inject


open class App : Application(), HasActivityInjector, HasServiceInjector, HasBroadcastReceiverInjector {

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
    @Inject lateinit var bbDebug: BBDebug

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            RxDogTag.builder().install()
        }
        AppInjector.init(this)
        WorkManager.initialize(this, Configuration.Builder().setWorkerFactory(workerFactory).build())

        // Sets theme mode
        uiSettings.theme = uiSettings.theme

        Timber.tag(TAG).d("onCreate() done!")
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityInjector

    override fun serviceInjector(): AndroidInjector<Service> = serviceInjector

    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> = receiverInjector
}
