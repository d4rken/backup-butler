package eu.darken.bb.common.file.core

import android.content.Context
import android.os.Parcelable
import androidx.annotation.Keep
import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.saf.SAFPath
import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import java.io.File

@Keep
interface APath : Parcelable {
    val path: String
    val name: String
    val pathType: Type

    fun userReadablePath(context: Context) = path
    fun userReadableName(context: Context) = name

    @Keep
    enum class Type {
        RAW, LOCAL, SAF
    }

    companion object {
        val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<APath> = MyPolymorphicJsonAdapterFactory.of(APath::class.java, "pathType")
                .withSubtype(RawPath::class.java, Type.RAW.name)
                .withSubtype(LocalPath::class.java, Type.LOCAL.name)
                .withSubtype(SAFPath::class.java, Type.SAF.name)
                .skipLabelSerialization()
    }

}

fun APath.asFile(): File = when (this) {
    is LocalPath -> this.file
    else -> File(this.path)
}