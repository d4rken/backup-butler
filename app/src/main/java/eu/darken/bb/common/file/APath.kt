package eu.darken.bb.common.file

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import java.io.File

interface APath {
    val path: String
    val name: String
    val pathType: SFileType


    enum class SFileType {
        SIMPLE, JAVA, SAF
    }

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<APath> = PolymorphicJsonAdapterFactory.of(APath::class.java, "pathType")
                .withSubtype(JavaPath::class.java, SFileType.JAVA.name)
                .withSubtype(SimplePath::class.java, SFileType.SIMPLE.name)
                .withSubtype(SAFPath::class.java, SFileType.SAF.name)
    }
}

fun APath.asFile(): File = when (this) {
    is JavaPath -> this.file
    else -> File(this.path)
}