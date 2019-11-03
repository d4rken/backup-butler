package eu.darken.bb.task.ui.editor.backup.sources.picker

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.list.GeneratorAdapter
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.EmptyRecyclerView
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject


class GeneratorPickerFragment : SmartFragment(), AutoInject {

    val navArgs by navArgs<GeneratorPickerFragmentArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: GeneratorPickerFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as GeneratorPickerFragmentVDC.Factory
        factory.create(handle, navArgs.taskId)
    })

    @Inject lateinit var adapter: GeneratorAdapter
    @BindView(R.id.storage_list) lateinit var generatorList: EmptyRecyclerView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton
    @BindView(R.id.loading_overlay) lateinit var loadingOverlay: LoadingOverlayView

    init {
        layoutRes = R.layout.task_editor_backup_generators_picker_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        generatorList.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.selectGenerator(adapter.data[i]) })

        vdc.generatorData.observe2(this) { state ->
            adapter.update(state.generatorData)

            loadingOverlay.setInvisible(!state.isLoading)
            generatorList.setInvisible(state.isLoading)
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
