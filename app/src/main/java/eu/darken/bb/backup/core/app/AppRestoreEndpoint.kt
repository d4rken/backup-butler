package eu.darken.bb.backup.core.app

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.processor.core.mm.MMDataRepo
import io.reactivex.Observable
import javax.inject.Inject

class AppRestoreEndpoint @Inject constructor(
        @AppContext override val context: Context,
        private val apkExporter: APKExporter,
        private val MMDataRepo: MMDataRepo
) : Restore.Endpoint, Progress.Client, HasContext {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)


    override fun restore(config: Restore.Config, unit: Backup.Unit): Boolean {
        TODO("not implemented")
    }

    override fun close() {
//        TODO("not implemented")
    }

    override fun toString(): String = "AppEndpoint()"

    companion object {
        val TAG = App.logTag("AppBackup", "Endpoint")
    }

}