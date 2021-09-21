package eu.darken.bb.task.ui.editor.backup.sources.picker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.list.GeneratorAdapter
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.RecyclerViewWrapperLayout
import eu.darken.bb.common.ui.setInvisible
import javax.inject.Inject

@AndroidEntryPoint
class GeneratorPickerFragment : SmartFragment(R.layout.task_editor_backup_generators_picker_fragment) {

    private val vdc: GeneratorPickerFragmentVDC by viewModels()

    @Inject lateinit var adapter: GeneratorAdapter
    @BindView(R.id.storage_list_wrapper) lateinit var generatorListWrapper: RecyclerViewWrapperLayout
    @BindView(R.id.storage_list) lateinit var generatorList: RecyclerView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        generatorList.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.selectGenerator(adapter.data[i]) })

        vdc.generatorData.observe2(this) { state ->
            adapter.update(state.generatorData)

            if (state.allExistingAdded) {
                generatorListWrapper.setEmptyState(
                    R.drawable.ic_emoji_happy,
                    R.string.task_editor_backup_sources_picker_alladded_desc
                )
            } else {
                generatorListWrapper.setEmptyState(
                    R.drawable.ic_emoji_neutral,
                    R.string.task_editor_backup_sources_picker_empty_desc
                )
            }

            generatorListWrapper.updateLoadingState(state.isLoading)
            fab.setInvisible(state.isLoading)

            requireActivity().invalidateOptionsMenu()
        }

        vdc.finishEvent.observe2(this) {
            findNavController().popBackStack()
        }

        fab.clicksDebounced().subscribe { vdc.createGenerator() }

        super.onViewCreated(view, savedInstanceState)
    }
}
