package eu.darken.bb.common.files.core

import android.os.Environment
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.files.core.local.LocalPath
import javax.inject.Inject

@PerApp
class DevEnvironment @Inject constructor() {

    val publicDeviceStorages: List<DevStorage>
        get() {
            // TODO what about secondary storages?
            return listOf(
                    DevStorage(
                            path = LocalPath.build(Environment.getExternalStorageDirectory())
                    )
            )
        }


    data class DevStorage(
            val path: LocalPath
    )
}