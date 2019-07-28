package eu.darken.bb.storage.core

import io.reactivex.Observable
import io.reactivex.Single

interface ConfigEditor<ConfigT : StorageConfig> {

    val config: Observable<ConfigT>

    fun load(storageRef: StorageRef): Single<ConfigT>

    fun save(storageRef: StorageRef): Single<ConfigT>

}