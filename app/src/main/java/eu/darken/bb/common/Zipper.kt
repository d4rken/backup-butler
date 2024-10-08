package eu.darken.bb.common

import eu.darken.bb.common.debug.logging.logTag
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// https://stackoverflow.com/a/48598099/1251958
class Zipper {

    @Throws(Exception::class)
    fun zip(files: Array<String>, zipFile: String) {

        var origin: BufferedInputStream?
        val out = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))

        for (i in files.indices) {
            Timber.tag(TAG).v("Compressing ${files[i]} into $zipFile")
            origin = BufferedInputStream(FileInputStream(files[i]), BUFFER)

            val entry = ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1))
            out.putNextEntry(entry)

            origin.use { input -> input.copyTo(out) }
        }

        out.finish()
        out.close()
    }

    companion object {
        internal val TAG = logTag("Zipper")
        const val BUFFER = 2048
    }
}