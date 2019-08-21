package eu.darken.bb.backup.core

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.core.app.AppBackupGenerator
import eu.darken.bb.backup.core.app.AppSpecGeneratorEditor
import eu.darken.bb.backup.core.file.FileBackupGenerator
import eu.darken.bb.backup.core.file.FileSpecGeneratorEditor

@Module
abstract class BackupTypeModule {

    @Binds
    @IntoMap
    @BackupTypeKey(Backup.Type.APP)
    abstract fun appGeneratorEditor(repo: AppSpecGeneratorEditor.Factory): Generator.Editor.Factory<out Generator.Editor>

    @Binds
    @IntoMap
    @GeneratorTypeKey(Backup.Type.APP)
    abstract fun appGenerator(repo: AppBackupGenerator): Generator

    @Binds
    @IntoMap
    @BackupTypeKey(Backup.Type.FILE)
    abstract fun fileGeneratorEditor(repo: FileSpecGeneratorEditor.Factory): Generator.Editor.Factory<out Generator.Editor>

    @Binds
    @IntoMap
    @GeneratorTypeKey(Backup.Type.FILE)
    abstract fun fileGenerator(repo: FileBackupGenerator): Generator
}


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class BackupTypeKey(val value: Backup.Type)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class GeneratorTypeKey(val value: Backup.Type)