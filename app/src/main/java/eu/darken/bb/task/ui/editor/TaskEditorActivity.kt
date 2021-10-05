package eu.darken.bb.task.ui.editor

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.v
import eu.darken.bb.common.navigation.isGraphSet
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActionBar
import eu.darken.bb.common.smart.SmartActivity
import eu.darken.bb.databinding.TaskEditorBackupActivityBinding
import eu.darken.bb.task.core.Task

@AndroidEntryPoint
class TaskEditorActivity : SmartActivity() {

    private val vdc: TaskEditorActivityVDC by viewModels()
    private lateinit var ui: TaskEditorBackupActivityBinding
    private val navController by lazy { findNavController(R.id.nav_host) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = TaskEditorBackupActivityBinding.inflate(layoutInflater)
        setContentView(ui.root)

        vdc.state.observe2(this) { state ->
            log { "isLoading=${state.isLoading} isGraphSet=${navController.isGraphSet()}" }
            if (!state.isLoading && !navController.isGraphSet()) {
                v { "Setting up NavController" }
                val graph = navController.navInflater.inflate(R.navigation.task_editor)
                if (state.requiresSetup) {
                    graph.startDestination = R.id.requirementsFragment
                } else {
                    graph.startDestination = state.stepFlow.start
                }
                navController.setGraph(graph, bundleOf("taskId" to state.taskId))
                setupActionBarWithNavController(navController)
                log { "Updating isGraphSet" }
                navController.addOnDestinationChangedListener { controller, destination, arguments ->
                    log { "Updating titles" }
                    requireActionBar().apply {
                        title = when (state.taskType) {
                            Task.Type.BACKUP_SIMPLE -> {
                                if (state.isExistingTask) getString(R.string.task_backup_edit_label)
                                else getString(R.string.task_backup_new_label)
                            }
                            Task.Type.RESTORE_SIMPLE -> {
                                if (state.isExistingTask) getString(R.string.task_editor_restore_edit_label)
                                else getString(R.string.task_editor_restore_new_label)
                            }
                        }
                        subtitle = destination.label
                        @SuppressLint("RestrictedApi")
                        if (controller.backStack.size <= 2) {
                            requireActionBar().setDisplayHomeAsUpEnabled(true)
                            requireActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24)
                        } else {
                            when (destination.id) {
                                R.id.storagePickerFragment -> supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_cancel)
                                R.id.generatorPickerFragment -> supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_cancel)
                                else -> supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
                            }
                        }
                    }
                }
            }
        }

        vdc.finishEvent.observe2(this) { finish() }

        onBackPressedDispatcher.addCallback {
            if (!navController.popBackStack()) finish()
        }
    }

    override fun onDestroy() {
        if (isFinishing) vdc.dismiss()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        else -> NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
    }

}
