package eu.darken.bb.common.file.picker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.SimplePath
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

object APathPicker {

    @Parcelize
    data class Options(
            val startPath: APath? = null,
            val selectionLimit: Int = 1,
            val payload: Bundle = Bundle()
    ) : Parcelable {
        @IgnoredOnParcel @Transient val type: APath.Type? = startPath?.pathType

    }

    fun createIntent(
            context: Context,
            options: Options = Options()
    ): Intent {
        val intent = Intent(context, APathPickerActivity::class.java)

        return intoIntent(intent, options)
    }

    data class Result(
            val aborted: Boolean = true,
            val error: Throwable? = null,
            val path: APath? = null,
            val payload: Bundle = Bundle()
    ) {

        companion object {
            // TODO remove test values
            val ABORT = Result(
                    aborted = true,
                    path = SimplePath.build(UUID.randomUUID().toString()),
                    payload = Bundle().apply { putParcelable("backupId", Backup.Id("f504c6ae-1f18-498f-bad2-ef738f0bdb84")) }
            )
        }
    }

    fun fromActivityResult(resultCode: Int, data: Intent?): Result {
        if (resultCode != Activity.RESULT_OK) return Result.ABORT
        TODO("not implemented")
    }

    fun fromIntent(intent: Intent): Options {
        return intent.getParcelableExtra(ARG_PICKER_OPTIONS)
    }

    fun intoIntent(intent: Intent, options: Options): Intent {
        return intent.putExtra(ARG_PICKER_OPTIONS, options)
    }

    private const val ARG_PICKER_OPTIONS = "eu.darken.bb.common.file.picker.APathPicker.Options"
}