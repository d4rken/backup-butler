package eu.darken.bb.backup.ui.generator.editor.types.files.legacy

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.Observer
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.backup.core.getGeneratorId
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.setTextIfDifferent
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject


class LegacyFilesEditorFragment : BaseEditorFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    override val vdc: LegacyFilesEditorFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as LegacyFilesEditorFragmentVDC.Factory
        factory.create(handle, arguments!!.getGeneratorId()!!)
    })

    @BindView(R.id.name_input) lateinit var labelInput: EditText
    @BindView(R.id.path_input) lateinit var pathInput: EditText
    @BindView(R.id.core_settings_container) lateinit var coreSettingsContainer: ViewGroup
    @BindView(R.id.core_settings_loadingoverlay) lateinit var coreSettingsLoadingOverlay: LoadingOverlayView

    init {
        layoutRes = R.layout.generator_editor_file_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().subtitle = getString(R.string.backuptype_files_label)
        pathInput.isEnabled = false
        vdc.state.observe(this, Observer { state ->
            labelInput.setTextIfDifferent(state.label)
            pathInput.setTextIfDifferent(state.path)

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsLoadingOverlay.setInvisible(!state.isWorking)
        })

        labelInput.userTextChangeEvents().subscribe { vdc.updateLabel(it.text.toString()) }
        pathInput.userTextChangeEvents().subscribe { vdc.updatePath(it.text.toString()) }

        super.onViewCreated(view, savedInstanceState)
    }

}
