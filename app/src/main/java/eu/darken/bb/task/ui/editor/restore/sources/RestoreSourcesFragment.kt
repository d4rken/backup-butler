package eu.darken.bb.task.ui.editor.restore.sources

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.lists.ItemSwipeTool
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.ui.setTextQuantity
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorRestoreSourcesFragmentBinding
import eu.darken.bb.task.ui.editor.restore.config.RestoreConfigFragmentArgs
import javax.inject.Inject

@AndroidEntryPoint
class RestoreSourcesFragment : SmartFragment(R.layout.task_editor_restore_sources_fragment) {

    val navArgs by navArgs<RestoreSourcesFragmentArgs>()

    private val vdc: RestoreSourcesFragmentVDC by viewModels()
    private val ui: TaskEditorRestoreSourcesFragmentBinding by viewBinding()

    @Inject lateinit var adapter: BackupAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.recyclerview.setupDefaults(adapter, dividers = false)

        vdc.summaryState.observe2(this) { state ->
            ui.countBackups.setTextQuantity(
                R.plurals.task_editor_restore_x_backups_selected_desc,
                state.sourceBackups.size
            )
            ui.setupbar.buttonPositiveSecondary.setGone(state.sourceBackups.isEmpty())

            ui.countContainer.setInvisible(state.isWorking)
            ui.loadingOverlayCounts.setInvisible(!state.isWorking)
        }

        vdc.backupsState.observe2(this) { state ->
            adapter.update(state.backups)

            ui.recyclerview.setInvisible(state.isWorking)
            ui.loadingOverlayBackuplist.setInvisible(!state.isWorking)

            val swipeTool = ItemSwipeTool(
                ItemSwipeTool.SwipeAction(
                    direction = ItemSwipeTool.SwipeAction.Direction.RIGHT,
                    icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_cancel)!!,
                    label = getString(R.string.general_exclude_action),
                    background = ColorDrawable(getColorForAttr(R.attr.colorError)),
                    callback = { viewHolder, _ ->
                        vdc.exclude(adapter.data[viewHolder.adapterPosition])
                    }
                )
            )
            swipeTool.attach(ui.recyclerview)

            ui.setupbar.buttonPositiveSecondary.clicksDebounced().subscribe {
                findNavController().navigate(
                    R.id.nav_action_next,
                    RestoreConfigFragmentArgs(taskId = navArgs.taskId).toBundle()
                )
            }

            vdc.finishEvent.observe2(this) {
                requireActivity().finish()
            }

            super.onViewCreated(view, savedInstanceState)
        }

    }
}
