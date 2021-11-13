package eu.darken.bb.task.ui.editor.restore.sources

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.lists.ItemSwipeTool
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.ui.setTextQuantity
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorRestoreSourcesFragmentBinding
import javax.inject.Inject

@AndroidEntryPoint
class RestoreSourcesFragment : Smart2Fragment(R.layout.task_editor_restore_sources_fragment) {

    val navArgs by navArgs<RestoreSourcesFragmentArgs>()

    override val vdc: RestoreSourcesFragmentVDC by viewModels()
    override val ui: TaskEditorRestoreSourcesFragmentBinding by viewBinding()

    @Inject lateinit var adapter: BackupAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            backupList.setupDefaults(adapter, dividers = false)
            toolbar.apply {
                setupWithNavController(findNavController())
            }
        }

        vdc.summaryState.observe2(this) { state ->
            ui.countBackups.setTextQuantity(
                R.plurals.task_editor_restore_x_backups_selected_desc,
                state.sourceBackups.size
            )
            ui.setupbar.apply {
                buttonPositiveSecondary.setGone(state.sourceBackups.isEmpty())
                buttonPositiveSecondary.clicksDebounced().subscribe { vdc.continueWithSources() }
            }

            ui.countContainer.setInvisible(state.isWorking)
            ui.loadingOverlayCounts.setInvisible(!state.isWorking)
        }

        vdc.backupsState.observe2(this) { state ->
            adapter.update(state.backups)

            ui.backupList.setInvisible(state.isWorking)
            ui.backupListOverlay.setInvisible(!state.isWorking)

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
            swipeTool.attach(ui.backupList)

            vdc.finishEvent.observe2(this) {
                findNavController().popBackStack(R.id.mainFragment, false)
            }

            super.onViewCreated(view, savedInstanceState)
        }

    }
}
