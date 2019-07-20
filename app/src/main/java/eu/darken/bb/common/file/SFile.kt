package eu.darken.bb.common.file

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import java.io.File

interface SFile {
    val path: String
    val name: String
    val pathType: PathType

    enum class PathType {
        JAVA_FILE
    }

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<SFile> = PolymorphicJsonAdapterFactory.of(SFile::class.java, "pathType")
                .withSubtype(JavaFile::class.java, PathType.JAVA_FILE.name)
    }
}

fun SFile.asFile(): File = when {
    this is JavaFile -> this.file
    else -> JavaFile.build(this.path).file
}