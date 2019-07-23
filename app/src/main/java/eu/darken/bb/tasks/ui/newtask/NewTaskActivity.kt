package eu.darken.bb.tasks.ui.newtask

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

class NewTaskActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdc: NewTaskActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as NewTaskActivityVDC.Factory
        factory.create(handle, intent.getTaskId() ?: UUID.randomUUID())
    })

    @BindView(R.id.button_previous) lateinit var buttonPrevious: Button
    @BindView(R.id.button_next) lateinit var buttonNext: Button

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.newtask_activity)
        ButterKnife.bind(this)

        supportActionBar!!.title = getString(R.string.label_new_task)

        vdc.state.observe(this, Observer { state ->

            buttonPrevious.setText(if (state.step == NewTaskActivityVDC.State.Step.INTRO) R.string.button_cancel else R.string.button_previous)

            buttonNext.isEnabled = state.allowNext || state.creatable

            buttonNext.setText(if (state.step == NewTaskActivityVDC.State.Step.DESTINATIONS) R.string.button_create else R.string.button_next)
        })
        vdc.task.observe(this, Observer {
            supportActionBar!!.subtitle = it.taskName
        })

        vdc.steps.observe(this, Observer { (state, task) ->
            buttonPrevious.visibility = View.VISIBLE
            buttonNext.visibility = View.VISIBLE
            showStep(state.step, task.taskId)
        })

        buttonPrevious.clicksDebounced().subscribe { vdc.previous() }
        buttonNext.clicksDebounced().subscribe { vdc.next() }

        vdc.finishActivity.observe(this, Observer { finish() })
    }

    override fun onBackPressed() {
        vdc.previous()
    }

    private fun showStep(step: NewTaskActivityVDC.State.Step, taskId: UUID) {
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
