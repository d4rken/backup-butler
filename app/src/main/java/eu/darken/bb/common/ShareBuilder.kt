package eu.darken.bb.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import eu.darken.bb.App
import eu.darken.bb.BuildConfig
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.asFile
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ShareBuilder @Inject constructor(
    @AppContext private val context: Context
) {
    private var useChooser: Boolean = false
    private var chooserTitle: String? = null
    private val files = ArrayList<APath>()
    private var extraSubject: String? = null
    private var extraText: String? = null
    private val emails = ArrayList<String>()

    fun create(): Intent {
        val intent: Intent
        if (emails.size > 0) {
            intent = Intent(Intent.ACTION_SEND)
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_EMAIL, emails.toTypedArray())
        } else if (files.size > 0) {
            intent = Intent(if (files.size == 1) Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE)

            val uris = addAccessibleFiles(context, intent, files)

            if (files.size == 1) {
                intent.putExtra(Intent.EXTRA_STREAM, uris.get(0))
            } else {
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            }

            intent.type = MimeHelper.getMime(files)
        } else {
            intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
        }

        intent.addCategory(Intent.CATEGORY_DEFAULT)

        intent.putExtra(Intent.EXTRA_SUBJECT, extraSubject)
        intent.putExtra(Intent.EXTRA_TEXT, extraText)

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return if (useChooser) Intent.createChooser(intent, chooserTitle)
        else intent
    }

    fun start() {
        try {
            val intent = create()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e)
        }
    }

    fun subject(@StringRes subjectRes: Int): ShareBuilder {
        return subject(context.getString(subjectRes))
    }

    fun subject(subject: String) = apply {
        extraSubject = subject
    }

    fun text(text: String) = apply {
        extraText = text
    }

    fun text(lines: List<String>): ShareBuilder {
        val formatted = StringBuilder()
        for (s in lines) formatted.append(s).append("\n")
        return text(formatted.toString())
    }

    fun file(path: APath): ShareBuilder {
        return files(setOf(path))
    }

    fun files(paths: Collection<APath>): ShareBuilder {
        if (paths.isEmpty()) throw IllegalArgumentException("Trying to share empty list of files")
        this.files.addAll(paths)
        return this
    }

    fun email(email: String) = apply {
        emails.add(email)
    }

    fun intentChooser(@StringRes title: Int) = apply {
        useChooser = true
        chooserTitle = context.getString(title)
        return this
    }

    private fun addAccessibleFiles(context: Context, intent: Intent, paths: Collection<APath>): ArrayList<Uri> {
        val uris = ArrayList<Uri>()
        for (f in paths) {
            if (ApiHelper.hasAndroidN()) {
                uris.add(FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", f.asFile()))
            } else {
                uris.add(Uri.fromFile(f.asFile()))
            }
        }
        if (uris.isNotEmpty() && ApiHelper.hasAndroidN()) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        return uris
    }

    companion object {
        val TAG = App.logTag("Intents", "ShareBuilder")
    }
}


