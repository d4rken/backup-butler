package eu.darken.bb.storage.core

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import eu.darken.bb.storage.core.local.LocalStorageFactory

@Module
abstract class StorageModule {

    @Binds
    @IntoSet
    @StorageFactory
    abstract fun localRepo(repo: LocalStorageFactory): BackupStorage.Factory
}