package eu.darken.bb.storage.core

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import eu.darken.bb.storage.core.local.LocalStorageFactory
import javax.inject.Qualifier

@Module
abstract class StorageTypeModule {

    @Binds
    @IntoSet
    @StorageFactory
    abstract fun localRepo(repo: LocalStorageFactory): BackupStorage.Factory
}


@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class StorageFactory

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class StorageEditor