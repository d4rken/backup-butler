package eu.darken.bb.backup.ui.generator.editor.types.files

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.backup.core.getGeneratorId
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.setTextIfDifferent
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject


class FilesEditorFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: FilesEditorFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as FilesEditorFragmentVDC.Factory
        factory.create(handle, arguments!!.getGeneratorId()!!)
    })

    @BindView(R.id.name_input) lateinit var labelInput: EditText
    @BindView(R.id.core_settings_container) lateinit var coreSettingsContainer: ViewGroup
    @BindView(R.id.core_settings_progress) lateinit var coreSettingsProgress: View
    @BindView(R.id.options_container) lateinit var optionsContainer: ViewGroup
    @BindView(R.id.options_progress) lateinit var optionsProgress: View

    init {
        layoutRes = R.layout.generator_editor_file_fragment
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
        requireActivityActionBar().subtitle = getString(R.string.backuptype_files_label)
        vdc.state.observe(this, Observer { state ->
            labelInput.setTextIfDifferent(state.label)

            requireActivity().invalidateOptionsMenu()

            requireActivityActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            requireActivityActionBar().setDisplayHomeAsUpEnabled(!state.existing)

            coreSettingsContainer.visibility = if (state.working) View.INVISIBLE else View.VISIBLE
            coreSettingsProgress.visibility = if (state.working) View.VISIBLE else View.INVISIBLE
            optionsContainer.visibility = if (state.working) View.INVISIBLE else View.VISIBLE
            optionsProgress.visibility = if (state.working) View.VISIBLE else View.INVISIBLE
        })

        vdc.finishActivity.observe(this, Observer { requireActivity().finish() })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> vdc.onGoBack()
        else -> super.onOptionsItemSelected(item)
    }

}
