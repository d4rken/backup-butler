package eu.darken.bb.backup.ui.generator.editor.types.app.config

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jakewharton.rxbinding4.widget.editorActions
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.editor.types.app.preview.AppEditorPreviewFragmentArgs
import eu.darken.bb.backup.ui.generator.editor.types.app.preview.PreviewMode
import eu.darken.bb.common.*
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.databinding.GeneratorEditorAppConfigFragmentBinding

@AndroidEntryPoint
class AppEditorConfigFragment : SmartFragment(R.layout.generator_editor_app_config_fragment) {

    val navArgs by navArgs<AppEditorConfigFragmentArgs>()

    private val vdc: AppEditorConfigFragmentVDC by viewModels()
    private val ui: GeneratorEditorAppConfigFragmentBinding by viewBinding()
    private var allowCreate: Boolean = false
    private var existing: Boolean = false

    init {
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe2(this) { state ->
            ui.nameInput.setTextIfDifferentAndNotFocused(state.label)

            ui.coreSettingsAutoinclude.isChecked = state.autoInclude
            ui.coreSettingsIncludeuser.isChecked = state.includeUserApps
            ui.coreSettingsIncludesystem.isChecked = state.includeSystemApps

            ui.coreSettingsIncludedPackages.description =
                resources.getCountString(R.plurals.x_items, state.packagesIncluded.size)
            ui.coreSettingsExcludedPackages.description =
                resources.getCountString(R.plurals.x_items, state.packagesExcluded.size)

            ui.optionsBackupapk.isChecked = state.backupApk
            ui.optionsBackupdata.isChecked = state.backupData
            ui.optionsBackupcache.isChecked = state.backupCache

            ui.coreSettingsContainer.setInvisible(state.isWorking)
            ui.coreSettingsProgress.setGone(!state.isWorking)
            ui.optionsContainer.setInvisible(state.isWorking)
            ui.optionsProgress.setGone(!state.isWorking)

            allowCreate = state.isValid
            existing = state.isExisting
            invalidateOptionsMenu()
        }

        ui.coreSettingsAutoinclude.setSwitchListener { _, b -> vdc.onUpdateAutoInclude(b) }
        ui.coreSettingsIncludeuser.setSwitchListener { _, b -> vdc.onUpdateIncludeUser(b) }
        ui.coreSettingsIncludesystem.setSwitchListener { _, b -> vdc.onUpdateIncludeSystem(b) }

        ui.coreSettingsIncludedPackages.clicksDebounced().subscribe { navigatePreview(PreviewMode.INCLUDE) }
        ui.coreSettingsExcludedPackages.clicksDebounced().subscribe { navigatePreview(PreviewMode.EXCLUDE) }

        ui.optionsBackupapk.setSwitchListener { _, b -> vdc.onUpdateBackupApk(b) }
        ui.optionsBackupdata.setSwitchListener { _, b -> vdc.onUpdateBackupData(b) }
        ui.optionsBackupcache.setSwitchListener { _, b -> vdc.onUpdateBackupCache(b) }

        ui.nameInput.userTextChangeEvents().subscribe { vdc.updateLabel(it.text.toString()) }
        ui.nameInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { ui.nameInput.clearFocus() }

        vdc.finishEvent.observe2(this) { requireActivity().finish() }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun navigatePreview(mode: PreviewMode) {
        val args = AppEditorPreviewFragmentArgs(
            generatorId = navArgs.generatorId,
            previewMode = mode
        )
        findNavController().navigate(R.id.action_appEditorConfigFragment_to_appEditorPreviewFragment, args.toBundle())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_generator_editor_app_config, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_create).isVisible = allowCreate
        menu.findItem(R.id.action_create).title =
            getString(if (existing) R.string.general_save_action else R.string.general_create_action)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_create -> {
            vdc.saveConfig()
            true
        }
        R.id.action_preview -> {
            navigatePreview(PreviewMode.PREVIEW)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}
