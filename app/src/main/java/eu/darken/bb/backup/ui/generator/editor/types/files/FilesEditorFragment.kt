package eu.darken.bb.backup.ui.generator.editor.types.files

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.backup.core.getGeneratorId
import eu.darken.bb.common.*
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.file.picker.APathPicker
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject


class FilesEditorFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: FilesEditorFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as FilesEditorFragmentVDC.Factory
        factory.create(handle, requireArguments().getGeneratorId()!!)
    })

    @BindView(R.id.name_input) lateinit var labelInput: EditText
    @BindView(R.id.core_settings_container) lateinit var coreSettingsContainer: ViewGroup
    @BindView(R.id.core_settings_loadingoverlay) lateinit var coreSettingsLoadingOverlay: LoadingOverlayView

    @BindView(R.id.path_display) lateinit var pathDisplay: TextView
    @BindView(R.id.path_select_button) lateinit var pathButton: Button

    init {
        layoutRes = R.layout.generator_editor_file_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().subtitle = getString(R.string.backuptype_files_label)

        vdc.state.observe(this, Observer { state ->
            labelInput.setTextIfDifferentAndNotFocused(state.label)
            pathDisplay.text = state.path?.userReadablePath(requireContext())

            pathButton.setText(if (state.path == null) R.string.action_select else R.string.action_change)

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsLoadingOverlay.setInvisible(!state.isWorking)
        })

        labelInput.userTextChangeEvents().subscribe { vdc.updateLabel(it.text.toString()) }
        pathButton.clicksDebounced().subscribe { vdc.showPicker() }

        vdc.pickerEvent.observe2(this) {
            val intent = APathPicker.createIntent(requireContext(), it)
            startActivityForResult(intent, 13)
        }

        vdc.errorEvent.observe2(this) { toastError(it) }

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
