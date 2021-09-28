package eu.darken.bb.common.files.core.saf.oswrapper.manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources.NotFoundException
import android.os.Build
import android.os.UserHandle
import android.os.storage.StorageVolume
import androidx.annotation.RequiresApi
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.debug.logging.logTag
import timber.log.Timber
import java.io.File

/**
 * http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/5.0.2_r1/android/os/storage/StorageVolume.java
 * https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/os/storage/StorageVolume.java
 */
data class StorageVolumeX internal constructor(private val volumeObj: Any) {

    private val volumeClass: Class<*> = volumeObj.javaClass

    @get:Throws(ReflectiveOperationException::class)
    @get:SuppressLint("NewApi")
    val isPrimary: Boolean
        get() = if (ApiHelper.hasAndroidN()) {
            (volumeObj as StorageVolume).isPrimary
        } else {
            volumeClass.getMethod("isPrimary").invoke(volumeObj) as Boolean
        }

    @get:Throws(ReflectiveOperationException::class)
    @get:SuppressLint("NewApi")
    val isRemovable: Boolean
        get() = if (ApiHelper.hasAndroidN()) {
            (volumeObj as StorageVolume).isRemovable
        } else {
            val _isRemovable = volumeClass.getMethod("isRemovable")
            _isRemovable.invoke(volumeObj) as Boolean
        }

    /**
     * Returns true if the volume is emulated.
     *
     * @return is removable
     */
    @get:Throws(ReflectiveOperationException::class)
    @get:SuppressLint("NewApi")
    val isEmulated: Boolean
        get() = if (ApiHelper.hasAndroidN()) {
            (volumeObj as StorageVolume).isEmulated
        } else {
            val _isEmulated = volumeClass.getMethod("isEmulated")
            _isEmulated.invoke(volumeObj) as Boolean
        }

    /**
     * http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/5.0.0_r1/com/android/server/MountService.java#904
     */
    @get:Throws(ReflectiveOperationException::class)
    @get:SuppressLint("NewApi")
    val uuid: String?
        get() = if (ApiHelper.hasAndroidN()) {
            (volumeObj as StorageVolume).uuid
        } else {
            volumeClass.getMethod("getUuid").invoke(volumeObj) as String?
        }

    @get:Throws(ReflectiveOperationException::class)
    @get:SuppressLint("NewApi")
    val state: String
        get() = if (ApiHelper.hasAndroidN()) {
            (volumeObj as StorageVolume).state
        } else {
            volumeClass.getMethod("getState").invoke(volumeObj) as String
        }

    @get:Throws(ReflectiveOperationException::class)
    val path: String
        get() {
            return volumeClass.getMethod("getPath").invoke(volumeObj) as String
        }

    @get:Throws(ReflectiveOperationException::class)
    val pathFile: File
        get() {
            return volumeClass.getMethod("getPathFile").invoke(volumeObj) as File
        }

    @get:Throws(ReflectiveOperationException::class)
    val userLabel: String
        get() {
            return volumeClass.getMethod("getUserLabel").invoke(volumeObj) as String
        }

    @SuppressLint("NewApi") @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Throws(ReflectiveOperationException::class)
    fun getDescription(context: Context?): String? {
        try {
            return volumeClass.getMethod("getDescription", Context::class.java).invoke(volumeObj, context) as String?
        } catch (e: NotFoundException) {
            Timber.tag(TAG).e(e)
        }
        return null
    }


    val owner: UserHandle?
        get() {
            try {
                return volumeClass.getMethod("getOwner").invoke(volumeObj) as UserHandle
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "StorageVolumeX.getOwner() threw an error.")
            }
            return null
        }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun createAccessIntent(directory: String?): Intent? {
        return (volumeObj as StorageVolume).createAccessIntent(directory)
    }

    override fun toString(): String {
        return try {
            "StorageVolumeX(uuid=$uuid, state=$state, path=$path, primary=$isPrimary, emulated=$isEmulated, owner=$owner, userlabel=$userLabel)"
        } catch (e: ReflectiveOperationException) {
            e.message!!
        }
    }

    companion object {
        val TAG: String = logTag("StorageVolumeX")
    }
}