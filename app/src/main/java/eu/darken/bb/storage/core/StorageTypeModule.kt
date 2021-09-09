package eu.darken.bb.storage.core

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import eu.darken.bb.storage.core.local.LocalStorage
import eu.darken.bb.storage.core.local.LocalStorageEditor
import eu.darken.bb.storage.core.saf.SAFStorage
import eu.darken.bb.storage.core.saf.SAFStorageEditor

@InstallIn(SingletonComponent::class)
@Module
abstract class StorageTypeModule {

    @Binds
    @IntoMap
    @StorageTypeKey(Storage.Type.LOCAL)
    abstract fun localRepo(repo: LocalStorage.Factory): Storage.Factory<out Storage>

    @Binds
    @IntoMap
    @StorageTypeKey(Storage.Type.LOCAL)
    abstract fun localStorageEditor(repo: LocalStorageEditor.Factory): StorageEditor.Factory<out StorageEditor>

    @Binds
    @IntoMap
    @StorageTypeKey(Storage.Type.SAF)
    abstract fun safRepo(repo: SAFStorage.Factory): Storage.Factory<out Storage>

    @Binds
    @IntoMap
    @StorageTypeKey(Storage.Type.SAF)
    abstract fun safStorageEditor(repo: SAFStorageEditor.Factory): StorageEditor.Factory<out StorageEditor>
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class StorageTypeKey(val value: Storage.Type)