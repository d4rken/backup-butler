package eu.darken.bb.storage.ui.viewer.content.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import eu.darken.bb.R
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.setupDefaults
import eu.darken.bb.common.vdcsAssisted
import eu.darken.bb.storage.core.*
import eu.darken.bb.task.core.BackupTaskRepo
import javax.inject.Inject

class ContentActionDialog : BottomSheetDialogFragment(), AutoInject {
    private var unbinder: Unbinder? = null

    @Inject lateinit var taskRepo: BackupTaskRepo
    @Inject lateinit var actionsAdapter: ActionsAdapter

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: ContentActionDialogVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as ContentActionDialogVDC.Factory
        factory.create(handle, arguments!!.getStorageId()!!, arguments!!.getBackupSpecId()!!)
    })

    @BindView(R.id.type_label) lateinit var typeLabel: TextView
    @BindView(R.id.storage_label) lateinit var storageLabel: TextView
    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.progress_circular) lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.storage_contentlist_action_dialog, container, false)
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
                storageLabel.text = state.storageInfo.config?.label ?: getString(R.string.label_unknown)
                typeLabel.text = getString(state.storageInfo.ref.storageType.labelRes)
            }

            actionsAdapter.update(state.allowedActions)

            recyclerView.visibility = if (state.loading) View.INVISIBLE else View.VISIBLE
            progressBar.visibility = if (state.loading) View.VISIBLE else View.INVISIBLE
            if (state.finished) dismissAllowingStateLoss()
        })
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(storageId: Storage.Id, backupSpecId: BackupSpec.Id): BottomSheetDialogFragment = ContentActionDialog().apply {
            arguments = Bundle().apply {
                putStorageId(storageId)
                putBackupSpecId(backupSpecId)
            }
        }
    }
}