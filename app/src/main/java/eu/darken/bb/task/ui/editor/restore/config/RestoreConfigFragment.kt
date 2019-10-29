package eu.darken.bb.task.ui.editor.restore.config

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.file.picker.APathPicker
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.toastError
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.ui.setTextQuantity
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.task.core.getTaskId
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import javax.inject.Inject


class RestoreConfigFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: RestoreConfigFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as RestoreConfigFragmentVDC.Factory
        factory.create(handle, arguments!!.getTaskId()!!)
    })

    @Inject lateinit var adapter: RestoreConfigAdapter

    @BindView(R.id.loading_overlay_counts) lateinit var loadingOverlayCounts: LoadingOverlayView
    @BindView(R.id.count_container) lateinit var countContainer: ViewGroup
    @BindView(R.id.count_backups) lateinit var countTypes: TextView
    @BindView(R.id.count_custom_configs) lateinit var countCustomConfigs: TextView

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.loading_overlay_backuplist) lateinit var loadingOverlayBackupList: LoadingOverlayView

    init {
        layoutRes = R.layout.task_editor_restore_configs_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(adapter, dividers = false)
        requireActivityActionBar().setSubtitle(R.string.label_configuration)

        vdc.summaryState.observe2(this) { state ->
            countTypes.setTextQuantity(R.plurals.restore_task_x_backups_types_desc, state.backupTypes.size)
            countCustomConfigs.setTextQuantity(R.plurals.x_items_custom_config_desc, state.customConfigCount)

            countContainer.setInvisible(state.isWorking)
            loadingOverlayCounts.setInvisible(!state.isWorking)
        }

        vdc.configState.observe2(this) { state ->
            val defaultItems = state.defaultConfigs
                    .map {
                        when (it) {
                            is SimpleRestoreTaskEditor.AppsConfigWrap -> AppConfigUIWrap(it) { config, id ->
                                vdc.updateConfig(config, id)
                            }
                            is SimpleRestoreTaskEditor.FilesConfigWrap -> FilesConfigUIWrap(it,
                                    configCallback = { config, id -> vdc.updateConfig(config, id) },
                                    pathAction = null
                            )
                            else -> throw IllegalStateException("Unknown config type: $it")
                        }

                    }
            val customItems = state.customConfigs
                    .map {
                        when (it) {
                            is SimpleRestoreTaskEditor.AppsConfigWrap -> AppConfigUIWrap(it) { config, id ->
                                vdc.updateConfig(config, id)
                            }
                            is SimpleRestoreTaskEditor.FilesConfigWrap -> FilesConfigUIWrap(it,
                                    configCallback = { config, id -> vdc.updateConfig(config, id) },
                                    pathAction = { config, id -> vdc.pathAction(config, id!!) }
                            )
                            else -> throw IllegalStateException("Unknown config type: $it")
                        }

                    }
            val adapterData = defaultItems.plus(customItems)
            adapter.update(adapterData)

            recyclerView.setInvisible(state.isWorking)
            loadingOverlayBackupList.setInvisible(!state.isWorking)
        }

        vdc.openPickerEvent.observe2(this) {
            val intent = APathPicker.createIntent(requireContext(), it)
            startActivityForResult(intent, 13)
        }

        vdc.errorEvent.observe2(this) { toastError(it) }

        super.onViewCreated(view, savedInstanceState)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                13 -> vdc.updatePath(APathPicker.fromActivityResult(data))
                else -> throw IllegalArgumentException("Unknown activity result: code=$requestCode, resultCode=$resultCode, data=$data")
            }
        } else if (requestCode == Activity.RESULT_OK) {
            Toast.makeText(context, R.string.error_empty_result, Toast.LENGTH_SHORT).show()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}
