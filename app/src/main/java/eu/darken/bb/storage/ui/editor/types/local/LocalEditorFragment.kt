package eu.darken.bb.storage.ui.editor.types.local

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
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.setTextIfDifferent
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.core.getStorageId
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject


class LocalEditorFragment : SmartFragment(), AutoInject {
    companion object {
        fun newInstance(): Fragment = LocalEditorFragment()
    }

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: LocalEditorFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as LocalEditorFragmentVDC.Factory
        factory.create(handle, arguments!!.getStorageId()!!)
    })

    @Inject lateinit var adapter: StorageAdapter

    @BindView(R.id.path_input) lateinit var pathInput: EditText
    @BindView(R.id.path_input_layout) lateinit var pathInputLayout: ViewGroup
    @BindView(R.id.name_input) lateinit var labelInput: EditText
    @BindView(R.id.core_settings_container) lateinit var coreSettingsContainer: ViewGroup
    @BindView(R.id.core_settings_progress) lateinit var coreSettingsProgress: View
    @BindView(R.id.options_container) lateinit var optionsContainer: ViewGroup
    @BindView(R.id.options_progress) lateinit var optionsProgress: View

    init {
        layoutRes = R.layout.storage_editor_local_fragment
    }

    override fun onAttach(context: Context) {
        setHasOptionsMenu(true)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                vdc.onGoBack()
            }
        })
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().setDisplayHomeAsUpEnabled(true)
        requireActivityActionBar().subtitle = getString(R.string.repo_type_local_storage_label)

        vdc.state.observe(this, Observer { state ->
            pathInput.setTextIfDifferent(state.path)
            labelInput.setTextIfDifferent(state.label)

            if (state.path.isNotEmpty()) {
                pathInputLayout.isEnabled = !state.existing
                pathInput.setTextColor(getColorForAttr(if (state.validPath || state.existing) R.attr.colorOnPrimarySurface else R.attr.colorError))
            }

            requireActivityActionBar().setHomeAsUpIndicator(if (state.existing) R.drawable.ic_cancel else R.drawable.ic_arrow_back)
            requireActivity().invalidateOptionsMenu()

            coreSettingsContainer.visibility = if (state.working) View.INVISIBLE else View.VISIBLE
            coreSettingsProgress.visibility = if (state.working) View.VISIBLE else View.INVISIBLE
            optionsContainer.visibility = if (state.working) View.INVISIBLE else View.VISIBLE
            optionsProgress.visibility = if (state.working) View.VISIBLE else View.INVISIBLE
        })

        vdc.finishActivity.observe(this, Observer { requireActivity().finish() })

        pathInput.userTextChangeEvents().subscribe { vdc.updatePath(it.text.toString()) }
        pathInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { pathInput.clearFocus() }
        labelInput.userTextChangeEvents().subscribe { vdc.updateName(it.text.toString()) }
        labelInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { labelInput.clearFocus() }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> vdc.onGoBack()
        else -> super.onOptionsItemSelected(item)
    }
}
