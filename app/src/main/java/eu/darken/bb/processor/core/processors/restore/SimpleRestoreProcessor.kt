package eu.darken.bb.processor.core.processors.restore

import android.content.Context
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.OpStatus
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.processor.core.processors.SimpleBaseProcessor
import eu.darken.bb.processor.core.tmp.TmpDataRepo
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageFactory
import eu.darken.bb.storage.core.StorageRefRepo
import eu.darken.bb.task.core.Task

class SimpleRestoreProcessor @AssistedInject constructor(
        @Assisted progressParent: Progress.Client,
        @AppContext context: Context,
        private val restoreEndpointFactories: @JvmSuppressWildcards Map<Backup.Type, Restore.Endpoint.Factory<out Restore.Endpoint>>,
        @StorageFactory private val storageFactories: Set<@JvmSuppressWildcards Storage.Factory>,
        private val tmpDataRepo: TmpDataRepo,
        private val generatorRepo: GeneratorRepo,
        private val storageRefRepo: StorageRefRepo
) : SimpleBaseProcessor(context, progressParent) {


    override fun doProcess(task: Task) {
        task as Task.Restore

        var success = 0
        var skipped = 0
        var error = 0

        resultBuilder.primary(
                OpStatus(context).apply {
                    this.success = success
                    this.skipped = skipped
                    this.failed = error
                }.toDisplayString()
        )
    }

    @AssistedInject.Factory
    interface Factory : Processor.Factory<SimpleRestoreProcessor>

    companion object {
        private val TAG = App.logTag("Processor", "Restore", "Simple")
    }
}