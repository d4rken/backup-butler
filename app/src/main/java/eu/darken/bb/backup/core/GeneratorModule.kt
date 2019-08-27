package eu.darken.bb.backup.core

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.core.app.AppSpecGenerator
import eu.darken.bb.backup.core.app.AppSpecGeneratorEditor
import eu.darken.bb.backup.core.files.FilesSpecGenerator
import eu.darken.bb.backup.core.files.FilesSpecGeneratorEditor

@Module
abstract class BackupTypeModule {

    @Binds
    @IntoMap
    @GeneratorTypeKey(Backup.Type.APP)
    abstract fun appGeneratorEditor(repo: AppSpecGeneratorEditor.Factory): Generator.Editor.Factory<out Generator.Editor>

    @Binds
    @IntoMap
    @GeneratorTypeKey(Backup.Type.APP)
    abstract fun appGenerator(repo: AppSpecGenerator): Generator

    @Binds
    @IntoMap
    @GeneratorTypeKey(Backup.Type.FILES)
    abstract fun fileGeneratorEditor(repo: FilesSpecGeneratorEditor.Factory): Generator.Editor.Factory<out Generator.Editor>

    @Binds
    @IntoMap
    @GeneratorTypeKey(Backup.Type.FILES)
    abstract fun fileGenerator(repo: FilesSpecGenerator): Generator
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class GeneratorTypeKey(val value: Backup.Type)