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
import eu.darken.bb.common.setTextIfDifferent
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject


class AppEditorFragment : BaseEditorFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    override val vdc: AppEditorFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as AppEditorFragmentVDC.Factory
        factory.create(handle, arguments!!.getGeneratorId()!!)
    })


    @BindView(R.id.name_input) lateinit var labelInput: EditText
    @BindView(R.id.input_include_packages) lateinit var includedPackagesInput: EditText
    @BindView(R.id.core_settings_container) lateinit var coreSettingsContainer: ViewGroup
    @BindView(R.id.core_settings_progress) lateinit var coreSettingsProgress: View
    @BindView(R.id.auto_include_apps) lateinit var autoIncludeApps: View

    @BindView(R.id.options_container) lateinit var optionsContainer: ViewGroup
    @BindView(R.id.options_progress) lateinit var optionsProgress: View

    init {
        layoutRes = R.layout.generator_editor_app_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().subtitle = getString(R.string.backuptype_app_label)

        vdc.state.observe(this, Observer { state ->
            labelInput.setTextIfDifferent(state.label)

            coreSettingsContainer.visibility = if (state.isWorking) View.INVISIBLE else View.VISIBLE
            coreSettingsProgress.visibility = if (state.isWorking) View.VISIBLE else View.INVISIBLE
            optionsContainer.visibility = if (state.isWorking) View.INVISIBLE else View.VISIBLE
            optionsProgress.visibility = if (state.isWorking) View.VISIBLE else View.INVISIBLE

            includedPackagesInput.setTextIfDifferent(state.includedPackages.joinToString(","))
        })


        labelInput.userTextChangeEvents().subscribe { vdc.updateLabel(it.text.toString()) }
        labelInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { labelInput.clearFocus() }

        includedPackagesInput.userTextChangeEvents().subscribe {
            vdc.updateIncludedPackages(it.text.split(','))
        }

        super.onViewCreated(view, savedInstanceState)
    }


}
