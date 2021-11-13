package eu.darken.bb.common.debug.modules

import android.content.Context
import android.util.Log
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.debug.*
import eu.darken.bb.common.debug.logging.logTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import timber.log.Timber

class UriPermissions @AssistedInject constructor(
    @Assisted host: DebugModuleHost,
    @ApplicationContext private val context: Context,
    @DebugScope private val debugScope: CoroutineScope,
) : DebugModule {
    companion object {
        private val TAG = logTag("Debug", "UriPermissions")
    }

    private var previousOptions: DebugOptions = DebugOptions.default()

    init {
        host.observeOptions()
            .filter { !previousOptions.compareIgnorePath(it) && it.level <= Log.INFO }
            .onEach { previousOptions = it }
            .map { context.contentResolver.persistedUriPermissions }
            .onEach { result ->
                Timber.tag(TAG).d("Persisted uri permissions:")
                for (s in result) Timber.tag(TAG).d("%d: %s", result.indexOf(s), s)
            }
            .catch { e -> Timber.tag(TAG).e(e, "Failed to get uri permissions") }
            .launchIn(debugScope)
    }

    @AssistedFactory
    interface Factory : DebugModule.Factory<UriPermissions>
}