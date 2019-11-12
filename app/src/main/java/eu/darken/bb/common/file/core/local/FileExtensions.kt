package eu.darken.bb.common.file.core.local

import eu.darken.bb.App
import eu.darken.bb.common.file.core.APath
import okio.buffer
import okio.sink
import okio.source
import timber.log.Timber
import java.io.*
import java.io.File

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

fun File.asSFile(): APath {
    return LocalPath.build(file = this)
}

fun File.requireExists(): File {
    if (!exists()) {
        val ex = IllegalStateException("Path doesn't exist, but should: $this")
        Timber.w(ex)
        throw ex
    }
    return this
}

fun File.requireNotExists(): File {
    if (exists()) {
        val ex = IllegalStateException("Path exist, but shouldn't: $this")
        Timber.w(ex)
        throw ex
    }
    return this
}

fun File.tryMkDirs(): File {
    if (exists()) {
        if (isDirectory) {
            Timber.v("Directory already exists, not creating: %s", this)
            return this
        } else {
            val ex = IllegalStateException("Directory exists, but is not a directory: $this")
            Timber.w(ex)
            throw ex
        }
    }

    if (mkdirs()) {
        Timber.v("Directory created: %s", this)
        return this
    } else {
        val ex = IllegalStateException("Couldn't create Directory: $this")
        Timber.w(ex)
        throw ex
    }
}

fun File.tryMkFile(): File {
    if (exists()) {
        if (isFile) {
            Timber.v("File already exists, not creating: %s", this)
            return this
        } else {
            val ex = IllegalStateException("Path exists but is not a file: $this")
            Timber.w(ex)
            throw ex
        }
    }

    if (!parentFile.exists()) parentFile.tryMkDirs()

    if (createNewFile()) {
        Timber.v("File created: %s", this)
        return this
    } else {
        val ex = IllegalStateException("Couldn't create file: $this")
        Timber.w(ex)
        throw ex
    }
}

fun File.deleteAll() {
    if (isDirectory) {
        val dirContent: Array<File>? = listFiles()
        dirContent?.forEach { it.deleteAll() }
    }
    if (delete()) {
        Timber.v("File.deleteAll(): Deleted %s", this)
    } else if (!exists()) {
        Timber.w("File.deleteAll(): File didn't exist: %s", this)
    } else {
        throw FileNotFoundException("Failed to delete file: $this")
    }
}

fun File.copyTo(fd: FileDescriptor) {
    FileInputStream(this).source().use { source ->
        FileOutputStream(fd).sink().buffer().use { buffer ->
            buffer.writeAll(source)
        }
    }
}

fun File.safeListFiles(): Array<File> {
    return this.listFiles() ?: throw IOException("listFiles() returned NULL")
}

fun File.safeListFiles(filter: (File) -> Boolean): Array<File> {
    return this.listFiles(filter) ?: throw IOException("listFiles(filter=$filter) returned NULL")
}

fun File.isSymbolicLink(): Boolean {
    val resolvedPath: File
    try {
        resolvedPath = canonicalFile
    } catch (e: IOException) {
        Timber.tag(App.logTag("File")).e(e)
        return false
    }

    return this != resolvedPath
}