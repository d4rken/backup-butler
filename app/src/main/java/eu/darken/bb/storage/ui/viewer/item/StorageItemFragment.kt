package eu.darken.bb.storage.ui.viewer.item

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.toastError
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.core.getStorageId
import eu.darken.bb.storage.ui.viewer.item.actions.ItemActionDialog
import javax.inject.Inject


class StorageItemFragment : SmartFragment(), AutoInject, HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: StorageItemFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as StorageItemFragmentVDC.Factory
        factory.create(handle, arguments!!.getStorageId()!!)
    })

    @Inject lateinit var adapter: StorageItemAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.loading_overlay) lateinit var loadingOverlay: LoadingOverlayView

    private var showOptionDeleteAll = false

    init {
        layoutRes = R.layout.storage_viewer_itemlist_fragment
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        requireActivityActionBar().setDisplayHomeAsUpEnabled(true)

        recyclerView.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.viewContent(adapter.data[i]) })

        vdc.state.observe2(this) { state ->
            requireActivityActionBar().title = state.storageLabel
            if (state.storageType != null) {
                requireActivityActionBar().setSubtitle(state.storageType.labelRes)
            }

            adapter.update(state.specInfos)

            recyclerView.setInvisible(state.isLoading)
            loadingOverlay.setInvisible(!state.isLoading)

            showOptionDeleteAll = state.allowDeleteAll && !state.isLoading
            invalidateOptionsMenu()
        }
        vdc.deletionState.observe2(this) { deletionState ->
            if (deletionState.backupSpec != null) {
                loadingOverlay.setPrimaryText(getString(R.string.progress_deleting_x, deletionState.backupSpec.getLabel(requireContext())))
            } else {
                loadingOverlay.setPrimaryText(null)
            }

        }
        vdc.contentActionEvent.observe2(this) {
            val bs = ItemActionDialog.newInstance(it.storageId, it.backupSpecId)
            bs.show(childFragmentManager, "${it.storageId}-${it.backupSpecId}")
        }

        vdc.finishEvent.observe(this, Observer { activity?.finish() })

        vdc.errorEvents.observe2(this) { toastError(it) }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_storage_viewer_item_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_delete_all).isVisible = showOptionDeleteAll
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            requireActivity().finish()
            true
        }
        R.id.action_delete_all -> {
            vdc.deleteAll()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
