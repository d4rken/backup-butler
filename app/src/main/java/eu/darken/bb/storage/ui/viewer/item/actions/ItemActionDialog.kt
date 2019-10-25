package eu.darken.bb.storage.ui.viewer.item.actions

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
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.getBackupSpecId
import eu.darken.bb.backup.core.putBackupSpecId
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.toastError
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.getStorageId
import eu.darken.bb.storage.core.putStorageId
import eu.darken.bb.storage.ui.viewer.StorageViewerActivity
import eu.darken.bb.task.core.TaskRepo
import javax.inject.Inject

class ItemActionDialog : BottomSheetDialogFragment(), AutoInject {
    private var unbinder: Unbinder? = null

    @Inject lateinit var taskRepo: TaskRepo
    @Inject lateinit var actionsAdapter: ActionsAdapter

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: ItemActionDialogVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as ItemActionDialogVDC.Factory
        factory.create(handle, arguments!!.getStorageId()!!, arguments!!.getBackupSpecId()!!)
    })

    private val activityVdc by lazy { (requireActivity() as StorageViewerActivity).vdc }

    @BindView(R.id.type_label) lateinit var typeLabel: TextView
    @BindView(R.id.storage_label) lateinit var storageLabel: TextView

    @BindView(R.id.info_container) lateinit var infoContainer: ViewGroup
    @BindView(R.id.info_container_loading) lateinit var infoContainerLoading: View

    @BindView(R.id.actionlist) lateinit var actionList: RecyclerView
    @BindView(R.id.actionlist_loading) lateinit var actionListLoading: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.storage_viewer_contentlist_action_dialog, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        actionList.setupDefaults(actionsAdapter)

        actionsAdapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int ->
            vdc.storageAction(actionsAdapter.data[i])
        })

        vdc.state.observe(this, Observer { state ->
            if (state.info != null) {
                typeLabel.text = getString(state.info.backupSpec.backupType.labelRes)
                storageLabel.text = state.info.backupSpec.getLabel(requireContext())
            } else {
                typeLabel.setText(R.string.label_unknown)
                storageLabel.setText(R.string.progress_loading_label)
            }
            infoContainerLoading.setInvisible(state.info != null)

            actionsAdapter.update(state.allowedActions)

            actionList.setInvisible(state.isWorking || state.allowedActions == null)
            actionListLoading.setInvisible(!state.isWorking && state.allowedActions != null)
        })

        vdc.pageEvent.observe(this, Observer { pageData ->
            activityVdc.goTo(pageData)
            dismiss()
        })

        vdc.finishedEvent.observe(this, Observer { dismissAllowingStateLoss() })

        vdc.errorEvents.observe2(this) { toastError(it) }

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(storageId: Storage.Id, backupSpecId: BackupSpec.Id): BottomSheetDialogFragment = ItemActionDialog().apply {
            arguments = Bundle().apply {
                putStorageId(storageId)
                putBackupSpecId(backupSpecId)
            }
        }
    }
}