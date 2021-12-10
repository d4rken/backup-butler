package eu.darken.bb.task.ui.editor.common.requirements

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorRequirementsFragmentBinding
import eu.darken.bb.task.core.Task
import javax.inject.Inject

@AndroidEntryPoint
class RequirementsFragment : Smart2Fragment(R.layout.task_editor_requirements_fragment) {

    val navArgs by navArgs<RequirementsFragmentArgs>()
    override val ui: TaskEditorRequirementsFragmentBinding by viewBinding()
    override val vdc: RequirementsFragmentVDC by viewModels()

    @Inject lateinit var adapter: RequirementsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            toolbar.apply {
                setupWithNavController(findNavController())
                setNavigationIcon(R.drawable.ic_baseline_close_24)
            }
            requirementsList.apply {
                isNestedScrollingEnabled = false
                setupDefaults(adapter, dividers = false)
            }
            explanationMoreAction.setOnClickListener {
                AlertDialog.Builder(requireContext()).setMessage(R.string.requirements_extended_desc).show()
            }
            setupbar.buttonPositiveSecondary.setOnClickListener {
                vdc.onContinue()
            }
        }

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.runMainAction(adapter.data[i]) })

        vdc.state.observe2(ui) {
            toolbar.title = when (it.taskType) {
                Task.Type.BACKUP_SIMPLE -> getString(R.string.task_editor_backup_new_label)
                Task.Type.RESTORE_SIMPLE -> getString(R.string.task_editor_restore_new_label)
            }
            adapter.update(it.requirements)
        }

        vdc.runTimePermissionEvent.observe2(this) { req ->
            requestPermissions(arrayOf(req.permission), 1)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            vdc.onPermissionResult(grantResults.all { it == PackageManager.PERMISSION_GRANTED })
        } else {
            throw IllegalArgumentException("Unknown permission request: code=$requestCode, permissions=$permissions")
        }
    }
}
