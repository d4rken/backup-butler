package eu.darken.bb.task.ui.editor.restore.config

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.jakewharton.rxbinding3.view.longClicks
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.file.picker.APathPicker
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.toastError
import eu.darken.bb.common.ui.*
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.restore.config.app.AppConfigUIWrap
import eu.darken.bb.task.ui.editor.restore.config.files.FilesConfigUIWrap
import javax.inject.Inject


class RestoreConfigFragment : SmartFragment(), AutoInject {

    val navArgs by navArgs<RestoreConfigFragmentArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: RestoreConfigFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as RestoreConfigFragmentVDC.Factory
        factory.create(handle, navArgs.taskId)
    })

    @Inject lateinit var adapter: RestoreConfigAdapter

    @BindView(R.id.loading_overlay_counts) lateinit var loadingOverlayCounts: LoadingOverlayView
    @BindView(R.id.count_container) lateinit var countContainer: ViewGroup
    @BindView(R.id.count_backups) lateinit var countTypes: TextView
    @BindView(R.id.count_custom_configs) lateinit var countCustomConfigs: TextView
    @BindView(R.id.count_config_issues) lateinit var countConfigIssues: TextView

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.loading_overlay_backuplist) lateinit var loadingOverlayBackupList: LoadingOverlayView
    @BindView(R.id.setupbar) lateinit var setupBar: SetupBarView

    init {
        layoutRes = R.layout.task_editor_restore_configs_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(adapter, dividers = false)

        vdc.summaryState.observe2(this) { state ->
            countTypes.setTextQuantity(R.plurals.task_editor_restore_x_backups_types_desc, state.backupTypes.size)

            countCustomConfigs.setTextQuantity(R.plurals.task_editor_restore_x_items_custom_config_desc, state.customConfigCount)
            countCustomConfigs.setGone(state.customConfigCount == 0)

            countConfigIssues.setTextQuantity(R.plurals.task_editor_restore_x_configs_have_issues, state.configsWithIssues)
            countConfigIssues.setGone(state.configsWithIssues == 0)

            setupBar.buttonPositivePrimary.setGone(state.configsWithIssues != 0)

            countContainer.setInvisible(state.isLoading)
            loadingOverlayCounts.setInvisible(!state.isLoading)
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

            recyclerView.setInvisible(state.isWorking || state.isLoading)
            loadingOverlayBackupList.setInvisible(!state.isWorking && !state.isLoading)

            setupBar.isEnabled = !state.isWorking && !state.isLoading
        }

        vdc.openPickerEvent.observe2(this) {
            val intent = APathPicker.createIntent(requireContext(), it)
            startActivityForResult(intent, 13)
        }

        vdc.errorEvent.observe2(this) { toastError(it) }

        vdc.finishEvent.observe2(this) { finishActivity() }

        setupBar.buttonPositivePrimary.clicksDebounced().subscribe { vdc.runTask() }
        setupBar.buttonPositivePrimary.longClicks().subscribe { vdc.saveTask() }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            13 -> APathPicker.checkForNonNeutralResult(this, resultCode, data) { vdc.updatePath(it) }
            else -> throw IllegalArgumentException("Unknown activity result: code=$requestCode, resultCode=$resultCode, data=$data")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
