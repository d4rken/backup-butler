package eu.darken.bb.storage.core.local

import eu.darken.bb.common.HotData
import eu.darken.bb.storage.core.ConfigEditor
import eu.darken.bb.storage.core.StorageRef
import io.reactivex.Single
import javax.inject.Inject

class LocalStorageConfigEditor @Inject constructor() : ConfigEditor<LocalStorageConfig> {
    private val configPub = HotData<LocalStorageConfig>()

    override val config = configPub.data

    override fun load(storageRef: StorageRef): Single<LocalStorageConfig> {
        TODO("not implemented")
    }

    override fun save(storageRef: StorageRef): Single<LocalStorageConfig> {
        TODO("not implemented")
    }

}