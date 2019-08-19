package eu.darken.bb.task.ui.editor.restore

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.getTaskId
import eu.darken.bb.task.core.putTaskId
import javax.inject.Inject

class RestoreTaskActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: RestoreTaskActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as RestoreTaskActivityVDC.Factory
        factory.create(handle, intent.getTaskId()!!)
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.task_editor_restore_activity)
        ButterKnife.bind(this)

        vdc.state.observe(this, Observer { state ->
            supportActionBar!!.title = if (state.existingTask) {
                getString(R.string.label_edit_restore_task)
            } else {
                getString(R.string.label_new_restore_task)
            }

            showStep(state.step, state.taskId)
        })

        vdc.finishActivity.observe(this, Observer { finish() })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_cancel)
    }

    override fun onBackPressed() {
        vdc.dismiss()
    }

    private fun showStep(step: RestoreTaskActivityVDC.State.Step, taskId: Task.Id) {
        var fragment = supportFragmentManager.findFragmentById(R.id.content_frame)
        if (step.fragmentClass.isInstance(fragment)) return

        fragment = supportFragmentManager.findFragmentByTag(step.name)
        if (fragment == null) {
            fragment = supportFragmentManager.fragmentFactory.instantiate(this.classLoader, step.fragmentClass.qualifiedName!!)
        }
        fragment.arguments = Bundle().apply { putTaskId(taskId) }
        supportFragmentManager.beginTransaction().replace(R.id.content_frame, fragment, step.name).commitAllowingStateLoss()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> vdc.dismiss()
        else -> super.onOptionsItemSelected(item)
    }

}
