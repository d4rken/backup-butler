package eu.darken.bb.common.file

import eu.darken.bb.App
import timber.log.Timber
import java.io.File

val TAG: String = App.logTag("FileExtensions")

fun File.copyTo(file: File) {
    file.tryMkFile()
    inputStream().use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}

fun File.asSFile(): SFile {
    return JavaFile.build(path)
}

fun File.assertExists(): File {
    if (!exists()) {
        val ex = IllegalStateException("Path doesn't exist, but should: $this")
        Timber.tag(TAG).w(ex)
        throw ex
    }
    return this
}

fun File.tryMkDirs(): File {
    if (exists()) {
        if (isDirectory) {
            Timber.tag(TAG).v("Directory already exists, not creating: %s", this)
            return this
        } else {
            val ex = IllegalStateException("Directory exists, but is not a directory: $this")
            Timber.tag(TAG).w(ex)
            throw ex
        }
    }

    if (mkdirs()) {
        Timber.tag(TAG).v("Directory created: %s", this)
        return this
    } else {
        val ex = IllegalStateException("Couldn't create Directory: $this")
        Timber.tag(TAG).w(ex)
        throw ex
    }
}

fun File.tryMkFile(): File {
    if (exists()) {
        if (isFile) {
            Timber.tag(TAG).v("File already exists, not creating: %s", this)
            return this
        } else {
            val ex = IllegalStateException("Path exists but is not a file: $this")
            Timber.tag(TAG).w(ex)
            throw ex
        }
    }

    if (!parentFile.exists()) parentFile.tryMkDirs()

    if (createNewFile()) {
        Timber.tag(TAG).v("File created: %s", this)
        return this
    } else {
        val ex = IllegalStateException("Couldn't create file: $this")
        Timber.tag(TAG).w(ex)
        throw ex
    }
}