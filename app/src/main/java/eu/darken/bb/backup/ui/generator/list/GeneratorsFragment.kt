package eu.darken.bb.backup.ui.generator.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding4.view.clicks
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import javax.inject.Inject

@AndroidEntryPoint
class GeneratorsFragment : SmartFragment(R.layout.generator_list_fragment) {

    private val vdc: GeneratorsFragmentVDC by viewModels()
    @Inject lateinit var adapter: GeneratorAdapter

    @BindView(R.id.generator_list) lateinit var generatorList: RecyclerView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        generatorList.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.editGenerator(adapter.data[i]) })

        vdc.viewState.observe2(this) {
            adapter.update(it.generators)
        }

        fab.clicks().subscribe { vdc.newGenerator() }

        vdc.editTaskEvent.observe2(this) {
            doNavigate(GeneratorsFragmentDirections.actionGeneratorsFragmentToGeneratorsActionDialog(it.generatorId))
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
