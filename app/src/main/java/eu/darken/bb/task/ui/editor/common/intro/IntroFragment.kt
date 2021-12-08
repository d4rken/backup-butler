package eu.darken.bb.task.ui.editor.common.intro

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferent
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorIntroFragmentBinding

@AndroidEntryPoint
class IntroFragment : Smart2Fragment(R.layout.task_editor_intro_fragment) {

    val navArgs by navArgs<IntroFragmentArgs>()
    override val ui: TaskEditorIntroFragmentBinding by viewBinding()
    override val vdc: IntroFragmentVDC by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            toolbar.setupWithNavController(findNavController())
            toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)

            nameInput.userTextChangeEvents().subscribe { vdc.updateTaskName(it.text.toString()) }

            setupbar.buttonPositiveSecondary.clicksDebounced().subscribe { vdc.onContinue() }
        }

        vdc.state.observe2(this, ui) {
            nameInput.setTextIfDifferent(it.label)
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
