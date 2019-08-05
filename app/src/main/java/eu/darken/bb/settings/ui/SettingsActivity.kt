package eu.darken.bb.settings.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.vdcs
import javax.inject.Inject

class SettingsActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: SettingsActivityVDC by vdcs { vdcSource }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.settings_activity)
        ButterKnife.bind(this)


    }

//    private fun showStep(step: TaskEditorActivityVDC.State.Step, taskId: UUID) {
//        var fragment = supportFragmentManager.findFragmentById(R.id.content_frame)
//        if (step.fragmentClass.isInstance(fragment)) return
//
//        fragment = supportFragmentManager.findFragmentByTag(step.name)
//        if (fragment == null) {
//            fragment = supportFragmentManager.fragmentFactory.instantiate(this.classLoader, step.fragmentClass.qualifiedName!!)
//        }
//        fragment.arguments = Bundle().apply { putTaskId(taskId) }
//        supportFragmentManager.beginTransaction().replace(R.id.content_frame, fragment, step.name).commitAllowingStateLoss()
//    }
}
