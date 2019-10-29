package eu.darken.bb.common.file

import android.content.Context
import android.os.Parcelable
import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import java.io.File

interface APath : Parcelable {
    val path: String
    val name: String
    val pathType: Type

    fun userReadablePath(context: Context) = path

    enum class Type {
        RAW, LOCAL, SAF
    }

    companion object {
        val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<APath> = MyPolymorphicJsonAdapterFactory.of(APath::class.java, "pathType")
                .withSubtype(RawPath::class.java, Type.RAW.name)
                .withSubtype(JavaPath::class.java, Type.LOCAL.name)
                .withSubtype(SAFPath::class.java, Type.SAF.name)
                .skipLabelSerialization()
    }

}

fun APath.asFile(): File = when (this) {
    is JavaPath -> this.file
    else -> File(this.path)
}