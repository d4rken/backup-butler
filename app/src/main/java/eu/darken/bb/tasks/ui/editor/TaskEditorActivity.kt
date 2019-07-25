package eu.darken.bb.tasks.ui.editor

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
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.vdcsAssisted
import eu.darken.bb.tasks.core.getTaskId
import eu.darken.bb.tasks.core.putTaskId
import java.util.*
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

        setContentView(R.layout.newtask_activity)
        ButterKnife.bind(this)

        vdcEditor.state.observe(this, Observer { state ->

            supportActionBar!!.title = if (state.existingTask) {
                getString(R.string.label_edit_task)
            } else {
                getString(R.string.label_new_task)
            }

        })
        vdcEditor.task.observe(this, Observer {
            supportActionBar!!.subtitle = it.taskName
        })

        vdcEditor.steps.observe(this, Observer { (state, task) ->
            buttonPrevious.setText(if (state.step == TaskEditorActivityVDC.State.Step.INTRO) R.string.button_cancel else R.string.button_previous)
            buttonPrevious.visibility = View.VISIBLE

            buttonNext.isEnabled = state.allowNext || state.saveable
            val nextLabel = if (state.step == TaskEditorActivityVDC.State.Step.DESTINATIONS) {
                if (state.existingTask) R.string.button_save else R.string.button_create
            } else {
                R.string.button_next
            }
            buttonNext.setText(nextLabel)
            buttonNext.visibility = View.VISIBLE

            showStep(state.step, task.taskId)
        })

        buttonPrevious.clicksDebounced().subscribe { vdcEditor.previous() }
        buttonNext.clicksDebounced().subscribe { vdcEditor.next() }

        vdcEditor.finishActivity.observe(this, Observer { finish() })
    }

    override fun onBackPressed() {
        vdcEditor.previous()
    }

    private fun showStep(step: TaskEditorActivityVDC.State.Step, taskId: UUID) {
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
