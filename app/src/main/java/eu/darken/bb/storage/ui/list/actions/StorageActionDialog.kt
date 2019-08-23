package eu.darken.bb.storage.ui.list.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.setupDefaults
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.getStorageId
import eu.darken.bb.storage.core.putStorageId
import eu.darken.bb.task.core.TaskRepo
import javax.inject.Inject

class StorageActionDialog : BottomSheetDialogFragment(), AutoInject {
    private var unbinder: Unbinder? = null

    @Inject lateinit var taskRepo: TaskRepo
    @Inject lateinit var actionsAdapter: ActionsAdapter

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: StorageActionDialogVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as StorageActionDialogVDC.Factory
        factory.create(handle, arguments!!.getStorageId()!!)
    })

    @BindView(R.id.type_label) lateinit var typeLabel: TextView
    @BindView(R.id.storage_label) lateinit var storageLabel: TextView
    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.loading_overlay) lateinit var loadingOverlay: LoadingOverlayView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.storage_list_action_dialog, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(actionsAdapter)

        actionsAdapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int ->
            vdc.storageAction(actionsAdapter.data[i])
        })

        vdc.state.observe(this, Observer { state ->
            if (state.storageInfo != null) {
                storageLabel.text = state.storageInfo.config?.label
                        ?: getString(R.string.label_unknown)
                typeLabel.text = getString(state.storageInfo.ref.storageType.labelRes)
            }

            actionsAdapter.update(state.allowedActions)

            recyclerView.visibility = if (state.isWorking) View.INVISIBLE else View.VISIBLE
            loadingOverlay.visibility = if (state.isWorking) View.VISIBLE else View.INVISIBLE
        })

        vdc.finishedEvent.observe2(this) { dismissAllowingStateLoss() }

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(storageId: Storage.Id): BottomSheetDialogFragment = StorageActionDialog().apply {
            arguments = Bundle().putStorageId(storageId)
        }
    }
}