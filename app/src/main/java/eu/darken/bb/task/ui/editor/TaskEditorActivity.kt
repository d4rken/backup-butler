package eu.darken.bb.task.ui.editor

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.BindView
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.getTaskId
import eu.darken.bb.task.core.putTaskId
import javax.inject.Inject

class TaskEditorActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdcEditor: TaskEditorActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as TaskEditorActivityVDC.Factory
        factory.create(handle, intent.getTaskId()!!)
    })

    @BindView(R.id.button_cancel) lateinit var buttonCancel: Button
    @BindView(R.id.button_previous) lateinit var buttonPrevious: Button
    @BindView(R.id.button_next) lateinit var buttonNext: Button
    @BindView(R.id.button_save) lateinit var buttonSave: Button
    @BindView(R.id.button_execute) lateinit var buttonExecute: Button

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.task_editor_backup_activity)
        ButterKnife.bind(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        vdcEditor.state.observe(this, Observer { state ->

            supportActionBar!!.title = when (state.taskType) {
                Task.Type.BACKUP_SIMPLE -> {
                    if (state.existingTask) getString(R.string.label_edit_backup_task)
                    else getString(R.string.label_new_backup_task)
                }
                Task.Type.RESTORE_SIMPLE -> {
                    if (state.existingTask) getString(R.string.label_edit_restore_task)
                    else getString(R.string.label_new_restore_task)
                }
            }

            buttonPrevious.setGone(state.stepPos == 0)
            buttonNext.setGone(state.stepPos == state.steps.size - 1)

            buttonSave.setGone(state.stepPos != state.steps.size - 1)
            buttonSave.isEnabled = state.isComplete

            buttonExecute.setGone(state.stepPos != state.steps.size - 1)
            buttonExecute.isEnabled = state.isComplete

            showStep(state.steps[state.stepPos], state.taskId)
        })

        buttonCancel.clicksDebounced().subscribe { vdcEditor.cancel() }
        buttonPrevious.clicksDebounced().subscribe { vdcEditor.previous() }
        buttonNext.clicksDebounced().subscribe { vdcEditor.next() }
        buttonSave.clicksDebounced().subscribe { vdcEditor.save() }
        buttonExecute.clicksDebounced().subscribe { vdcEditor.execute() }

        vdcEditor.finishActivity.observe(this, Observer { finish() })

    }

    override fun onBackPressed() {
        vdcEditor.previous()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showStep(step: TaskEditorActivityVDC.State.Step, taskId: Task.Id) {
        var fragment = supportFragmentManager.findFragmentById(R.id.content_frame)
        if (step.fragmentClass.isInstance(fragment)) return

        fragment = supportFragmentManager.findFragmentByTag(step.name)
        if (fragment == null) {
            fragment = supportFragmentManager.fragmentFactory.instantiate(this.classLoader, step.fragmentClass.qualifiedName!!)
        }
        fragment.arguments = Bundle().apply { putTaskId(taskId) }
        supportFragmentManager.beginTransaction().replace(R.id.content_frame, fragment, step.name).commitAllowingStateLoss()
    }
}
