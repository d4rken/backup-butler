package eu.darken.bb.task.ui.editor.backup.destinations

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
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.SetupBarView
import eu.darken.bb.storage.ui.list.StorageAdapter
import eu.darken.bb.task.ui.editor.backup.destinations.picker.StoragePickerFragmentArgs
import javax.inject.Inject

@AndroidEntryPoint
class DestinationsFragment : SmartFragment(R.layout.task_editor_backup_storages_fragment) {

    val navArgs by navArgs<DestinationsFragmentArgs>()

    private val vdc: DestinationsFragmentVDC by viewModels()

    @BindView(R.id.list_destinations) lateinit var destinationsList: RecyclerView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton
    @BindView(R.id.setupbar) lateinit var setupBar: SetupBarView

    @Inject lateinit var adapter: StorageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        destinationsList.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.removeDestination(adapter.data[i]) })

        setupBar.buttonPositivePrimary.clicksDebounced().subscribe { vdc.executeTask() }
        setupBar.buttonPositiveSecondary.clicksDebounced().subscribe { vdc.saveTask() }

        vdc.state.observe2(this) { state ->
            adapter.update(state.destinations)

            setupBar.buttonPositivePrimary.isEnabled = state.destinations.isNotEmpty()
            setupBar.buttonPositiveSecondary.isEnabled = state.destinations.isNotEmpty()
        }

        fab.clicksDebounced().subscribe {
            findNavController().navigate(
                R.id.nav_action_show_picker,
                StoragePickerFragmentArgs(taskId = navArgs.taskId).toBundle()
            )
        }

        vdc.finishEvent.observe2(this) { finishActivity() }
        super.onViewCreated(view, savedInstanceState)
    }
}
