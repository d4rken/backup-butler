package eu.darken.bb.common.file

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import java.io.File

interface SFile {
    val path: String
    val name: String
    val type: Type
    val pathType: SFileType

    enum class Type {
        FILE, DIRECTORY;
    }

    enum class SFileType {
        SIMPLE, JAVA, URI
    }

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<SFile> = PolymorphicJsonAdapterFactory.of(SFile::class.java, "pathType")
                .withSubtype(JavaFile::class.java, SFileType.JAVA.name)
                .withSubtype(SimpleFile::class.java, SFileType.SIMPLE.name)
                .withSubtype(UriFile::class.java, SFileType.URI.name)
    }
}

fun SFile.asFile(): File = when (this) {
    is JavaFile -> this.file
    else -> File(this.path)
}

//@NonNull
//public static Collection<String> fileToString(@NonNull Collection<SDMFile> files) {
//    Collection<String> strings = new ArrayList<>();
//    for (SDMFile file : files) strings.add(file.getPath());
//    return strings;
//}