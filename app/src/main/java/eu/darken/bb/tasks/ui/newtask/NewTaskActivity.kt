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
import eu.darken.bb.common.vdcs
import javax.inject.Inject


class NewTaskActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: NewTaskActivityVDC by vdcs { vdcSource }

    @BindView(R.id.button_previous) lateinit var buttonPrevious: Button
    @BindView(R.id.button_next) lateinit var buttonNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.newtask_activity)
        ButterKnife.bind(this)

        vdc.state.observe(this, Observer { state ->
            buttonPrevious.visibility = if (state.allowPrevious) View.VISIBLE else View.INVISIBLE
            buttonNext.isEnabled = state.allowNext
            buttonNext.setText(if (state.step == NewTaskActivityVDC.State.Step.DESTINATIONS) R.string.button_create else R.string.button_next)
            showStep(state.step)
        })

        buttonPrevious.setOnClickListener { vdc.changeStep(-1) }
        buttonNext.setOnClickListener { vdc.changeStep(+1) }
    }

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    private fun showStep(step: NewTaskActivityVDC.State.Step) {
        var fragment = supportFragmentManager.findFragmentById(R.id.content_frame)
        if (step.fragmentClass.isInstance(fragment)) return

        fragment = supportFragmentManager.findFragmentByTag(step.name)
        if (fragment == null) {
            fragment = supportFragmentManager.fragmentFactory.instantiate(this.classLoader, step.fragmentClass.qualifiedName!!)
        }

        supportFragmentManager.beginTransaction().replace(R.id.content_frame, fragment, step.name).commitAllowingStateLoss()
    }
}
