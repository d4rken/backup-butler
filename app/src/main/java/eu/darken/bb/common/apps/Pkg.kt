package eu.darken.bb.common.apps

interface Pkg {

    val packageName: String

    val versionCode: Long

    val packageType: Type

    fun getLabel(ipcFunnel: IPCFunnel): String?

    @Throws(Exception::class)
    fun <T> tryField(fieldName: String): T?

    enum class Type {
        NORMAL, INSTANT, LIBRARY
    }
}
