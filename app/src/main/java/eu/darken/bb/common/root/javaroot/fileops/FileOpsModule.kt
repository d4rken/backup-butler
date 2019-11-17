package eu.darken.bb.common.root.javaroot.fileops

import timber.log.Timber
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class FileOpsModule : FileOps.Stub() {

    override fun readFile(path: String?): RemoteInputStream? = try {
        FileInputStream(path).toRemoteInputStream()
    } catch (e: IOException) {
        Timber.e(e)
        null
    }

    override fun writeFile(path: String?): RemoteOutputStream? = try {
        FileOutputStream(path).toRemoteOutputStream()
    } catch (e: IOException) {
        Timber.e(e)
        null
    }

}