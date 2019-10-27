package eu.darken.bb.common.file.picker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import eu.darken.bb.common.observe2
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject

class APathPickerActivity : AppCompatActivity() {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: APathPickerActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as APathPickerActivityVDC.Factory
        factory.create(handle, APathPicker.fromIntent(intent))
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        vdc.state.observe2(this) { state ->

        }

        vdc.launchPicker.observe2(this) { intent ->
            startActivityForResult(intent, 47)
        }

        vdc.showOptions.observe2(this) {
            TODO()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            47 -> {
                vdc.onPickerResult()
            }
            else -> throw IllegalArgumentException("Unknown activity result: code=$requestCode, resultCode=$resultCode, data=$data")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}