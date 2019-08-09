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
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.getStorageId
import eu.darken.bb.storage.core.putStorageId
import javax.inject.Inject

class StorageViewerActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdc: StorageViewerActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
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

            showPage(state.page, state.storageId)

            if (state.error != null) {
                Toast.makeText(this, state.error.tryLocalizedErrorMessage(this), Toast.LENGTH_LONG).show()
            }
        })

        vdc.finishActivity.observe(this, Observer { finish() })
    }


    private fun showPage(page: StorageViewerActivityVDC.State.Page, storageId: Storage.Id) {
        var fragment = supportFragmentManager.findFragmentById(R.id.content_frame)
        if (page.fragmentClass.isInstance(fragment)) return

        fragment = supportFragmentManager.findFragmentByTag(page.name)
        if (fragment == null) {
            fragment = supportFragmentManager.fragmentFactory.instantiate(this.classLoader, page.fragmentClass.qualifiedName!!)
        }
        fragment.arguments = Bundle().apply { putStorageId(storageId) }
        supportFragmentManager.beginTransaction().replace(R.id.content_frame, fragment, page.name).commitAllowingStateLoss()
    }

}
