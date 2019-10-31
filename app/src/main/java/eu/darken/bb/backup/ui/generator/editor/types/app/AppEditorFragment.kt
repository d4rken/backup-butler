package eu.darken.bb.backup.ui.generator.editor.types.app

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.Observer
import butterknife.BindView
import com.jakewharton.rxbinding3.widget.editorActions
import eu.darken.bb.R
import eu.darken.bb.backup.core.getGeneratorId
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.setTextIfDifferentAndNotFocused
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.SwitchPreferenceView
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject


class AppEditorFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: AppEditorFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as AppEditorFragmentVDC.Factory
        factory.create(handle, requireArguments().getGeneratorId()!!)
    })


    @BindView(R.id.name_input) lateinit var labelInput: EditText
    @BindView(R.id.core_settings_container) lateinit var coreSettingsContainer: ViewGroup
    @BindView(R.id.core_settings_progress) lateinit var coreSettingsProgress: View

    @BindView(R.id.options_container) lateinit var optionsContainer: ViewGroup
    @BindView(R.id.options_progress) lateinit var optionsProgress: View

    @BindView(R.id.core_settings_autoinclude) lateinit var optionAutoInclude: SwitchPreferenceView
    @BindView(R.id.core_settings_includeuser) lateinit var optionIncludeUser: SwitchPreferenceView
    @BindView(R.id.core_settings_includesystem) lateinit var optionIncludeSystem: SwitchPreferenceView

    @BindView(R.id.options_backupapk) lateinit var optionBackupApk: SwitchPreferenceView
    @BindView(R.id.options_backupdata) lateinit var optionBackupData: SwitchPreferenceView
    @BindView(R.id.options_backupcache) lateinit var optionBackupCache: SwitchPreferenceView

    init {
        layoutRes = R.layout.generator_editor_app_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().subtitle = getString(R.string.backuptype_app_label)

        vdc.state.observe(this, Observer { state ->
            labelInput.setTextIfDifferentAndNotFocused(state.label)

            optionAutoInclude.isChecked = state.autoInclude
            optionIncludeUser.isChecked = state.includeUserApps
            optionIncludeSystem.isChecked = state.includeSystemApps

            optionBackupApk.isChecked = state.backupApk
            optionBackupData.isChecked = state.backupData
            optionBackupCache.isChecked = state.backupCache

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsProgress.setGone(!state.isWorking)
            optionsContainer.setInvisible(state.isWorking)
            optionsProgress.setGone(!state.isWorking)
        })

        optionAutoInclude.setSwitchListener { _, b -> vdc.onUpdateAutoInclude(b) }
        optionIncludeUser.setSwitchListener { _, b -> vdc.onUpdateIncludeUser(b) }
        optionIncludeSystem.setSwitchListener { _, b -> vdc.onUpdateIncludeSystem(b) }

        optionBackupApk.setSwitchListener { _, b -> vdc.onUpdateBackupApk(b) }
        optionBackupData.setSwitchListener { _, b -> vdc.onUpdateBackupData(b) }
        optionBackupCache.setSwitchListener { _, b -> vdc.onUpdateBackupCache(b) }


        labelInput.userTextChangeEvents().subscribe { vdc.updateLabel(it.text.toString()) }
        labelInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { labelInput.clearFocus() }

        super.onViewCreated(view, savedInstanceState)
    }


}
