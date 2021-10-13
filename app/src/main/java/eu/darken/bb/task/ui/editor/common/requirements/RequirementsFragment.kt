package eu.darken.bb.task.ui.editor.common.requirements

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorRequirementsFragmentBinding
import javax.inject.Inject

@AndroidEntryPoint
class RequirementsFragment : SmartFragment(R.layout.task_editor_requirements_fragment) {

    val navArgs by navArgs<RequirementsFragmentArgs>()
    private val ui: TaskEditorRequirementsFragmentBinding by viewBinding()
    private val vdc: RequirementsFragmentVDC by viewModels()

    @Inject lateinit var adapter: RequirementsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.requirementsList.apply {
            isNestedScrollingEnabled = false
            setupDefaults(adapter, dividers = false)
        }
        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.runMainAction(adapter.data[i]) })

        vdc.state.observe2(this, ui) { adapter.update(it.requirements) }
        vdc.navEvents.observe2(this) { doNavigate(it) }

        vdc.runTimePermissionEvent.observe2(this) { req ->
            requestPermissions(arrayOf(req.permission), 1)
        }

        ui.explanationMoreAction.clicksDebounced().subscribe {
            AlertDialog.Builder(requireContext()).setMessage(R.string.requirements_extended_desc).show()
        }

        ui.setupbar.buttonPositiveSecondary.clicksDebounced().subscribe {
            vdc.onContinue()
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
