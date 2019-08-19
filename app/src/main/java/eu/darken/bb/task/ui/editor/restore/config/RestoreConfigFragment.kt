package eu.darken.bb.task.ui.editor.restore.config

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.setupDefaults
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.task.core.getTaskId
import javax.inject.Inject


class RestoreConfigFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: RestoreConfigFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as RestoreConfigFragmentVDC.Factory
        factory.create(handle, arguments!!.getTaskId()!!)
    })

    @Inject lateinit var adapter: RestoreConfigAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.loading_overlay) lateinit var loadingOverlay: LoadingOverlayView

    init {
        layoutRes = R.layout.task_editor_restore_configs_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(adapter, dividers = false)
        requireActivityActionBar().setSubtitle(R.string.restore_options_label)

        vdc.state.observe(this, Observer { state ->

            loadingOverlay.setInvisible(!state.isLoading)
            recyclerView.setInvisible(state.isLoading)
            adapter.update(state.restoreConfigs.map { Pair(it, { config: Restore.Config -> vdc.updateConfig(config) }) })
        })

        super.onViewCreated(view, savedInstanceState)
    }

}
