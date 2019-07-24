package eu.darken.bb.tasks.ui.editor.sources

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import dagger.android.support.AndroidSupportInjection
import eu.darken.bb.R
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcsAssisted
import eu.darken.bb.tasks.core.getTaskId
import javax.inject.Inject


class SourcesFragment : SmartFragment() {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: SourcesFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as SourcesFragmentVDC.Factory
        factory.create(handle, arguments!!.getTaskId()!!)
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.newtask_sources_fragment, container, false)
        addUnbinder(ButterKnife.bind(this, layout))
        return layout
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
