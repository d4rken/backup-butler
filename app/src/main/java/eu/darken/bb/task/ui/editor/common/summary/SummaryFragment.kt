package eu.darken.bb.task.ui.editor.common.summary

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorSummaryFragmentBinding
import eu.darken.bb.task.core.Task

@AndroidEntryPoint
class SummaryFragment : Smart2Fragment(R.layout.task_editor_summary_fragment) {

    override val ui: TaskEditorSummaryFragmentBinding by viewBinding()
    override val vdc: SummaryFragmentVDC by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            toolbar.setupWithNavController(findNavController())

            setupbar.apply {
                buttonPositivePrimary.apply {
                    setOnClickListener { vdc.runTask() }
                    isGone = true
                }
                buttonPositiveSecondary.apply {
                    setOnClickListener { vdc.saveTask() }
                    isGone = true
                }
            }
        }

        vdc.state.observe2(ui) { state ->
            setupbar.isEnabled = state !is SummaryFragmentVDC.State.Busy

            if (state is SummaryFragmentVDC.State.Summary) {
                setupbar.buttonPositiveSecondary.isGone = state.isSingleUse
                setupbar.buttonPositivePrimary.isGone = false

                toolbar.title = when (state.taskType) {
                    Task.Type.BACKUP_SIMPLE -> getString(R.string.task_editor_backup_new_label)
                    Task.Type.RESTORE_SIMPLE -> getString(R.string.task_editor_restore_new_label)
                }
                taskName.text = state.label
                taskDescription.text = state.description.get(requireContext())
            } else {
                toolbar.title = getString(R.string.progress_loading_label)
                taskName.text = getString(R.string.progress_loading_label)
                taskDescription.text = getString(R.string.progress_loading_label)
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
