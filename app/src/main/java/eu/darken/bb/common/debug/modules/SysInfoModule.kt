package eu.darken.bb.common.debug.modules

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.Log
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.debug.*
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.hasApiLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@SuppressLint("NewApi")
class SysInfoModule @AssistedInject constructor(
    @Assisted host: DebugModuleHost,
    @ApplicationContext context: Context,
    @DebugScope private val debugScope: CoroutineScope,
) : DebugModule {
    private var previousOptions: DebugOptions = DebugOptions.default()

    init {
        host.observeOptions()
            .filter { !previousOptions.compareIgnorePath(it) && it.level <= Log.INFO }
            .onEach { previousOptions = it }
            .onEach {
                if (hasApiLevel(24)) {
                    val appLocales = context.resources.configuration.locales
                    val deviceLocales = Resources.getSystem().configuration.locales
                    Timber.tag(TAG).d("App locales: %s, Device locales: %s", appLocales, deviceLocales)
                } else {
                    val appLocale = context.resources.configuration.locale
                    val deviceLocale = Resources.getSystem().configuration.locale
                    Timber.tag(TAG).d("App locales: %s, Device locales: %s", appLocale, deviceLocale)
                }
            }
            .launchIn(debugScope)
    }

    companion object {
        private val TAG = logTag("Debug", "SysInfoModule")
    }

    @AssistedFactory
    interface Factory : DebugModule.Factory<SysInfoModule>
}