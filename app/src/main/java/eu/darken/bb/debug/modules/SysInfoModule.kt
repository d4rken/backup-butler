package eu.darken.bb.debug.modules

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.debug.DebugModule
import eu.darken.bb.debug.DebugOptions
import eu.darken.bb.debug.compareIgnorePath
import eu.thedarken.sdm.tools.debug.DebugModuleHost
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

@SuppressLint("NewApi")
class SysInfoModule @AssistedInject constructor(
        @Assisted host: DebugModuleHost,
        @AppContext context: Context
) : DebugModule {
    private var previousOptions: DebugOptions = DebugOptions.default()

    init {
        host.observeOptions()
                .observeOn(Schedulers.io())
                .filter { !previousOptions.compareIgnorePath(it) && it.level <= Log.INFO }
                .doOnNext { previousOptions = it }
                .subscribe {
                    if (ApiHelper.hasAndroidN()) {
                        val appLocales = context.resources.configuration.locales
                        val deviceLocales = Resources.getSystem().configuration.locales
                        Timber.tag(TAG).d("App locales: %s, Device locales: %s", appLocales, deviceLocales)
                    } else {
                        val appLocale = context.resources.configuration.locale
                        val deviceLocale = Resources.getSystem().configuration.locale
                        Timber.tag(TAG).d("App locales: %s, Device locales: %s", appLocale, deviceLocale)
                    }
                }
    }

    companion object {
        private val TAG = App.logTag("Debug", "SysInfoModule")
    }

    @AssistedInject.Factory
    interface Factory : DebugModule.Factory<SysInfoModule>
}