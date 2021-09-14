package eu.darken.bb.task.ui.editor.backup.sources

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.list.GeneratorAdapter
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.SetupBarView
import eu.darken.bb.task.ui.editor.backup.destinations.DestinationsFragmentArgs
import eu.darken.bb.task.ui.editor.backup.destinations.picker.StoragePickerFragmentArgs
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class SourcesFragment : SmartFragment() {

    val navArgs by navArgs<SourcesFragmentArgs>()

    private val vdc: SourcesFragmentVDC by viewModels()

    @BindView(R.id.list_sources) lateinit var sourcesList: RecyclerView
    @BindView(R.id.setupbar) lateinit var setupBar: SetupBarView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton

    @Inject lateinit var adapter: GeneratorAdapter
    @Inject lateinit var pickerAdapterProvider: Provider<GeneratorAdapter>

    init {
        layoutRes = R.layout.task_editor_backup_generators_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sourcesList.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.removeSource(adapter.data[i]) })

        vdc.state.observe2(this) { state ->
            adapter.update(state.sources)

            setupBar.buttonPositiveSecondary.isEnabled = state.sources.isNotEmpty()
        }

        fab.clicksDebounced().subscribe {
            findNavController().navigate(
                R.id.nav_action_show_picker,
                StoragePickerFragmentArgs(taskId = navArgs.taskId).toBundle()
            )
        }

        setupBar.buttonPositiveSecondary.clicksDebounced().subscribe {
            findNavController().navigate(
                R.id.nav_action_next,
                DestinationsFragmentArgs(taskId = navArgs.taskId).toBundle()
            )
        }
        super.onViewCreated(view, savedInstanceState)
    }
}
