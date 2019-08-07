package eu.darken.bb.backup.core

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.core.app.AppBackupGenerator
import eu.darken.bb.backup.core.app.AppSpecGeneratorEditor

@Module
abstract class BackupTypeModule {

    @Binds
    @IntoMap
    @BackupTypeKey(Backup.Type.APP)
    abstract fun appConfigEditor(repo: AppSpecGeneratorEditor.Factory): Generator.Editor.Factory<out Generator.Editor>

    @Binds
    @IntoMap
    @GeneratorTypeKey(Backup.Type.APP)
    abstract fun appConfigGenerator(repo: AppBackupGenerator): Generator
}


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class BackupTypeKey(val value: Backup.Type)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class GeneratorTypeKey(val value: Backup.Type)