package eu.darken.bb.task.ui.editor.common.intro

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import butterknife.BindView
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferent
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.SetupBarView
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.task.ui.editor.backup.sources.SourcesFragmentArgs
import javax.inject.Inject

@AndroidEntryPoint
class IntroFragment : SmartFragment() {

    val navArgs by navArgs<IntroFragmentArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: IntroFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as IntroFragmentVDC.Factory
        factory.create(handle, navArgs.taskId)
    })

    @BindView(R.id.name_input) lateinit var nameInput: EditText
    @BindView(R.id.setupbar) lateinit var setupBar: SetupBarView

    init {
        layoutRes = R.layout.task_editor_intro_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe2(this) {
            nameInput.setTextIfDifferent(it.label)
        }

        nameInput.userTextChangeEvents().subscribe { vdc.updateTaskName(it.text.toString()) }

        setupBar.buttonPositiveSecondary.clicksDebounced().subscribe {
            findNavController().navigate(
                R.id.nav_action_next,
                SourcesFragmentArgs(taskId = navArgs.taskId).toBundle()
            )
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
