package eu.darken.bb.common.file

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import java.io.File

interface AFile {
    val path: String
    val name: String
    val type: Type
    val pathType: SFileType

    val isFile: Boolean
        get() = type == Type.FILE

    val isDir: Boolean
        get() = type == Type.DIRECTORY

    enum class Type {
        FILE, DIRECTORY;
    }

    enum class SFileType {
        SIMPLE, JAVA, SAF
    }

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<AFile> = PolymorphicJsonAdapterFactory.of(AFile::class.java, "pathType")
                .withSubtype(JavaFile::class.java, SFileType.JAVA.name)
                .withSubtype(SimpleFile::class.java, SFileType.SIMPLE.name)
                .withSubtype(SAFFile::class.java, SFileType.SAF.name)
    }
}

fun AFile.asFile(): File = when (this) {
    is JavaFile -> this.file
    else -> File(this.path)
}

//@NonNull
//public static Collection<String> fileToString(@NonNull Collection<SDMFile> files) {
//    Collection<String> strings = new ArrayList<>();
//    for (SDMFile file : files) strings.add(file.getPath());
//    return strings;
//}