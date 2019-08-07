package eu.darken.bb.storage.core

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import eu.darken.bb.storage.core.local.LocalStorageEditor
import eu.darken.bb.storage.core.local.LocalStorageFactory
import javax.inject.Qualifier

@Module
abstract class StorageTypeModule {

    @Binds
    @IntoSet
    @StorageFactory
    abstract fun localRepo(repo: LocalStorageFactory): Storage.Factory

    @Binds
    @IntoMap
    @StorageTypeKey(Storage.Type.LOCAL)
    abstract fun localStorageEditor(repo: LocalStorageEditor.Factory): StorageEditor.Factory<out StorageEditor>
}


@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class StorageFactory


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class StorageTypeKey(val value: Storage.Type)