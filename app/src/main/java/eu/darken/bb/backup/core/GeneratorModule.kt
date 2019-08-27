package eu.darken.bb.backup.core

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.core.app.AppSpecGenerator
import eu.darken.bb.backup.core.app.AppSpecGeneratorEditor
import eu.darken.bb.backup.core.files.legacy.LegacyFilesSpecGenerator
import eu.darken.bb.backup.core.files.legacy.LegacyFilesSpecGeneratorEditor

@Module
abstract class BackupTypeModule {

    @Binds
    @IntoMap
    @GeneratorTypeKey(Generator.Type.APP)
    abstract fun appGeneratorEditor(repo: AppSpecGeneratorEditor.Factory): Generator.Editor.Factory<out Generator.Editor>

    @Binds
    @IntoMap
    @GeneratorTypeKey(Generator.Type.APP)
    abstract fun appGenerator(repo: AppSpecGenerator): Generator

    @Binds
    @IntoMap
    @GeneratorTypeKey(Generator.Type.FILE_LEGACY)
    abstract fun fileGeneratorEditor(repo: LegacyFilesSpecGeneratorEditor.Factory): Generator.Editor.Factory<out Generator.Editor>

    @Binds
    @IntoMap
    @GeneratorTypeKey(Generator.Type.FILE_LEGACY)
    abstract fun fileGenerator(repo: LegacyFilesSpecGenerator): Generator
}


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class BackupTypeKey(val value: Backup.Type)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class GeneratorTypeKey(val value: Generator.Type)