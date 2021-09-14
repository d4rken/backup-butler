package eu.darken.bb.backup.ui.generator.editor.types.app.config

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import butterknife.BindView
import com.jakewharton.rxbinding4.widget.editorActions
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.editor.types.app.preview.AppEditorPreviewFragmentArgs
import eu.darken.bb.backup.ui.generator.editor.types.app.preview.PreviewMode
import eu.darken.bb.common.getCountString
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferentAndNotFocused
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.PreferenceView
import eu.darken.bb.common.ui.SwitchPreferenceView
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.userTextChangeEvents

@AndroidEntryPoint
class AppEditorConfigFragment : SmartFragment() {

    val navArgs by navArgs<AppEditorConfigFragmentArgs>()

    private val vdc: AppEditorConfigFragmentVDC by viewModels()

    @BindView(R.id.name_input) lateinit var labelInput: EditText
    @BindView(R.id.core_settings_container) lateinit var coreSettingsContainer: ViewGroup
    @BindView(R.id.core_settings_progress) lateinit var coreSettingsProgress: View

    @BindView(R.id.options_container) lateinit var optionsContainer: ViewGroup
    @BindView(R.id.options_progress) lateinit var optionsProgress: View

    @BindView(R.id.core_settings_autoinclude) lateinit var optionAutoInclude: SwitchPreferenceView
    @BindView(R.id.core_settings_includeuser) lateinit var optionIncludeUser: SwitchPreferenceView
    @BindView(R.id.core_settings_includesystem) lateinit var optionIncludeSystem: SwitchPreferenceView
    @BindView(R.id.core_settings_included_packages) lateinit var optionIncludedApps: PreferenceView
    @BindView(R.id.core_settings_excluded_packages) lateinit var optionExcludedAPps: PreferenceView

    @BindView(R.id.options_backupapk) lateinit var optionBackupApk: SwitchPreferenceView
    @BindView(R.id.options_backupdata) lateinit var optionBackupData: SwitchPreferenceView
    @BindView(R.id.options_backupcache) lateinit var optionBackupCache: SwitchPreferenceView

    private var allowCreate: Boolean = false
    private var existing: Boolean = false

    init {
        layoutRes = R.layout.generator_editor_app_config_fragment
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe2(this) { state ->
            labelInput.setTextIfDifferentAndNotFocused(state.label)

            optionAutoInclude.isChecked = state.autoInclude
            optionIncludeUser.isChecked = state.includeUserApps
            optionIncludeSystem.isChecked = state.includeSystemApps

            optionIncludedApps.description = resources.getCountString(R.plurals.x_items, state.packagesIncluded.size)
            optionExcludedAPps.description = resources.getCountString(R.plurals.x_items, state.packagesExcluded.size)

            optionBackupApk.isChecked = state.backupApk
            optionBackupData.isChecked = state.backupData
            optionBackupCache.isChecked = state.backupCache

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsProgress.setGone(!state.isWorking)
            optionsContainer.setInvisible(state.isWorking)
            optionsProgress.setGone(!state.isWorking)

            allowCreate = state.isValid
            existing = state.isExisting
            invalidateOptionsMenu()
        }

        optionAutoInclude.setSwitchListener { _, b -> vdc.onUpdateAutoInclude(b) }
        optionIncludeUser.setSwitchListener { _, b -> vdc.onUpdateIncludeUser(b) }
        optionIncludeSystem.setSwitchListener { _, b -> vdc.onUpdateIncludeSystem(b) }

        optionIncludedApps.clicksDebounced().subscribe { navigatePreview(PreviewMode.INCLUDE) }
        optionExcludedAPps.clicksDebounced().subscribe { navigatePreview(PreviewMode.EXCLUDE) }

        optionBackupApk.setSwitchListener { _, b -> vdc.onUpdateBackupApk(b) }
        optionBackupData.setSwitchListener { _, b -> vdc.onUpdateBackupData(b) }
        optionBackupCache.setSwitchListener { _, b -> vdc.onUpdateBackupCache(b) }

        labelInput.userTextChangeEvents().subscribe { vdc.updateLabel(it.text.toString()) }
        labelInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { labelInput.clearFocus() }

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
