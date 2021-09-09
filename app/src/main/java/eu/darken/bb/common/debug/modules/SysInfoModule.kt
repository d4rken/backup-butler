package eu.darken.bb.common.debug.modules

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.Log
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.App
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.debug.DebugModule
import eu.darken.bb.common.debug.DebugModuleHost
import eu.darken.bb.common.debug.DebugOptions
import eu.darken.bb.common.debug.compareIgnorePath
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

@SuppressLint("NewApi")
class SysInfoModule @AssistedInject constructor(
    @Assisted host: DebugModuleHost,
    @ApplicationContext context: Context
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

    @AssistedFactory
    interface Factory : DebugModule.Factory<SysInfoModule>
}