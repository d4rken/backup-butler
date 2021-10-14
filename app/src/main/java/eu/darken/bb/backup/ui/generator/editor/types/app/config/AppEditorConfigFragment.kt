package eu.darken.bb.backup.ui.generator.editor.types.app.config

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.jakewharton.rxbinding4.widget.editorActions
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.editor.types.app.preview.PreviewMode
import eu.darken.bb.common.*
import eu.darken.bb.common.navigation.doNavigate
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
            invalidateOptionsMenu()
        }
        ui.apply {
            coreSettingsAutoinclude.setSwitchListener { _, b -> vdc.onUpdateAutoInclude(b) }
            coreSettingsIncludeuser.setSwitchListener { _, b -> vdc.onUpdateIncludeUser(b) }
            coreSettingsIncludesystem.setSwitchListener { _, b -> vdc.onUpdateIncludeSystem(b) }
        }

        ui.apply {
            coreSettingsIncludedPackages.clicksDebounced().subscribe { navigatePreview(PreviewMode.INCLUDE) }
            coreSettingsExcludedPackages.clicksDebounced().subscribe { navigatePreview(PreviewMode.EXCLUDE) }
        }

        ui.apply {
            optionsBackupapk.setSwitchListener { _, b -> vdc.onUpdateBackupApk(b) }
            optionsBackupdata.setSwitchListener { _, b -> vdc.onUpdateBackupData(b) }
            optionsBackupcache.setSwitchListener { _, b -> vdc.onUpdateBackupCache(b) }
        }

        ui.apply {
            nameInput.userTextChangeEvents().subscribe { vdc.updateLabel(it.text.toString()) }
            nameInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { ui.nameInput.clearFocus() }
        }

        ui.appPreviewAction.setOnClickListener { navigatePreview(PreviewMode.PREVIEW) }
        vdc.matchedPkgsCount.observe2(this, ui) {
            appPreviewInfo.text = resources.getString(
                R.string.app_preview_currently_matching_x_description,
                resources.getCountString(R.plurals.x_items, it)
            )
        }

        vdc.finishEvent.observe2(this) { requireActivity().finish() }

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_generator_editor_app_config, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_create).apply {
            isVisible = allowCreate
            title = getString(if (existing) R.string.general_save_action else R.string.general_create_action)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_create -> {
            vdc.saveConfig()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}
