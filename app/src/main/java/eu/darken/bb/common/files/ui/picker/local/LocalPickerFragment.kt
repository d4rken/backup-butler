package eu.darken.bb.common.files.ui.picker.local

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.error.asErrorDialogBuilder
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.ui.picker.SharedPathPickerVM
import eu.darken.bb.common.flow.launchInView
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.permission.Permission
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.ui.BreadCrumbBar
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.PathPickerLocalFragmentBinding
import kotlinx.coroutines.flow.onEach
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class LocalPickerFragment : Smart2Fragment(R.layout.path_picker_local_fragment) {

    val navArgs by navArgs<LocalPickerFragmentArgs>()
    override val vdc: LocalPickerFragmentVDC by viewModels()
    override val ui: PathPickerLocalFragmentBinding by viewBinding()

    @Inject lateinit var adapter: PathLookupAdapter
    private val sharedVM by lazy { ViewModelProvider(requireActivity())[SharedPathPickerVM::class.java] }

    private var allowCreateDir = false

    init {
        setHasOptionsMenu(true)
    }

    private lateinit var permissionWriteStorageLauncher: Permission.Launcher
    private lateinit var permissionManageStorageLauncher: Permission.Launcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionWriteStorageLauncher = Permission.WRITE_EXTERNAL_STORAGE.setup(this) { perm, _ ->
            vdc.onUpdatePermission(perm)
        }
        permissionManageStorageLauncher = Permission.MANAGE_EXTERNAL_STORAGE.setup(this) { perm, _ ->
            vdc.onUpdatePermission(perm)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (ui.breadcrumbBar as BreadCrumbBar<APath>).apply {
            crumbNamer = { if (it.name.isEmpty()) File.separator else it.name }
            crumbListener = { vdc.selectItem(it) }
        }

        ui.filesList.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.selectItem(adapter.data[i]) })

        vdc.state.observe2(this) { state ->
            ui.breadcrumbBar.setCrumbs(state.currentCrumbs)
            adapter.update(state.currentListing)

            allowCreateDir = state.allowCreateDir
            invalidateOptionsMenu()
        }

        vdc.errorEvents.observe2(this) { error ->
            error.asErrorDialogBuilder(requireContext()).show()
        }

        ui.selectAction.setOnClickListener { vdc.finishSelection() }

        vdc.resultEvents.observe2(this) {
            sharedVM.postResult(it)
        }
        vdc.missingPermissionEvent.observe2(this) { permission ->
            Snackbar
                .make(
                    requireView(),
                    R.string.storage_additional_permission_required_msg,
                    Snackbar.LENGTH_INDEFINITE
                )
                .setAction(R.string.general_grant_action) { vdc.grantPermission(permission) }
                .show()
        }
        vdc.requestPermissionEvent.observe2(this) { perm ->
            when (perm) {
                Permission.WRITE_EXTERNAL_STORAGE -> permissionWriteStorageLauncher.launch()
                Permission.MANAGE_EXTERNAL_STORAGE -> permissionManageStorageLauncher.launch()
                else -> throw UnsupportedOperationException("Requesting $perm is not setup for this screen.")
            }
        }

        vdc.createDirEvent.observe2(this) {
            val alertLayout = layoutInflater.inflate(R.layout.view_alertdialog_edittext, null)
            val input = alertLayout.findViewById<EditText>(R.id.input_text)

            val dialog = MaterialAlertDialogBuilder(requireContext()).apply {
                setView(alertLayout)
                setTitle(R.string.general_create_dir)
                setPositiveButton(R.string.general_create_action) { _, _ ->
                    vdc.createDir(input.text.toString())
                }
                setNegativeButton(R.string.general_cancel_action) { _, _ ->

                }
            }.create()

            dialog.setOnShowListener {
                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.isEnabled = false
                input.userTextChangeEvents()
                    .onEach { positiveButton.isEnabled = it.text.isNotEmpty() }
                    .launchInView(this@LocalPickerFragment)
            }

            dialog.show()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_pathpicker_local, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_create_dir).isVisible = allowCreateDir
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_create_dir -> {
            vdc.createDirRequest()
            true
        }
        R.id.action_home -> {
            vdc.goHome()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
