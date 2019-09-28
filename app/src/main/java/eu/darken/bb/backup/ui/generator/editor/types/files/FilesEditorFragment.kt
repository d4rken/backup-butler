package eu.darken.bb.backup.ui.generator.editor.types.files

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.backup.core.getGeneratorId
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferentAndNotFocused
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class FilesEditorFragment : BaseEditorFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    override val vdc: FilesEditorFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as FilesEditorFragmentVDC.Factory
        factory.create(handle, arguments!!.getGeneratorId()!!)
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
            pathDisplay.text = state.path

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsLoadingOverlay.setInvisible(!state.isWorking)
        })

        labelInput.userTextChangeEvents().debounce(2, TimeUnit.SECONDS).subscribe { vdc.updateLabel(it.text.toString()) }

        pathButton.clicksDebounced().subscribe { vdc.showPicker() }

        vdc.openSAFPickerEvent.observe2(this) {
            startActivityForResult(it, 13)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        if (requestCode == 13) {
            if (resultCode == Activity.RESULT_OK && result?.data != null) {
                vdc.updatePathSAF(result.data!!)
            } else {
                Toast.makeText(context, R.string.msg_please_try_again, Toast.LENGTH_SHORT).show()
            }
        } else {
            throw IllegalArgumentException("Unknown activity result: code=$requestCode, resultCode=$resultCode, result=$result")
        }
        super.onActivityResult(requestCode, resultCode, result)
    }

}
