package eu.darken.bb.common.files.core.saf.oswrapper.manager

/**
 * https://github.com/android/platform_frameworks_base/blob/master/core/java/android/os/storage/DiskInfo.java
 */
data class DiskInfoX(private val diskInfoObject: Any) {
    val volumeInfoClass: Class<*> = diskInfoObject.javaClass

    @get:Throws(ReflectiveOperationException::class)
    val id: String
        get() = volumeInfoClass.getMethod("getId").invoke(diskInfoObject) as String

    @get:Throws(ReflectiveOperationException::class)
    val description: String?
        get() = volumeInfoClass.getMethod("getDescription").invoke(diskInfoObject) as String

    @get:Throws(ReflectiveOperationException::class)
    val isAdoptable: Boolean
        get() = volumeInfoClass.getMethod("isAdoptable").invoke(diskInfoObject) as Boolean

    @get:Throws(ReflectiveOperationException::class)
    val isDefaultPrimary: Boolean
        get() = volumeInfoClass.getMethod("isDefaultPrimary").invoke(diskInfoObject) as Boolean

    @get:Throws(ReflectiveOperationException::class)
    val isSd: Boolean
        get() = volumeInfoClass.getMethod("isSd").invoke(diskInfoObject) as Boolean

    @get:Throws(ReflectiveOperationException::class)
    val isUsb: Boolean
        get() = volumeInfoClass.getMethod("isUsb").invoke(diskInfoObject) as Boolean
}