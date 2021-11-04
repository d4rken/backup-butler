package eu.darken.bb.storage.ui.editor.types

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.navigation.popBackStack
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StorageEditorTypeselectionFragmentBinding
import eu.darken.bb.storage.core.Storage
import javax.inject.Inject

@AndroidEntryPoint
class StorageTypeFragment : SmartFragment(R.layout.storage_editor_typeselection_fragment) {

    private val vdc: StorageTypeFragmentVDC by viewModels()
    private val ui: StorageEditorTypeselectionFragmentBinding by viewBinding()
    @Inject lateinit var adapter: StorageTypeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            recyclerview.setupDefaults(adapter)
            toolbar.apply {
                setNavigationIcon(R.drawable.ic_baseline_close_24)
                setNavigationOnClickListener { popBackStack() }
            }
        }

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.createType(adapter.data[i]) })

        vdc.state.observe2(this) {
            adapter.update(it.supportedTypes)
        }

        vdc.navigationEvent.observe2(this) { (type, id) ->
            val nextStep = when (type) {
                Storage.Type.LOCAL -> StorageTypeFragmentDirections.actionTypeSelectionFragmentToLocalEditorFragment(
                    storageId = id
                )
                Storage.Type.SAF -> StorageTypeFragmentDirections.actionTypeSelectionFragmentToSafEditorFragment(
                    storageId = id
                )
            }
            doNavigate(nextStep)
        }

        super.onViewCreated(view, savedInstanceState)
    }

}
