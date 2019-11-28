package eu.darken.bb.common.file.core

import android.content.Context
import android.os.Parcelable
import androidx.annotation.Keep
import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.saf.SAFPath
import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import kotlinx.android.parcel.Parcelize

@Keep
interface APath : Parcelable {
    val path: String
    val name: String
    val pathType: PathType

    fun userReadablePath(context: Context) = path
    fun userReadableName(context: Context) = name

    fun child(vararg segments: String): APath

    @Keep
    @Parcelize
    enum class FileType : Parcelable {
        DIRECTORY, SYMBOLIC_LINK, FILE
    }

    @Keep
    enum class PathType {
        RAW, LOCAL, SAF
    }

    companion object {
        val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<APath> = MyPolymorphicJsonAdapterFactory.of(APath::class.java, "pathType")
                .withSubtype(RawPath::class.java, PathType.RAW.name)
                .withSubtype(LocalPath::class.java, PathType.LOCAL.name)
                .withSubtype(SAFPath::class.java, PathType.SAF.name)
                .skipLabelSerialization()
    }

}


inline fun <reified PT : APath, GT : APathGateway<PT, APathLookup<PT>>> PT.walk(gateway: GT, direction: FileWalkDirection): APathTreeWalk<PT, GT> = APathTreeWalk(gateway, this, direction)

inline fun <reified PT : APath, GT : APathGateway<PT, APathLookup<PT>>> PT.walkTopDown(gateway: GT): APathTreeWalk<PT, GT> = walk(gateway, FileWalkDirection.TOP_DOWN)