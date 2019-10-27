package eu.darken.bb.common.file

import eu.darken.bb.common.TypeMissMatchException
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class SimplePath(
        override val path: String
) : APath {

    override var pathType: APath.Type
        get() = APath.Type.SIMPLE
        set(value) {
            TypeMissMatchException.check(value, pathType)
        }

    override val name: String
        get() = path.substringAfterLast(File.separatorChar)

    companion object {
        fun build(base: File, vararg crumbs: String): SimplePath =
                build(base.canonicalPath, *crumbs)

        fun build(vararg crumbs: String): SimplePath {
            var compacter = File(crumbs[0])
            for (i in 1 until crumbs.size) {
                compacter = File(compacter, crumbs[i])
            }
            return SimplePath(compacter.path)
        }
    }
}