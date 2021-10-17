package eu.darken.bb.storage.ui.editor

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class StorageEditorContract : ActivityResultContract<StorageEditorRequest, StorageEditorResult?>() {

    override fun createIntent(
        context: Context,
        input: StorageEditorRequest
    ): Intent = Intent(context, StorageEditorActivity::class.java).apply {
        val args = StorageEditorActivityArgs(storageId = input.storageId)
        putExtras(args.toBundle())
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): StorageEditorResult? = intent?.getParcelableExtra(RESULT_KEY)

    companion object {
        const val REQUEST_KEY = "StorageEditorRequest"
        const val RESULT_KEY = "StorageEditorResult"
    }
}