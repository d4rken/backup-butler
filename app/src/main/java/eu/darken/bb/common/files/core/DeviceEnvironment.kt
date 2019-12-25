package eu.darken.bb.common.files.core

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.files.core.local.LocalPath
import javax.inject.Inject

@PerApp
class DeviceEnvironment @Inject constructor(
        @AppContext private val context: Context
) {

    val publicPrimaryStorage: DeviceStorage
        get() {
            return DeviceStorage(LocalPath.build(Environment.getExternalStorageDirectory()))
        }

    val publicSecondaryStorage: Collection<DeviceStorage>
        get() {
            val pathResult = mutableListOf<LocalPath>()
            for (extMyDir in ContextCompat.getExternalFilesDirs(context, null)) {
                if (extMyDir == null) continue
                if (!extMyDir.isAbsolute) continue
                var findRoot = extMyDir
                for (i in 0..3) {
                    findRoot = findRoot.parentFile
                    if (findRoot == null) break
                }
                if (findRoot == null) continue
                pathResult.add(LocalPath.build(findRoot))
            }
            val primary = publicPrimaryStorage.storagePath
            return pathResult
                    .filter { it != primary }
                    .map { DeviceStorage((it)) }
        }

    val publicStorage: List<DeviceStorage>
        get() {
            return listOf(publicPrimaryStorage).plus(publicSecondaryStorage)
        }

    // http://androidxref.com/5.1.1_r6/xref/frameworks/base/core/java/android/os/Environment.java#136
    val publicDeviceStorages: List<DeviceStorage>
        get() {
            // TODO what about secondary storages?
            return listOf(
                    DeviceStorage(
                            storagePath = LocalPath.build(Environment.getExternalStorageDirectory())
                    )
            )
        }


    data class DeviceStorage(
            val storagePath: LocalPath
    )
}