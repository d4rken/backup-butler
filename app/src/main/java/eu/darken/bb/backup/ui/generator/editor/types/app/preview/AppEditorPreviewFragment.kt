package eu.darken.bb.backup.ui.generator.editor.types.app.preview

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject


class AppEditorPreviewFragment : SmartFragment(), AutoInject {

    val args by navArgs<AppEditorPreviewFragmentArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: AppEditorPreviewFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as AppEditorPreviewFragmentVDC.Factory
        factory.create(handle, args.generatorId, args.previewMode)
    })


//    @BindView(R.id.name_input) lateinit var labelInput: EditText


    init {
        layoutRes = R.layout.generator_editor_app_preview_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().subtitle = getString(R.string.backuptype_app_label)

        vdc.state.observe(this, Observer { state ->

        })

        super.onViewCreated(view, savedInstanceState)
    }


}
