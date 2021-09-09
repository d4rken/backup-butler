package eu.darken.bb.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

@Singleton
class ClipboardHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val clipboard: ClipboardManager by lazy {
        return@lazy if (Looper.getMainLooper() == Looper.myLooper()) {
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        } else {
            // java.lang.RuntimeException · Can't create handler inside thread that has not called Looper.prepare()
            Timber.w("Clipboard is not initialized on the main thread, applying workaround")
            val lock = ReentrantLock()
            val lockCondition = lock.newCondition()

            var clipboardManager: ClipboardManager? = null

            Handler(Looper.getMainLooper()).postAtFrontOfQueue {
                clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                lock.withLock { lockCondition.signal() }
            }

            lock.withLock { lockCondition.await() }

            clipboardManager!!
        }
    }

    fun copyToClipboard(text: String) {
        val clip = ClipData.newPlainText("Backup Butler", text)
        clipboard.setPrimaryClip(clip)
    }
}
