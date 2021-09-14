package eu.darken.bb.storage.ui.editor.types

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.ui.editor.types.local.LocalEditorFragmentArgs
import eu.darken.bb.storage.ui.editor.types.saf.SAFEditorFragmentArgs
import javax.inject.Inject

@AndroidEntryPoint
class TypeSelectionFragment : SmartFragment() {

    private val vdc: TypeSelectionFragmentVDC by viewModels()
    @Inject lateinit var adapter: TypeSelectionAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView

    init {
        layoutRes = R.layout.storage_editor_typeselection_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.createType(adapter.data[i]) })

        vdc.state.observe2(this) {
            adapter.update(it.supportedTypes)
        }

        vdc.navigationEvent.observe2(this) { (type, id) ->
            val nextStep = when (type) {
                Storage.Type.LOCAL -> R.id.action_typeSelectionFragment_to_localEditorFragment
                Storage.Type.SAF -> R.id.action_typeSelectionFragment_to_safEditorFragment
            }
            val args = when (type) {
                Storage.Type.LOCAL -> LocalEditorFragmentArgs(storageId = id).toBundle()
                Storage.Type.SAF -> SAFEditorFragmentArgs(storageId = id).toBundle()
            }
            val appbarConfig = AppBarConfiguration.Builder(R.id.localEditorFragment, R.id.safEditorFragment).build()
            NavigationUI.setupActionBarWithNavController(
                requireActivity() as AppCompatActivity,
                findNavController(),
                appbarConfig
            )
            findNavController().navigate(nextStep, args)
        }

        super.onViewCreated(view, savedInstanceState)
    }

}
