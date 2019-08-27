package eu.darken.bb.task.ui.editor.backup.intro

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.lifecycle.Observer
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.task.core.getTaskId
import javax.inject.Inject


class IntroFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: IntroFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as IntroFragmentVDC.Factory
        factory.create(handle, arguments!!.getTaskId()!!)
    })

    @BindView(R.id.name_input) lateinit var nameInput: EditText

    init {
        layoutRes = R.layout.task_editor_intro_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().setSubtitle(R.string.label_introduction)

        vdc.state.observe(this, Observer {
            if (nameInput.text.toString() != it.taskName) nameInput.setText(it.taskName)
        })
        nameInput.userTextChangeEvents().subscribe { vdc.updateTaskName(it.text.toString()) }

        super.onViewCreated(view, savedInstanceState)
    }
}
