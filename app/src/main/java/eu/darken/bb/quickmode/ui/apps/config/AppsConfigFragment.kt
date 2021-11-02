package eu.darken.bb.quickmode.ui.apps.config

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.errors.asErrorDialogBuilder
import eu.darken.bb.common.lists.modular.mods.TypedVHCreatorMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.navigation.popBackStack
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.QuickmodeAppsConfigFragmentBinding
import eu.darken.bb.quickmode.ui.common.config.ConfigAdapter
import eu.darken.bb.storage.ui.picker.StoragePickerResultListener

@AndroidEntryPoint
class AppsConfigFragment : SmartFragment(R.layout.quickmode_apps_config_fragment), StoragePickerResultListener {

    private val vdc: AppsConfigFragmentVDC by viewModels()
    private val ui: QuickmodeAppsConfigFragmentBinding by viewBinding()
    private val adapter = ConfigAdapter { data ->
        listOf(
            TypedVHCreatorMod({ data[it] is AppsOptionVH.Item }) { AppsOptionVH(it) }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStoragePickerListener {
            vdc.onStoragePickerResult(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            recyclerView.setupDefaults(adapter, dividers = false)
            toolbar.apply {
                title = getString(R.string.backup_type_app_label)
                subtitle = getString(R.string.quick_mode_subtitle)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_reset -> {
                            vdc.reset()
                            true
                        }
                        else -> false
                    }
                }
                setNavigationOnClickListener { popBackStack() }
            }
        }

        vdc.state.observe2(this, ui) { state ->
            toolbar.menu.apply {
                findItem(R.id.action_reset).isVisible = state.isExisting
            }
            adapter.update(state.items)
        }

        vdc.navEvents.observe2(this) {
            it?.run { doNavigate(this) } ?: popBackStack()
        }
        vdc.errorEvent.observe2(this) { it.asErrorDialogBuilder(requireContext()).show() }

        super.onViewCreated(view, savedInstanceState)
    }
}
