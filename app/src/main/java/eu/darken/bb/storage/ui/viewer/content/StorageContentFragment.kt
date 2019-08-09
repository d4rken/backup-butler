package eu.darken.bb.storage.ui.viewer.content

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.setupDefaults
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.common.vdcsAssisted
import eu.darken.bb.storage.core.getStorageId
import eu.darken.bb.storage.ui.viewer.content.actions.ContentActionDialog
import javax.inject.Inject


class StorageContentFragment : SmartFragment(), AutoInject, HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: StorageContentFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as StorageContentFragmentVDC.Factory
        factory.create(handle, arguments!!.getStorageId()!!)
    })
    @Inject lateinit var adapter: ContentAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    init {
        layoutRes = R.layout.storage_contentlist_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.viewContent(adapter.data[i]) })

        vdc.state.observe(this, Observer {
            adapter.update(it.contents)

            if (it.error != null) {
                Toast.makeText(requireContext(), it.error.tryLocalizedErrorMessage(requireContext()), Toast.LENGTH_LONG).show()
            }
        })

        vdc.contentActionEvent.observe(this, Observer {
            val bs = ContentActionDialog.newInstance(it.storageId, it.backupSpecId)
            bs.show(childFragmentManager, "${it.storageId}-${it.backupSpecId}")
        })

        vdc.finishEvent.observe(this, Observer { activity?.finish() })

        super.onViewCreated(view, savedInstanceState)
    }
}
