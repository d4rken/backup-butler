package eu.darken.bb.task.ui.editor.common.requirements

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.SetupBarView
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.ui.editor.backup.sources.SourcesFragmentArgs
import javax.inject.Inject

@AndroidEntryPoint
class RequirementsFragment : SmartFragment(R.layout.task_editor_requirements_fragment) {

    val navArgs by navArgs<RequirementsFragmentArgs>()

    private val vdc: RequirementsFragmentVDC by viewModels()

    @BindView(R.id.explanation_more_action) lateinit var requirementsMoreAction: Button
    @BindView(R.id.setupbar) lateinit var setupBar: SetupBarView
    @BindView(R.id.requirements_list) lateinit var requirementsList: RecyclerView

    @Inject lateinit var adapter: RequirementsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requirementsList.isNestedScrollingEnabled = false
        requirementsList.setupDefaults(adapter, dividers = false)
        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.runMainAction(adapter.data[i]) })

        vdc.state.observe2(this) { state ->
            adapter.update(state.requirements)

            setupBar.buttonPositiveSecondary.clicksDebounced().subscribe {
                val nextStep = when (state.taskType) {
                    Task.Type.BACKUP_SIMPLE -> R.id.action_permissionFragment_to_introFragment
                    Task.Type.RESTORE_SIMPLE -> R.id.action_permissionFragment_to_restoreSourcesFragment
                }
                findNavController().navigate(
                    nextStep,
                    SourcesFragmentArgs(taskId = navArgs.taskId).toBundle()
                )
            }
        }

        requirementsMoreAction.clicksDebounced().subscribe {
            AlertDialog.Builder(requireContext()).setMessage(R.string.requirements_extended_desc).show()
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
