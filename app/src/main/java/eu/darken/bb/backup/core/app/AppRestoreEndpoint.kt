package eu.darken.bb.backup.core.app

import android.content.Context
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.processor.core.tmp.TmpDataRepo

class AppRestoreEndpoint @AssistedInject constructor(
        @Assisted val progressClient: Progress.Client?,
        @AppContext override val context: Context,
        private val apkExporter: APKExporter,
        private val tmpDataRepo: TmpDataRepo
) : Restore.Endpoint, Progress.Client, HasContext {


    override fun updateProgress(update: (Progress.Data) -> Progress.Data) {
        progressClient?.updateProgress(update)
    }

    override fun restore(config: Restore.Config): Boolean {
        TODO("not implemented")
    }

    override fun toString(): String = "AppEndpoint()"

    companion object {
        val TAG = App.logTag("AppBackup", "Endpoint")
    }

    @AssistedInject.Factory
    interface Factory : Restore.Endpoint.Factory<AppRestoreEndpoint>

}