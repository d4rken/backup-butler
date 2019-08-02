package eu.darken.bb.backups.ui.editor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.backups.core.getConfigId
import eu.darken.bb.backups.core.putConfigId
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.vdcsAssisted
import java.util.*
import javax.inject.Inject

class BackupEditorActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdc: BackupEditorActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as BackupEditorActivityVDC.Factory
        factory.create(handle, intent.getConfigId()!!)
    })

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.backupconfig_editor_activity)
        ButterKnife.bind(this)

        vdc.state.observe(this, Observer { state ->
            if (state.existing) {
                supportActionBar!!.title = getString(R.string.label_edit_backupconfig)
            } else {
                supportActionBar!!.title = getString(R.string.label_create_backupconfig)
            }
            supportActionBar!!.subtitle = when (state.page) {
                BackupEditorActivityVDC.State.Page.SELECTION -> getString(R.string.label_backuptype_selection)
                BackupEditorActivityVDC.State.Page.APP -> getString(R.string.backuptype_app_label)
            }

            showPage(state.page, state.configId)
        })

        vdc.finishActivity.observe(this, Observer { finish() })
    }


    private fun showPage(page: BackupEditorActivityVDC.State.Page, configId: UUID) {
        var fragment = supportFragmentManager.findFragmentById(R.id.content_frame)
        if (page.fragmentClass.isInstance(fragment)) return

        fragment = supportFragmentManager.findFragmentByTag(page.name)
        if (fragment == null) {
            fragment = supportFragmentManager.fragmentFactory.instantiate(this.classLoader, page.fragmentClass.qualifiedName!!)
        }
        fragment.arguments = Bundle().apply { putConfigId(configId) }
        supportFragmentManager.beginTransaction().replace(R.id.content_frame, fragment, page.name).commitAllowingStateLoss()
    }

}
