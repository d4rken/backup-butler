package eu.darken.bb.common.files.core.saf.oswrapper.manager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import androidx.annotation.RequiresApi
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.ApiHelper.hasAndroidN
import eu.darken.bb.common.debug.logging.logTag
import java.io.File
import java.lang.reflect.Method
import java.util.*
import javax.inject.Inject

@Reusable
class StorageManagerX @Inject constructor(@ApplicationContext context: Context) {

    private val storageManager: StorageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    private var _getVolumeList: Method? = null
    private var _getVolumes: Method? = null

    @get:Throws(ReflectiveOperationException::class)
    @get:SuppressLint("NewApi")
    val volumeList: List<StorageVolumeX>
        get() {
            val svList: MutableList<StorageVolumeX> = ArrayList()
            if (hasAndroidN()) {
                for (vol in storageManager.storageVolumes) svList.add(StorageVolumeX(vol))
            } else {
                if (_getVolumeList == null) _getVolumeList = storageManager.javaClass.getMethod("getVolumeList")
                val storageVolumes = _getVolumeList!!.invoke(storageManager) as Array<Any>
                for (storageVolume in storageVolumes) svList.add(StorageVolumeX(storageVolume))
            }
            return svList
        }

    @get:Throws(ReflectiveOperationException::class)
    @get:RequiresApi(Build.VERSION_CODES.M)
    val volumes: List<VolumeInfoX>
        get() {
            if (_getVolumes == null) _getVolumes = storageManager.javaClass.getMethod("getVolumes")
            val volumeInfoXList: MutableList<VolumeInfoX> = ArrayList()
            val storageInfos = _getVolumes!!.invoke(storageManager) as List<*>
            for (storageInfo in storageInfos) volumeInfoXList.add(VolumeInfoX(storageInfo!!))
            return volumeInfoXList
        }

    @SuppressLint("NewApi")
    fun getRootStorageVolume(file: File): StorageVolumeX? {
        return if (hasAndroidN()) {
            val volume = storageManager.getStorageVolume(file)
            volume?.let { StorageVolumeX(it) }
        } else {
            volumeList.firstOrNull { it.path == file.path }
        }
    }

    companion object {
        val TAG = logTag("StorageManagerX")
    }
}