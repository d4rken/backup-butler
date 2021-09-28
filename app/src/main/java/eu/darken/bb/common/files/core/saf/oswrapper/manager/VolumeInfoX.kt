package eu.darken.bb.common.files.core.saf.oswrapper.manager

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import eu.darken.bb.common.debug.logging.logTag
import timber.log.Timber
import java.io.File
import java.lang.reflect.Method

/**
 * https://github.com/android/platform_frameworks_base/blob/master/core/java/android/os/storage/VolumeInfo.java
 * https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/os/storage/VolumeInfo.java;
 */
@TargetApi(Build.VERSION_CODES.M)
data class VolumeInfoX internal constructor(private val volumeInfoObject: Any) {
    private val volumeInfoClass: Class<*> = volumeInfoObject.javaClass
    private val methodGetId: Method = volumeInfoClass.getMethod("getId")
    private val methodIsPrimary: Method = volumeInfoClass.getMethod("isPrimary")
    private val methodGetFsUuid: Method = volumeInfoClass.getMethod("getFsUuid")
    private val methodGetPath: Method = volumeInfoClass.getMethod("getPath")
    private val methodGetState: Method = volumeInfoClass.getMethod("getState")
    private val methodGetDisk: Method = volumeInfoClass.getMethod("getDisk")
    private val methodGetType: Method = volumeInfoClass.getMethod("getType")
    private var methodGetDescription: Method? = null
    private var methodGetPathForUser: Method? = null

    @get:Throws(ReflectiveOperationException::class)
    val disk: DiskInfoX?
        get() {
            return methodGetDisk.invoke(volumeInfoObject)?.let { DiskInfoX(it) }
        }

    @get:Throws(ReflectiveOperationException::class)
    val isMounted: Boolean
        get() = state == STATE_MOUNTED

    @get:Throws(ReflectiveOperationException::class)
    val isPrivate: Boolean
        get() = type == TYPE_PRIVATE

    @get:Throws(ReflectiveOperationException::class)
    val isEmulated: Boolean
        get() = type == TYPE_EMULATED

    @get:Throws(ReflectiveOperationException::class)
    val isRemovable: Boolean
        get() {
            val type = type
            return if (type == TYPE_EMULATED) {
                id != ID_EMULATED_INTERNAL
            } else type == TYPE_PUBLIC
        }

    @get:Throws(ReflectiveOperationException::class)
    val type: Int
        get() = methodGetType.invoke(volumeInfoObject) as Int

    @get:Throws(ReflectiveOperationException::class)
    val state: Int
        get() = methodGetState.invoke(volumeInfoObject) as Int

    @get:Throws(ReflectiveOperationException::class)
    val id: String
        get() = methodGetId.invoke(volumeInfoObject) as String

    @get:Throws(ReflectiveOperationException::class)
    val isPrimary: Boolean
        get() = methodIsPrimary.invoke(volumeInfoObject) as Boolean

    @get:Throws(ReflectiveOperationException::class)
    val fsUuid: String?
        get() = methodGetFsUuid.invoke(volumeInfoObject) as String?

    @get:Throws(ReflectiveOperationException::class)
    val path: File
        get() = methodGetPath.invoke(volumeInfoObject) as File

    // https://github.com/d4rken/sdmaid-public/issues/1678
    val description: String?
        get() {
            if (methodGetDescription == null) {
                try {
                    methodGetDescription = volumeInfoClass.getMethod("getDescription")
                } catch (e: ReflectiveOperationException) { // https://github.com/d4rken/sdmaid-public/issues/1678
                    Timber.tag(TAG).w(e)
                }
            }
            if (methodGetDescription != null) {
                try {
                    return methodGetDescription!!.invoke(volumeInfoObject) as String?
                } catch (e: ReflectiveOperationException) {
                    Timber.tag(TAG).w(e)
                }
            }
            return null
        }

    fun getPathForUser(userId: Int): File? {
        if (methodGetPathForUser == null) {
            try {
                methodGetPathForUser = volumeInfoClass.getMethod("getPathForUser", Int::class.javaPrimitiveType)
            } catch (e: ReflectiveOperationException) { // https://github.com/d4rken/sdmaid-public/issues/1678
                Timber.tag(TAG).w(e)
            }
        }
        if (methodGetPathForUser != null) {
            try {
                return methodGetPathForUser!!.invoke(volumeInfoObject, userId) as File
            } catch (e: ReflectiveOperationException) {
                Timber.tag(TAG).w(e)
            }
        }
        return null
    }

    override fun toString(): String {
        return try {
            "VolumeInfoX(fsUuid=$fsUuid, state=$state, path=$path, description=$description, disk=$disk)"
        } catch (e: ReflectiveOperationException) {
            "Failed to gather info for ${volumeInfoObject}: ${e.message}"
        }
    }

    companion object {
        private val TAG: String = logTag("VolumeInfoX")

        private const val TYPE_PUBLIC = 0
        private const val TYPE_PRIVATE = 1
        private const val TYPE_EMULATED = 2
        private const val TYPE_ASEC = 3
        private const val TYPE_OBB = 4
        const val STATE_UNMOUNTED = 0
        const val STATE_CHECKING = 1
        const val STATE_MOUNTED = 2
        const val STATE_MOUNTED_READ_ONLY = 3
        const val STATE_FORMATTING = 4
        const val STATE_EJECTING = 5
        const val STATE_UNMOUNTABLE = 6
        const val STATE_REMOVED = 7
        const val STATE_BAD_REMOVAL = 8

        /**
         * Real volume representing internal emulated storage
         */
        private const val ID_EMULATED_INTERNAL = "emulated"

        @SuppressLint("PrivateApi") @Throws(ReflectiveOperationException::class)
        fun getEnvironmentForState(state: Int): String {
            val volumeInfoClass = Class.forName("android.os.storage.VolumeInfo")
            val methodGetEnvironmentForState =
                volumeInfoClass.getMethod("getEnvironmentForState", Int::class.javaPrimitiveType)
            return methodGetEnvironmentForState.invoke(null, state) as String
        }
    }
}