package eu.darken.bb.backup.ui.generator.editor.types

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.ui.generator.editor.types.app.config.AppEditorConfigFragmentArgs
import eu.darken.bb.backup.ui.generator.editor.types.files.FilesEditorConfigFragmentArgs
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject

@AndroidEntryPoint
class GeneratorTypeFragment : SmartFragment() {

    val navArgs by navArgs<GeneratorTypeFragmentArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: GeneratorTypeFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as GeneratorTypeFragmentVDC.Factory
        factory.create(handle, navArgs.generatorId)
    })

    @Inject lateinit var adapter: GeneratorTypeAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView

    init {
        layoutRes = R.layout.generator_editor_typeselection_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.createType(adapter.data[i]) })

        vdc.state.observe2(this) {
            adapter.update(it.supportedTypes)
        }

        vdc.navigationEvent.observe2(this) { (type, id) ->
            val nextStep = when (type) {
                Backup.Type.APP -> R.id.action_generatorTypeFragment_to_appEditorFragment
                Backup.Type.FILES -> R.id.action_generatorTypeFragment_to_filesEditorFragment
            }
            val args = when (type) {
                Backup.Type.APP -> AppEditorConfigFragmentArgs(generatorId = id).toBundle()
                Backup.Type.FILES -> FilesEditorConfigFragmentArgs(generatorId = id).toBundle()
            }
            val appbarConfig =
                AppBarConfiguration.Builder(R.id.appEditorConfigFragment, R.id.filesEditorFragment).build()
            setupActionBarWithNavController(requireActivity() as AppCompatActivity, findNavController(), appbarConfig)
            findNavController().navigate(nextStep, args)
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
