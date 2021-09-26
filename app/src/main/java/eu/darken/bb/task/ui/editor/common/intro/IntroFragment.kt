package eu.darken.bb.task.ui.editor.common.intro

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferent
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorIntroFragmentBinding
import eu.darken.bb.task.ui.editor.backup.sources.SourcesFragmentArgs

@AndroidEntryPoint
class IntroFragment : SmartFragment(R.layout.task_editor_intro_fragment) {

    val navArgs by navArgs<IntroFragmentArgs>()
    private val ui: TaskEditorIntroFragmentBinding by viewBinding()
    private val vdc: IntroFragmentVDC by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe2(this, ui) {
            nameInput.setTextIfDifferent(it.label)
        }

        ui.nameInput.userTextChangeEvents().subscribe { vdc.updateTaskName(it.text.toString()) }

        ui.setupbar.buttonPositiveSecondary.clicksDebounced().subscribe {
            findNavController().navigate(
                R.id.nav_action_next,
                SourcesFragmentArgs(taskId = navArgs.taskId).toBundle()
            )
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
