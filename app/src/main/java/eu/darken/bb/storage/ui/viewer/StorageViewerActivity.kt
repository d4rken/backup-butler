package eu.darken.bb.storage.ui.viewer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.common.vdcsAssisted
import eu.darken.bb.storage.core.getStorageId
import eu.darken.bb.storage.core.putBackupSpecId
import eu.darken.bb.storage.core.putStorageId
import javax.inject.Inject

class StorageViewerActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory

    val vdc: StorageViewerActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as StorageViewerActivityVDC.Factory
        factory.create(handle, intent.getStorageId()!!)
    })

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.storage_viewer_activity)
        ButterKnife.bind(this)

        vdc.state.observe(this, Observer { state ->
            supportActionBar?.subtitle = state.label

            if (state.error != null) {
                Toast.makeText(this, state.error.tryLocalizedErrorMessage(this), Toast.LENGTH_LONG).show()
            }
        })

        vdc.pageEvent.observe(this, Observer { pageData ->
            showPage(pageData)
        })

        vdc.finishActivity.observe(this, Observer { finish() })
    }

    private fun showPage(pageData: StorageViewerActivityVDC.PageData) {
        val current = supportFragmentManager.findFragmentById(R.id.content_frame)

        val desired = supportFragmentManager.findFragmentByTag(pageData.page.name)
        var newFragment = desired
        if (newFragment == null) {
            newFragment = supportFragmentManager.fragmentFactory.instantiate(this.classLoader, pageData.page.fragmentClass.qualifiedName!!)
        }
        newFragment.arguments = Bundle().apply {
            putStorageId(pageData.storageId)
            if (pageData.backupSpecId != null) putBackupSpecId(pageData.backupSpecId)
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content_frame, newFragment, pageData.toString())
        if (current != null) transaction.addToBackStack(null)
        transaction.commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
