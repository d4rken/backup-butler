package eu.darken.bb.task.ui.editor

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import butterknife.BindView
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.navigation.hasAction
import eu.darken.bb.common.navigation.isGraphSet
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartActivity
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.getTaskId
import eu.darken.bb.task.core.putTaskId
import javax.inject.Inject

class TaskEditorActivity : SmartActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: TaskEditorActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as TaskEditorActivityVDC.Factory
        factory.create(handle, intent.getTaskId()!!)
    })
    @BindView(R.id.button_cancel) lateinit var buttonCancel: Button
    @BindView(R.id.button_next) lateinit var buttonNext: Button
    @BindView(R.id.button_save) lateinit var buttonSave: Button

    @BindView(R.id.button_execute) lateinit var buttonExecute: Button

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.task_editor_backup_activity)
        ButterKnife.bind(this)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            vdc.updateCurrent(destination.id)
        }

        vdc.state.observe(this, Observer { state ->
            supportActionBar!!.title = when (state.taskType) {
                Task.Type.BACKUP_SIMPLE -> {
                    if (state.isExistingTask) getString(R.string.label_edit_backup_task)
                    else getString(R.string.label_new_backup_task)
                }
                Task.Type.RESTORE_SIMPLE -> {
                    if (state.isExistingTask) getString(R.string.label_edit_restore_task)
                    else getString(R.string.label_new_restore_task)
                }
            }

            if (!navController.isGraphSet()) {
                val graph = navController.navInflater.inflate(R.navigation.task_editor)
                graph.startDestination = state.stepFlow.start
                navController.setGraph(graph, Bundle().apply { putTaskId(state.taskId) })
                setupActionBarWithNavController(navController)
            }

            buttonNext.setGone(!navController.currentDestination.hasAction(R.id.next))
            buttonNext.clicksDebounced().subscribe {
                navController.navigate(R.id.next, Bundle().apply { putTaskId(state.taskId) })
            }

            buttonSave.setGone(navController.currentDestination.hasAction(R.id.next) || state.isOneTimeTask)
            buttonSave.isEnabled = state.isValid

            buttonExecute.setGone(navController.currentDestination.hasAction(R.id.next))
            buttonExecute.isEnabled = state.isValid
        })

        buttonCancel.clicksDebounced().subscribe { finish() }

        buttonSave.clicksDebounced().subscribe { vdc.save() }
        buttonExecute.clicksDebounced().subscribe { vdc.save(execute = true) }

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
