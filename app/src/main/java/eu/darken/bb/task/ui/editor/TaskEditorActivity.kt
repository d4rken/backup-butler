package eu.darken.bb.task.ui.editor

import android.os.Bundle
import android.view.View
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

    @BindView(R.id.button_previous) lateinit var buttonPrevious: Button
    @BindView(R.id.button_next) lateinit var buttonNext: Button

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.task_editor_activity)
        ButterKnife.bind(this)

        vdcEditor.state.observe(this, Observer { state ->

            supportActionBar!!.title = if (state.existingTask) {
                getString(R.string.label_edit_task)
            } else {
                getString(R.string.label_new_task)
            }

            buttonPrevious.setText(if (state.step == TaskEditorActivityVDC.State.Step.INTRO) R.string.action_cancel else R.string.action_previous)
            buttonPrevious.visibility = View.VISIBLE

            buttonNext.isEnabled = state.allowNext || state.saveable
            val nextLabel = if (state.step == TaskEditorActivityVDC.State.Step.DESTINATIONS) {
                if (state.existingTask) R.string.action_save else R.string.action_create
            } else {
                R.string.action_next
            }
            buttonNext.setText(nextLabel)
            buttonNext.visibility = View.VISIBLE

            showStep(state.step, state.taskId)
        })

        buttonPrevious.clicksDebounced().subscribe { vdcEditor.previous() }
        buttonNext.clicksDebounced().subscribe { vdcEditor.next() }

        vdcEditor.finishActivity.observe(this, Observer { finish() })
    }

    override fun onBackPressed() {
        vdcEditor.previous()
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
