package eu.darken.bb.task.ui.editor.restore.config

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.jakewharton.rxbinding4.view.longClicks
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.files.ui.picker.PathPickerActivityContract
import eu.darken.bb.common.lists.differ.update
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.toastError
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.ui.setTextQuantity
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorRestoreConfigsFragmentBinding
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.restore.config.app.AppConfigUIWrap
import eu.darken.bb.task.ui.editor.restore.config.files.FilesConfigUIWrap
import javax.inject.Inject

@AndroidEntryPoint
class RestoreConfigFragment : SmartFragment(R.layout.task_editor_restore_configs_fragment) {

    private val vdc: RestoreConfigFragmentVDC by viewModels()
    private val ui: TaskEditorRestoreConfigsFragmentBinding by viewBinding()

    @Inject lateinit var adapter: RestoreConfigAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            configList.setupDefaults(adapter, dividers = false)
            toolbar.setupWithNavController(findNavController())

            setupbar.apply {
                buttonPositivePrimary.clicksDebounced().subscribe { vdc.runTask() }
                buttonPositivePrimary.longClicks().subscribe { vdc.saveTask() }
            }
        }

        vdc.summaryState.observe2(this, ui) { state ->
            countBackups.setTextQuantity(R.plurals.task_editor_restore_x_backups_types_desc, state.backupTypes.size)

            countCustomConfigs.setTextQuantity(
                R.plurals.task_editor_restore_x_items_custom_config_desc,
                state.customConfigCount
            )
            countCustomConfigs.setGone(state.customConfigCount == 0)

            countConfigIssues.setTextQuantity(
                R.plurals.task_editor_restore_x_configs_have_issues,
                state.configsWithIssues
            )
            countConfigIssues.setGone(state.configsWithIssues == 0)

            setupbar.buttonPositivePrimary.setGone(state.configsWithIssues != 0)

            countContainer.setInvisible(state.isLoading)
            loadingOverlayCounts.setInvisible(!state.isLoading)
        }

        vdc.configState.observe2(this, ui) { state ->
            val defaultItems = state.defaultConfigs
                .map {
                    when (it) {
                        is SimpleRestoreTaskEditor.AppsConfigWrap -> AppConfigUIWrap(it) { config, id ->
                            vdc.updateConfig(config, id)
                        }
                        is SimpleRestoreTaskEditor.FilesConfigWrap -> FilesConfigUIWrap(
                            it,
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

            configList.setInvisible(state.isWorking || state.isLoading)
            configListOverlay.setInvisible(!state.isWorking && !state.isLoading)

            setupbar.isEnabled = !state.isWorking && !state.isLoading
        }
        val pickerLauncher = registerForActivityResult(PathPickerActivityContract()) {
            if (it != null) vdc.updatePath(it)
            else Toast.makeText(requireContext(), R.string.general_error_empty_result_msg, Toast.LENGTH_SHORT).show()
        }
        vdc.openPickerEvent.observe2(this) { pickerLauncher.launch(it) }

        vdc.errorEvent.observe2(this) { toastError(it) }

        vdc.finishEvent.observe2(this) {
            findNavController().popBackStack(R.id.mainFragment, false)
        }

        super.onViewCreated(view, savedInstanceState)
    }

}
