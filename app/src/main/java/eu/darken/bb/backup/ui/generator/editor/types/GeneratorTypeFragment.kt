package eu.darken.bb.backup.ui.generator.editor.types

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.getGeneratorId
import eu.darken.bb.backup.core.putGeneratorId
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject


class GeneratorTypeFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: GeneratorTypeFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as GeneratorTypeFragmentVDC.Factory
        factory.create(handle, requireArguments().getGeneratorId()!!)
    })

    @Inject lateinit var adapter: GeneratorTypeAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView

    init {
        layoutRes = R.layout.generator_editor_typeselection_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().subtitle = getString(R.string.label_select_type)
        recyclerView.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.createType(adapter.data[i]) })

        vdc.state.observe(this, Observer {
            adapter.update(it.supportedTypes)
        })

        vdc.navigationEvent.observe2(this) { (type, id) ->
            val nextStep = when (type) {
                Backup.Type.APP -> R.id.action_generatorTypeFragment_to_appEditorFragment
                Backup.Type.FILES -> R.id.action_generatorTypeFragment_to_filesEditorFragment
            }
            findNavController().navigate(nextStep, Bundle().apply { putGeneratorId(id) })
            val appbarConfig = AppBarConfiguration.Builder(R.id.source_editor, R.id.appEditorFragment, R.id.filesEditorFragment).build()
            setupActionBarWithNavController(requireActivity() as AppCompatActivity, findNavController(), appbarConfig)
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
