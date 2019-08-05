package eu.darken.bb.backups.ui.generator.editor

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.BindView
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.backups.core.getGeneratorId
import eu.darken.bb.backups.core.putGeneratorId
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.vdcsAssisted
import java.util.*
import javax.inject.Inject

class GeneratorEditorActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdc: GeneratorEditorActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as GeneratorEditorActivityVDC.Factory
        factory.create(handle, intent.getGeneratorId()!!)
    })

    @BindView(R.id.working_indicator) lateinit var workingIndicator: ViewGroup

    private var allowCreate: Boolean = false
    private var existing: Boolean = false

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.generator_editor_activity)
        ButterKnife.bind(this)

        vdc.state.observe(this, Observer { state ->
            if (state.existing) {
                supportActionBar!!.title = getString(R.string.label_edit_source_config)
            } else {
                supportActionBar!!.title = getString(R.string.label_create_source_config)
            }
            supportActionBar!!.subtitle = when (state.page) {
                GeneratorEditorActivityVDC.State.Page.SELECTION -> getString(R.string.label_select_type)
                GeneratorEditorActivityVDC.State.Page.APP -> getString(R.string.backuptype_app_label)
                GeneratorEditorActivityVDC.State.Page.FILES -> getString(R.string.backuptype_files_label)
            }

            allowCreate = state.allowSave
            existing = state.existing

            workingIndicator.visibility = if (state.working) View.VISIBLE else View.GONE

            showPage(state.page, state.configId)
        })

        vdc.finishActivity.observe(this, Observer { finish() })
    }


    private fun showPage(page: GeneratorEditorActivityVDC.State.Page, configId: UUID) {
        var fragment = supportFragmentManager.findFragmentById(R.id.content_frame)
        if (page.fragmentClass.isInstance(fragment)) return

        fragment = supportFragmentManager.findFragmentByTag(page.name)
        if (fragment == null) {
            fragment = supportFragmentManager.fragmentFactory.instantiate(this.classLoader, page.fragmentClass.qualifiedName!!)
        }
        fragment.arguments = Bundle().apply { putGeneratorId(configId) }
        supportFragmentManager.beginTransaction().replace(R.id.content_frame, fragment, page.name).commitAllowingStateLoss()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_generator_editor, menu)
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
