package eu.darken.bb.common.file.picker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import dagger.android.AndroidInjection
import eu.darken.bb.R
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartActivity
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.ui.editor.types.TypeSelectionAdapter
import javax.inject.Inject

class APathPickerActivity : SmartActivity() {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: APathPickerActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as APathPickerActivityVDC.Factory
        factory.create(handle, APathPicker.fromIntent(intent))
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED, APathPicker.toActivityResult(APathPicker.Result(
                options = APathPicker.fromIntent(intent)
        )))

        vdc.state.observe2(this) { state ->
            if (state.showPickList) {
                setContentView(R.layout.picker_activity)
                val list = findViewById<RecyclerView>(R.id.picktype_list)
                val adapter = TypeSelectionAdapter()
                adapter.data.addAll(state.pickTypes)
                adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.onTypeSelected(adapter.data[i]) })
                list.setupDefaults(adapter)
            }
        }

        vdc.launchPickerEvent.observe2(this) { (intent, type) ->
            startActivityForResult(intent, type.ordinal + 10)
        }

        vdc.finishEvent.observe2(this) { result ->
            val resultCode = when {
                result.isSuccess -> Activity.RESULT_OK
                else -> Activity.RESULT_CANCELED
            }
            val data: Intent = APathPicker.toActivityResult(result)
            setResult(resultCode, data)
            finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data?.data != null) {
            when (APath.Type.values()[requestCode - 10]) {
                APath.Type.SAF -> {
                    vdc.onSAFPickerResult(data.data!!)
                }
                else -> throw NotImplementedError()
            }
        } else {
            vdc.onEmptyResult()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}