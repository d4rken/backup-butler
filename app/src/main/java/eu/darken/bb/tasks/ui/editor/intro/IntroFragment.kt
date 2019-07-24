package eu.darken.bb.tasks.ui.editor.intro

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.Observer
import butterknife.BindView
import butterknife.ButterKnife
import com.jakewharton.rxbinding3.widget.textChanges
import dagger.android.support.AndroidSupportInjection
import eu.darken.bb.R
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcsAssisted
import eu.darken.bb.tasks.core.getTaskId

import javax.inject.Inject


class IntroFragment : SmartFragment() {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: IntroFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as IntroFragmentVDC.Factory
        factory.create(handle, arguments!!.getTaskId()!!)
    })

    @BindView(R.id.name_input) lateinit var nameInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.newtask_intro_fragment, container, false)
        addUnbinder(ButterKnife.bind(this, layout))
        return layout
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        vdc.state.observe(this, Observer {
            if (nameInput.text.toString() != it.taskName) nameInput.setText(it.taskName)
        })
        nameInput.textChanges().skipInitialValue().subscribe { vdc.updateTaskName(it) }

        super.onViewCreated(view, savedInstanceState)
    }
}
