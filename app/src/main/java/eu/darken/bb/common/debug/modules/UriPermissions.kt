package eu.darken.bb.common.debug.modules

import android.content.Context
import android.util.Log
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.debug.DebugModule
import eu.darken.bb.common.debug.DebugModuleHost
import eu.darken.bb.common.debug.DebugOptions
import eu.darken.bb.common.debug.compareIgnorePath
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class UriPermissions @AssistedInject constructor(
        @Assisted host: DebugModuleHost,
        @AppContext private val context: Context
) : DebugModule {
    companion object {
        private val TAG = App.logTag("Debug", "UriPermissions")
    }

    private var previousOptions: DebugOptions = DebugOptions.default()

    init {
        host.observeOptions()
                .observeOn(Schedulers.io())
                .filter { !previousOptions.compareIgnorePath(it) && it.level <= Log.INFO }
                .doOnNext { previousOptions = it }
                .map { context.contentResolver.persistedUriPermissions }
                .subscribe(
                        { result ->
                            Timber.tag(TAG).d("Persisted uri permissions:")
                            for (s in result) Timber.tag(TAG).d("%d: %s", result.indexOf(s), s)
                        },
                        { e -> Timber.tag(TAG).e(e, "Failed to get uri permissions") }
                )
    }

    @AssistedInject.Factory
    interface Factory : DebugModule.Factory<UriPermissions>
}