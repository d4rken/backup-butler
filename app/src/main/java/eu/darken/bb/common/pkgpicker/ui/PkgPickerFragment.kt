package eu.darken.bb.common.pkgpicker.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.getCountString
import eu.darken.bb.common.lists.differ.update
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.navigation.popBackStack
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.PkgPickerFragmentBinding
import eu.darken.bb.storage.ui.editor.StorageEditorResultListener
import javax.inject.Inject

@AndroidEntryPoint
class PkgPickerFragment : SmartFragment(R.layout.pkg_picker_fragment),
    StorageEditorResultListener {

    private val vdc: PkgPickerFragmentVDC by viewModels()
    private val ui: PkgPickerFragmentBinding by viewBinding()

    @Inject lateinit var adapter: PkgPickerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            storageList.setupDefaults(adapter)
            toolbar.apply {
                setupWithNavController(findNavController())
                setNavigationIcon(R.drawable.ic_baseline_close_24)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_select_all -> {
                            vdc.selectAll()
                            true
                        }
                        R.id.action_unselect_all -> {
                            vdc.unselectAll()
                            true
                        }
                        else -> false
                    }
                }
            }

            fab.setOnClickListener { vdc.done() }
        }

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.selectPkg(adapter.data[i]) })

        vdc.state.observe2(this, ui) { state ->
            adapter.update(state.items)
            toolbar.apply {
                subtitle = resources.getCountString(R.plurals.x_selected, state.selected.size)
                menu.apply {
                    findItem(R.id.action_select_all).isVisible = state.items.size != state.selected.size
                    findItem(R.id.action_unselect_all).isVisible = state.selected.isNotEmpty()
                }
            }
            fab.isInvisible = state.selected.isEmpty()
        }

        vdc.navEvents.observe2(this) { doNavigate(it) }

        vdc.finishEvent.observe2(this) {
            setPkgPickerResult(it)
            popBackStack()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
