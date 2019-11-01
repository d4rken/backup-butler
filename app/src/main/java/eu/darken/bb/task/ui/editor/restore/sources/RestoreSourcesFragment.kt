package eu.darken.bb.task.ui.editor.restore.sources

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.ItemSwipeTool
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setGone
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
        factory.create(handle, requireArguments().getTaskId()!!)
    })

    @Inject lateinit var adapter: BackupAdapter

    @BindView(R.id.loading_overlay_counts) lateinit var loadingOverlayCounts: LoadingOverlayView
    @BindView(R.id.count_container) lateinit var countContainer: ViewGroup
    @BindView(R.id.count_backups) lateinit var countBackups: TextView

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.loading_overlay_backuplist) lateinit var loadingOverlayBackupList: LoadingOverlayView

    init {
        layoutRes = R.layout.task_editor_restore_sources_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(adapter, dividers = false)
        requireActivityActionBar().setSubtitle(R.string.label_backupspec_generators)

        vdc.summaryState.observe2(this) { state ->
            countBackups.setTextQuantity(R.plurals.restore_task_x_backups_selected_desc, state.sourceBackups.size)
            countBackups.setGone(state.sourceBackups.isEmpty())

            countContainer.setInvisible(state.isWorking)
            loadingOverlayCounts.setInvisible(!state.isWorking)
        }

        vdc.backupsState.observe2(this) { state ->
            adapter.update(state.backups)

            recyclerView.setInvisible(state.isWorking)
            loadingOverlayBackupList.setInvisible(!state.isWorking)
        }

        val swipeTool = ItemSwipeTool(
                ItemSwipeTool.SwipeAction(
                        direction = ItemSwipeTool.SwipeAction.Direction.RIGHT,
                        icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_cancel)!!,
                        label = getString(R.string.action_exclude),
                        background = ColorDrawable(Color.RED),
                        callback = { viewHolder, _ ->
                            vdc.exclude(adapter.data[viewHolder.adapterPosition])
                        }
                )
        )
        swipeTool.attach(recyclerView)

        super.onViewCreated(view, savedInstanceState)
    }

}
