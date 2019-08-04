package eu.darken.bb.backups.core

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.backups.core.app.AppBackupSpecGenerator
import eu.darken.bb.backups.core.app.AppSpecGeneratorEditor

@Module
abstract class BackupTypeModule {

    @Binds
    @IntoMap
    @BackupTypeKey(Backup.Type.APP)
    abstract fun appConfigEditor(repo: AppSpecGeneratorEditor.Factory): SpecGenerator.Editor.Factory<out SpecGenerator.Editor>

    @Binds
    @IntoMap
    @GeneratorTypeKey(Backup.Type.APP)
    abstract fun appConfigGenerator(repo: AppBackupSpecGenerator): SpecGenerator
}


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class BackupTypeKey(val value: Backup.Type)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class GeneratorTypeKey(val value: Backup.Type)