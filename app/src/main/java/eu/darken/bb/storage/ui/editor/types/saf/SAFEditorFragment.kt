package eu.darken.bb.storage.ui.editor.types.saf

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import butterknife.BindView
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.widget.editorActions
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.files.ui.picker.APathPicker
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferent
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.core.ExistingStorageException
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject

@AndroidEntryPoint
class SAFEditorFragment : SmartFragment() {

    val navArgs by navArgs<SAFEditorFragmentArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: SAFEditorFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as SAFEditorFragmentVDC.Factory
        factory.create(handle, navArgs.storageId)
    })

    @Inject lateinit var adapter: StorageAdapter

    @BindView(R.id.name_input) lateinit var labelInput: EditText

    @BindView(R.id.path_display) lateinit var pathDisplay: TextView
    @BindView(R.id.path_button) lateinit var pathSelect: TextView

    @BindView(R.id.core_settings_container) lateinit var coreSettingsContainer: ViewGroup
    @BindView(R.id.core_settings_progress) lateinit var coreSettingsProgress: View

    private var allowCreate: Boolean = false
    private var existing: Boolean = false

    init {
        layoutRes = R.layout.storage_editor_saf_fragment
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe2(this) { state ->
            labelInput.setTextIfDifferent(state.label)

            pathDisplay.text = state.path
            pathSelect.isEnabled = !state.isExisting

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsProgress.setInvisible(!state.isWorking)

            allowCreate = state.isValid
            existing = state.isExisting
            requireActivity().invalidateOptionsMenu()
        }

        pathSelect.clicksDebounced().subscribe { vdc.selectPath() }

        labelInput.userTextChangeEvents().subscribe { vdc.updateName(it.text.toString()) }
        labelInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { labelInput.clearFocus() }

        vdc.openPickerEvent.observe2(this) {
            startActivityForResult(APathPicker.createIntent(requireContext(), it), 13)
        }

        vdc.errorEvent.observe2(this) { error ->
            val snackbar = Snackbar.make(
                view,
                error.tryLocalizedErrorMessage(requireContext()),
                Snackbar.LENGTH_LONG
            )
            if (error is ExistingStorageException) {
                snackbar.setAction(R.string.general_import_action) {
                    vdc.importStorage(error.path)
                }
            }
            snackbar.show()
        }

        vdc.finishEvent.observe2(this) {
            finishActivity()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            13 -> APathPicker.checkForNonNeutralResult(this, resultCode, data) { vdc.onUpdatePath(it) }
            else -> throw IllegalArgumentException("Unknown activity result: code=$requestCode, resultCode=$resultCode, data=$data")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_storage_editor_saf_fragment, menu)
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
