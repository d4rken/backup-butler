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
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StorageEditorTypeselectionFragmentBinding
import eu.darken.bb.storage.core.Storage
import javax.inject.Inject

@AndroidEntryPoint
class TypeSelectionFragment : SmartFragment(R.layout.storage_editor_typeselection_fragment) {

    private val vdc: TypeSelectionFragmentVDC by viewModels()
    private val ui: StorageEditorTypeselectionFragmentBinding by viewBinding()
    @Inject lateinit var adapter: TypeSelectionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.recyclerview.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.createType(adapter.data[i]) })

        vdc.state.observe2(this) {
            adapter.update(it.supportedTypes)
        }

        vdc.navigationEvent.observe2(this) { (type, id) ->
            val nextStep = when (type) {
                Storage.Type.LOCAL -> TypeSelectionFragmentDirections.actionTypeSelectionFragmentToLocalEditorFragment(
                    storageId = id
                )
                Storage.Type.SAF -> TypeSelectionFragmentDirections.actionTypeSelectionFragmentToSafEditorFragment(
                    storageId = id
                )
            }
            doNavigate(nextStep)
        }

        super.onViewCreated(view, savedInstanceState)
    }

}
