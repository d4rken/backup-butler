package eu.darken.bb.quickmode.ui.apps.config

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.mods.TypedVHCreatorMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.navigation.popBackStack
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.QuickmodeAppsConfigFragmentBinding
import eu.darken.bb.quickmode.ui.common.config.ConfigAdapter
import eu.darken.bb.storage.ui.picker.StoragePickerListener

@AndroidEntryPoint
class AppsConfigFragment : Smart2Fragment(R.layout.quickmode_apps_config_fragment), StoragePickerListener {

    override val vdc: AppsConfigFragmentVDC by viewModels()
    override val ui: QuickmodeAppsConfigFragmentBinding by viewBinding()
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

        vdc.state.observe2(ui) { state ->
            toolbar.menu.apply {
                findItem(R.id.action_reset).isVisible = state.isExisting
            }
            adapter.update(state.items)
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
