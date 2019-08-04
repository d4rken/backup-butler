package eu.darken.bb.tasks.ui.editor.sources

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcsAssisted
import eu.darken.bb.tasks.core.getTaskId
import javax.inject.Inject


class SourcesFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: SourcesFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as SourcesFragmentVDC.Factory
        factory.create(handle, arguments!!.getTaskId()!!)
    })

    @BindView(R.id.add_source) lateinit var addSource: Button

    init {
        layoutRes = R.layout.task_editor_sources_fragment
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addSource.clicksDebounced().subscribe { vdc.createSource() }
    }
}
