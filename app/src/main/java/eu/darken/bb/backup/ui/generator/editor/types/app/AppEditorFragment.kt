package eu.darken.bb.backup.ui.generator.editor.types.app

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.BindView
import com.jakewharton.rxbinding3.widget.editorActions
import eu.darken.bb.R
import eu.darken.bb.backup.core.getGeneratorId
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.setTextIfDifferent
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.vdcsAssisted
import javax.inject.Inject


class AppEditorFragment : SmartFragment(), AutoInject {
    companion object {
        fun newInstance(): Fragment = AppEditorFragment()
    }

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: AppEditorFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as AppEditorFragmentVDC.Factory
        factory.create(handle, arguments!!.getGeneratorId()!!)
    })


    @BindView(R.id.name_input) lateinit var labelInput: EditText
    @BindView(R.id.input_include_packages) lateinit var includedPackagesInput: EditText
    @BindView(R.id.core_settings_container) lateinit var coreSettingsContainer: ViewGroup
    @BindView(R.id.core_settings_progress) lateinit var coreSettingsProgress: View
    @BindView(R.id.options_container) lateinit var optionsContainer: ViewGroup
    @BindView(R.id.options_progress) lateinit var optionsProgress: View

    private var allowCreate = false
    private var isExisting = false

    init {
        layoutRes = R.layout.generator_editor_app_fragment
    }

    override fun onAttach(context: Context) {
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                vdc.onGoBack()
            }
        })
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe(this, Observer { state ->
            labelInput.setTextIfDifferent(state.label)

            allowCreate = state.allowCreate
            isExisting = state.existing
            requireActivity().invalidateOptionsMenu()

            requireActivityActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            requireActivityActionBar().setDisplayHomeAsUpEnabled(!state.existing)

            coreSettingsContainer.visibility = if (state.working) View.INVISIBLE else View.VISIBLE
            coreSettingsProgress.visibility = if (state.working) View.VISIBLE else View.INVISIBLE
            optionsContainer.visibility = if (state.working) View.INVISIBLE else View.VISIBLE
            optionsProgress.visibility = if (state.working) View.VISIBLE else View.INVISIBLE

            includedPackagesInput.setTextIfDifferent(state.includedPackages.joinToString(","))
        })

        vdc.finishActivity.observe(this, Observer { requireActivity().finish() })

        labelInput.userTextChangeEvents().subscribe { vdc.updateLabel(it.text.toString()) }
        labelInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { labelInput.clearFocus() }

        includedPackagesInput.userTextChangeEvents().subscribe {
            vdc.updateIncludedPackages(it.text.split(','))
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> vdc.onGoBack()
        else -> super.onOptionsItemSelected(item)
    }
}
