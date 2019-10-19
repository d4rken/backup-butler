package eu.darken.bb.task.ui.editor.restore.config

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.setupDefaults
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.ui.setTextQuantity
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.task.core.getTaskId
import javax.inject.Inject


class RestoreConfigFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: RestoreConfigFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as RestoreConfigFragmentVDC.Factory
        factory.create(handle, arguments!!.getTaskId()!!)
    })

    @Inject lateinit var adapter: RestoreConfigAdapter

    @BindView(R.id.loading_overlay_counts) lateinit var loadingOverlayCounts: LoadingOverlayView
    @BindView(R.id.count_container) lateinit var countContainer: ViewGroup
    @BindView(R.id.count_backups) lateinit var countTypes: TextView

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.loading_overlay_backuplist) lateinit var loadingOverlayBackupList: LoadingOverlayView
    init {
        layoutRes = R.layout.task_editor_restore_configs_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(adapter, dividers = false)
        requireActivityActionBar().setSubtitle(R.string.restore_options_label)

        vdc.summaryState.observe2(this) { state ->
            countTypes.setTextQuantity(R.plurals.restore_task_x_backups_selected_desc, state.backupTypes.size)
            countTypes.setGone(state.backupTypes.isEmpty())

            countContainer.setInvisible(state.isWorking)
            loadingOverlayCounts.setInvisible(!state.isWorking)
        }

        vdc.configState.observe2(this) { state ->
            adapter.update(state.restoreConfigs.map { Pair(it, { config: Restore.Config -> vdc.updateConfig(config) }) })

            recyclerView.setInvisible(state.isWorking)
            loadingOverlayBackupList.setInvisible(!state.isWorking)
        }

        super.onViewCreated(view, savedInstanceState)
    }

}
