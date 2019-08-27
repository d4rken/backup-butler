package eu.darken.bb.common.file

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import java.io.File

interface SFile {
    val path: String
    val name: String
    val type: Type
    val pathType: SFileType

    val parent: SFile

    enum class Type {
        FILE, DIRECTORY
    }

    enum class SFileType {
        SIMPLE, JAVA_FILE
    }

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<SFile> = PolymorphicJsonAdapterFactory.of(SFile::class.java, "pathType")
                .withSubtype(JavaFile::class.java, SFileType.JAVA_FILE.name)
                .withSubtype(SimpleFile::class.java, SFileType.SIMPLE.name)
    }
}

fun SFile.asFile(): File = when {
    this is JavaFile -> this.file
    else -> JavaFile.build(this.path).file
}

//@NonNull
//public static Collection<String> fileToString(@NonNull Collection<SDMFile> files) {
//    Collection<String> strings = new ArrayList<>();
//    for (SDMFile file : files) strings.add(file.getPath());
//    return strings;
//}