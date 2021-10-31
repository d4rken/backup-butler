package eu.darken.bb.task.ui.tasklist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.view.clicks
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.lists.differ.update
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskListFragmentBinding
import eu.darken.bb.main.ui.MainFragmentDirections
import eu.darken.bb.processor.ui.ProcessorActivity
import javax.inject.Inject

@AndroidEntryPoint
class TaskListFragment : SmartFragment(R.layout.task_list_fragment) {

    private val vdc: TaskListFragmentVDC by viewModels()
    private val ui: TaskListFragmentBinding by viewBinding()
    @Inject lateinit var adapter: TaskListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            tasksList.setupDefaults(adapter)
            fab.clicks().subscribe { vdc.newTask() }
        }

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.editTask(adapter.data[i].task) })

        vdc.state.observe2(this, ui) { state ->
            log { "Updating UI state with $state" }
            adapter.update(state.tasks)
            fab.isInvisible = false
        }

        vdc.editTaskEvent.observe2(this) {
            doNavigate(MainFragmentDirections.actionMainFragmentToTaskActionDialog(it.taskId))
        }

        var snackbar: Snackbar? = null
        vdc.processorEvent.observe2(this) { isActive ->
            if (isVisible && isActive && snackbar == null) {
                snackbar = Snackbar.make(view, R.string.progress_processing_task_label, Snackbar.LENGTH_INDEFINITE)
                    .setAnchorView(ui.fab)
                    .setAction(R.string.general_show_action) {
                        startActivity(Intent(requireContext(), ProcessorActivity::class.java))
                    }
                snackbar?.show()
            } else if (!isActive && snackbar != null) {
                snackbar?.dismiss()
                snackbar = null
            }
        }
        vdc.navEvents.observe2(this) { doNavigate(it) }
        super.onViewCreated(view, savedInstanceState)
    }
}
