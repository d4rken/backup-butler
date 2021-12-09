package eu.darken.bb.backup.ui.generator.editor.types.app.config

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.editor.GeneratorEditorFragmentChild
import eu.darken.bb.backup.ui.generator.editor.setGeneratorEditorResult
import eu.darken.bb.backup.ui.generator.editor.types.app.preview.PreviewMode
import eu.darken.bb.common.*
import eu.darken.bb.common.flow.launchInView
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.navigation.popBackStack
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.databinding.GeneratorEditorAppConfigFragmentBinding
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.widget.editorActionEvents

@AndroidEntryPoint
class AppEditorConfigFragment : SmartFragment(R.layout.generator_editor_app_config_fragment),
    GeneratorEditorFragmentChild {

    val navArgs by navArgs<AppEditorConfigFragmentArgs>()

    private val vdc: AppEditorConfigFragmentVDC by viewModels()
    private val ui: GeneratorEditorAppConfigFragmentBinding by viewBinding()
    private var allowCreate: Boolean = false
    private var existing: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            toolbar.apply {
                setNavigationOnClickListener { popBackStack() }
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_create -> {
                            vdc.saveConfig()
                            true
                        }
                        else -> super.onOptionsItemSelected(it)
                    }
                }
            }

            coreSettingsAutoinclude.setSwitchListener { _, b -> vdc.onUpdateAutoInclude(b) }
            coreSettingsIncludeuser.setSwitchListener { _, b -> vdc.onUpdateIncludeUser(b) }
            coreSettingsIncludesystem.setSwitchListener { _, b -> vdc.onUpdateIncludeSystem(b) }

            coreSettingsIncludedPackages.setOnClickListener { navigatePreview(PreviewMode.INCLUDE) }
            coreSettingsExcludedPackages.setOnClickListener { navigatePreview(PreviewMode.EXCLUDE) }

            optionsBackupapk.setSwitchListener { _, b -> vdc.onUpdateBackupApk(b) }
            optionsBackupdata.setSwitchListener { _, b -> vdc.onUpdateBackupData(b) }
            optionsBackupcache.setSwitchListener { _, b -> vdc.onUpdateBackupCache(b) }


            nameInput
                .userTextChangeEvents()
                .onEach { vdc.updateLabel(it.text.toString()) }
                .launchInView(this@AppEditorConfigFragment)
            nameInput
                .editorActionEvents { it.keyEvent?.keyCode == KeyEvent.KEYCODE_ENTER }
                .onEach { ui.nameInput.clearFocus() }
                .launchInView(this@AppEditorConfigFragment)
        }

        ui.appPreviewAction.setOnClickListener { navigatePreview(PreviewMode.PREVIEW) }

        vdc.state.observe2(this, ui) { state ->
            nameInput.setTextIfDifferentAndNotFocused(state.label)

            coreSettingsAutoinclude.isChecked = state.autoInclude
            coreSettingsIncludeuser.isChecked = state.includeUserApps
            coreSettingsIncludesystem.isChecked = state.includeSystemApps

            coreSettingsIncludedPackages.description =
                resources.getCountString(R.plurals.x_items, state.packagesIncluded.size)
            coreSettingsExcludedPackages.description =
                resources.getCountString(R.plurals.x_items, state.packagesExcluded.size)

            optionsBackupapk.isChecked = state.backupApk
            optionsBackupdata.isChecked = state.backupData
            optionsBackupcache.isChecked = state.backupCache

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsProgress.setGone(!state.isWorking)
            optionsContainer.setInvisible(state.isWorking)
            optionsProgress.setGone(!state.isWorking)

            allowCreate = state.isValid
            existing = state.isExisting

            toolbar.menu.apply {
                findItem(R.id.action_create).apply {
                    isVisible = allowCreate
                    title = getString(if (existing) R.string.general_save_action else R.string.general_create_action)
                }
            }
        }

        vdc.matchedPkgsCount.observe2(this, ui) {
            appPreviewInfo.text = resources.getString(
                R.string.app_preview_currently_matching_x_description,
                resources.getCountString(R.plurals.x_items, it)
            )
        }

        vdc.finishEvent.observe2(this) {
            setGeneratorEditorResult(it)
            popBackStack()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun navigatePreview(mode: PreviewMode) {
        doNavigate(
            AppEditorConfigFragmentDirections.actionAppEditorConfigFragmentToAppEditorPreviewFragment(
                generatorId = navArgs.generatorId,
                previewMode = mode,
            )
        )
    }
}
