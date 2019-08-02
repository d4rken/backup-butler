package eu.darken.bb.backups.core

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.backups.core.app.AppBackupConfigEditor

@Module
abstract class BackupTypeModule {

    @Binds
    @IntoMap
    @BackupTypeKey(Backup.Type.APP)
    abstract fun appConfigEditor(repo: AppBackupConfigEditor.Factory): BackupConfigEditor.Factory<out BackupConfigEditor>
}


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class BackupTypeKey(val value: Backup.Type)