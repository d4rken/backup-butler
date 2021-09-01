package eu.darken.bb.backup.ui.generator.editor.types.files

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.files.ui.picker.APathPicker
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferentAndNotFocused
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.toastError
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject


class FilesEditorConfigFragment : SmartFragment(), AutoInject {

    val navArgs by navArgs<FilesEditorConfigFragmentArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: FilesEditorConfigFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as FilesEditorConfigFragmentVDC.Factory
        factory.create(handle, navArgs.generatorId)
    })

    @BindView(R.id.name_input) lateinit var labelInput: EditText
    @BindView(R.id.core_settings_container) lateinit var coreSettingsContainer: ViewGroup
    @BindView(R.id.core_settings_loadingoverlay) lateinit var coreSettingsLoadingOverlay: LoadingOverlayView

    @BindView(R.id.path_display) lateinit var pathDisplay: TextView
    @BindView(R.id.path_select_button) lateinit var pathButton: Button

    private var allowCreate: Boolean = false
    private var existing: Boolean = false

    init {
        layoutRes = R.layout.generator_editor_file_fragment
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe2(this) { state ->
            labelInput.setTextIfDifferentAndNotFocused(state.label)
            pathDisplay.text = state.path?.userReadablePath(requireContext())

            pathButton.setText(if (state.path == null) R.string.general_select_action else R.string.general_change_action)

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsLoadingOverlay.setInvisible(!state.isWorking)

            allowCreate = state.isValid
            existing = state.isExisting
            invalidateOptionsMenu()
        }

        labelInput.userTextChangeEvents().subscribe { vdc.updateLabel(it.text.toString()) }
        pathButton.clicksDebounced().subscribe { vdc.showPicker() }

        vdc.pickerEvent.observe2(this) {
            val intent = APathPicker.createIntent(requireContext(), it)
            startActivityForResult(intent, 13)
        }

        vdc.finishEvent.observe2(this) { requireActivity().finish() }

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_generator_editor_files_config, menu)
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
        else -> super.onOptionsItemSelected(item)
    }

}
