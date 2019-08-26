package eu.darken.bb.task.ui.editor.restore.sources

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.setupDefaults
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.ui.setTextQuantity
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.task.core.getTaskId
import javax.inject.Inject


class RestoreSourcesFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: RestoreSourcesFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as RestoreSourcesFragmentVDC.Factory
        factory.create(handle, arguments!!.getTaskId()!!)
    })

    @Inject lateinit var adapter: RestoreSourcesAdapter

    @BindView(R.id.loading_overlay_counts) lateinit var loadingOverlayCounts: LoadingOverlayView
    @BindView(R.id.count_container) lateinit var countContainer: ViewGroup
    @BindView(R.id.count_storages) lateinit var countStorages: TextView
    @BindView(R.id.count_specs) lateinit var countSpecs: TextView
    @BindView(R.id.count_backups) lateinit var countBackups: TextView

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.loading_overlay_backuplist) lateinit var loadingOverlayBackupList: LoadingOverlayView

    init {
        layoutRes = R.layout.task_editor_restore_sources_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(adapter, dividers = false)
        requireActivityActionBar().setSubtitle(R.string.label_sources)

        vdc.countState.observe2(this) { state ->
            countStorages.setTextQuantity(R.plurals.restore_task_x_backup_storages_desc, state.sourceStorages.size)
            countSpecs.setTextQuantity(R.plurals.restore_task_x_backup_backupspecs_desc, state.sourceBackupSpecs.size)
            countBackups.setTextQuantity(R.plurals.restore_task_x_backup_specific_desc, state.sourceBackups.size)

            countContainer.setInvisible(state.isWorking)
            loadingOverlayCounts.setInvisible(!state.isWorking)
        }

        vdc.backupsState.observe2(this) { state ->
            recyclerView.setInvisible(state.isWorking)
            loadingOverlayBackupList.setInvisible(!state.isWorking)
            loadingOverlayBackupList.setPrimaryText(R.string.todo)
        }

        super.onViewCreated(view, savedInstanceState)
    }

}
