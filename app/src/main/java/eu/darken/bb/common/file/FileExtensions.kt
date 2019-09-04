package eu.darken.bb.common.file

import eu.darken.bb.App
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException


val TAG: String = App.logTag("FileExtensions")

fun File.copyTo(file: File) {
    file.tryMkFile()
    inputStream().use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}

@Suppress("FunctionName")
fun File(vararg crumbs: String): File {
    var compacter = File(crumbs[0])
    for (i in 1 until crumbs.size) {
        compacter = File(compacter, crumbs[i])
    }
    return compacter
}

fun File.asSFile(): SFile {
    return JavaFile.build(file = this)
}

fun File.requireExists(): File {
    if (!exists()) {
        val ex = IllegalStateException("Path doesn't exist, but should: $this")
        Timber.tag(TAG).w(ex)
        throw ex
    }
    return this
}

fun File.requireNotExists(): File {
    if (exists()) {
        val ex = IllegalStateException("Path exist, but shouldn't: $this")
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

fun File.deleteAll() {
    if (isDirectory) {
        for (c in listFiles()) {
            c.deleteAll()
        }
    }
    if (delete()) {
        Timber.tag(TAG).v("File.deleteAll(): Deleted %s", this)
    } else if (!exists()) {
        Timber.tag(TAG).w("File.deleteAll(): File didn't exist: %s", this)
    } else {
        throw FileNotFoundException("Failed to delete file: $this")
    }
}