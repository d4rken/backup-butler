package eu.darken.bb.common.file

import java.io.File

interface SFile {
    val path: String
    val name: String
}

fun SFile.asFile(): File = when {
    this is JavaFile -> this.file
    else -> JavaFile.build(this.path).file
}