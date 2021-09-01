package eu.darken.bb.storage.ui.viewer.item

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.snackbar.Snackbar
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
import eu.darken.bb.processor.ui.ProcessorActivity
import eu.darken.bb.storage.ui.viewer.item.actions.ItemActionDialogArgs
import javax.inject.Inject


class StorageItemFragment : SmartFragment(), AutoInject, HasSupportFragmentInjector {

    val navArgs by navArgs<StorageItemFragmentArgs>()

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: StorageItemFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as StorageItemFragmentVDC.Factory
        factory.create(handle, navArgs.storageId)
    })

    @Inject lateinit var adapter: StorageItemAdapter

    @BindView(R.id.storage_item_list) lateinit var recyclerView: RecyclerView
    @BindView(R.id.loading_overlay) lateinit var loadingOverlay: LoadingOverlayView

    private var showOptionDeleteAll = false

    init {
        layoutRes = R.layout.storage_viewer_itemlist_fragment
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.viewContent(adapter.data[i]) })

        vdc.state.observe2(this) { state ->
            requireActivityActionBar().title = state.storageLabel
            if (state.storageType != null) {
                requireActivityActionBar().setSubtitle(state.storageType.labelRes)
            }

            adapter.update(state.specInfos)

            recyclerView.setInvisible(state.isWorking)
            loadingOverlay.setInvisible(!state.isWorking)

            showOptionDeleteAll = state.allowDeleteAll && !state.isWorking
            invalidateOptionsMenu()
        }

        vdc.deletionState.observe2(this) { deletionState ->
            if (deletionState.backupSpec != null) {
                loadingOverlay.setPrimaryText(
                    getString(
                        R.string.progress_deleting_x_label,
                        deletionState.backupSpec.getLabel(requireContext())
                    )
                )
            } else {
                loadingOverlay.setPrimaryText(null)
            }

        }

        vdc.contentActionEvent.observe2(this) {
            val args = ItemActionDialogArgs(storageId = it.storageId, specId = it.backupSpecId)
            findNavController().navigate(R.id.action_storageItemFragment_to_storageItemActionDialog, args.toBundle())
        }

        vdc.finishEvent.observe2(this) { finishActivity() }

        vdc.errorEvents.observe2(this) { toastError(it) }

        var snackbar: Snackbar? = null
        vdc.processorEvent.observe2(this) { isActive ->
            if (isVisible && isActive && snackbar == null) {
                snackbar = Snackbar.make(view, R.string.progress_processing_task_label, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.general_show_action) {
                        startActivity(Intent(requireContext(), ProcessorActivity::class.java))
                    }
                snackbar?.show()
            } else if (!isActive && snackbar != null) {
                snackbar?.dismiss()
                snackbar = null
            }
        }
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
        R.id.action_delete_all -> {
            vdc.deleteAll()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
