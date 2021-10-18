package eu.darken.bb.task.ui.editor.common.intro

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferent
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorIntroFragmentBinding

@AndroidEntryPoint
class IntroFragment : SmartFragment(R.layout.task_editor_intro_fragment) {

    val navArgs by navArgs<IntroFragmentArgs>()
    private val ui: TaskEditorIntroFragmentBinding by viewBinding()
    private val vdc: IntroFragmentVDC by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            toolbar.setupWithNavController(findNavController())

            nameInput.userTextChangeEvents().subscribe { vdc.updateTaskName(it.text.toString()) }

            setupbar.buttonPositiveSecondary.clicksDebounced().subscribe { vdc.onContinue() }
        }

        vdc.state.observe2(this, ui) {
            nameInput.setTextIfDifferent(it.label)
        }
        vdc.navEvents.observe2(this) { doNavigate(it) }
        super.onViewCreated(view, savedInstanceState)
    }
}
