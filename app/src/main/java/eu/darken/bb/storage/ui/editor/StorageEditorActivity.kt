package eu.darken.bb.storage.ui.editor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.vdcsAssisted
import eu.darken.bb.storage.core.getStorageId
import eu.darken.bb.storage.core.putStorageId
import java.util.*
import javax.inject.Inject

class StorageEditorActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdc: StorageEditorActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as StorageEditorActivityVDC.Factory
        factory.create(handle, intent.getStorageId()!!)
    })


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.newtask_activity)
        ButterKnife.bind(this)

        vdc.state.observe(this, Observer { state ->
            if (state.existing) {
                supportActionBar!!.title = getString(R.string.label_edit_storage)
            } else {
                supportActionBar!!.title = getString(R.string.label_create_storage)
            }
            supportActionBar!!.subtitle = when (state.page) {
                StorageEditorActivityVDC.State.Page.SELECTION -> getString(R.string.label_storage_selection)
                StorageEditorActivityVDC.State.Page.LOCAL -> getString(R.string.repo_type_local_storage_label)
            }

            showPage(state.page, state.storageId)
        })

        vdc.finishActivity.observe(this, Observer { finish() })
    }


    private fun showPage(page: StorageEditorActivityVDC.State.Page, storageId: UUID) {
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
