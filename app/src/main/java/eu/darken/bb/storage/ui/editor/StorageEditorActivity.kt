package eu.darken.bb.storage.ui.editor

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.BindView
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.showFragment
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.core.getStorageId
import eu.darken.bb.storage.core.putStorageId
import javax.inject.Inject

class StorageEditorActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: StorageEditorActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as StorageEditorActivityVDC.Factory
        factory.create(handle, intent.getStorageId()!!)
    })

    private var allowCreate: Boolean = false
    private var existing: Boolean = false

    @BindView(R.id.loading_overlay) lateinit var loadingOverlay: LoadingOverlayView

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.storage_editor_activity)
        ButterKnife.bind(this)

        vdc.state.observe(this, Observer { state ->
            if (state.existing) {
                supportActionBar!!.title = getString(R.string.label_edit_storage)
            } else {
                supportActionBar!!.title = getString(R.string.label_create_storage)
            }

            allowCreate = state.allowSave
            existing = state.existing
            loadingOverlay.setGone(!state.isWorking)
            invalidateOptionsMenu()
        })

        vdc.pageEvent.observe(this, Observer { pageEvent ->
            showFragment(
                    fragmentClass = pageEvent.getPage().fragmentClass,
                    arguments = Bundle().apply { putStorageId(pageEvent.storageId) }
            )
        })

        vdc.finishActivityEvent.observe(this, Observer { finish() })
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (supportFragmentManager.popBackStackImmediate()) {
            true
        } else {
            finish()
            super.onSupportNavigateUp()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_storage_editor_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_create).isVisible = allowCreate
        menu.findItem(R.id.action_create).title = getString(if (existing) R.string.action_save else R.string.action_create)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_create -> {
            vdc.saveConfig()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}
