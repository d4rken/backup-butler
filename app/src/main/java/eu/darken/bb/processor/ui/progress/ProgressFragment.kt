package eu.darken.bb.processor.ui.progress

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.observe2
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.tryTextElseHide
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.ProcessorProgressFragmentBinding

@AndroidEntryPoint
class ProgressFragment : Smart2Fragment(R.layout.processor_progress_fragment) {

    override val vdc: ProgressFragmentVDC by viewModels()
    override val ui: ProcessorProgressFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe2(this, ui) { state ->
            taskLabel.text = when {
                state.taskProgress.primary.get(requireContext()).isNotEmpty() -> state.taskProgress.primary.get(
                    requireContext()
                )
                else -> getString(R.string.progress_loading_label)
            }
            generatorLabel.tryTextElseHide(state.taskProgress.secondary.get(requireContext()))
            backupspecLabel.tryTextElseHide(state.taskProgress.tertiary.get(requireContext()), View.GONE)
            processProgressCounter.tryTextElseHide(state.taskProgress.count.displayValue(requireContext()))

            childProgressContainer.setGone(state.actionProgress == null)
            progressIcon.setGone(state.actionProgress == null)
            progressPrimary.setGone(state.actionProgress == null)
            progressSecondary.setGone(state.actionProgress == null)
            progressBar.setGone(state.actionProgress == null)
            progressCounter.setGone(state.actionProgress == null)
            if (state.actionProgress != null) {
                progressPrimary.tryTextElseHide(state.actionProgress.primary.get(requireContext()))
                progressSecondary.tryTextElseHide(state.actionProgress.secondary.get(requireContext()))
                progressCounter.tryTextElseHide(state.actionProgress.count.displayValue(requireContext()))
                when (state.actionProgress.count) {
                    is Progress.Count.Indeterminate -> {
                        progressBar.visibility = View.VISIBLE
                        progressBar.isIndeterminate = true
                    }
                    is Progress.Count.None -> {
                        progressBar.visibility = View.GONE
                    }
                    else -> {
                        progressBar.visibility = View.VISIBLE
                        progressBar.isIndeterminate = false
                        progressBar.progress = state.actionProgress.count.current.toInt()
                        progressBar.max = state.actionProgress.count.max.toInt()
                    }
                }
            }
        }
        onFinishEvent = {
            activity?.finish()
        }
        super.onViewCreated(view, savedInstanceState)
    }
}
