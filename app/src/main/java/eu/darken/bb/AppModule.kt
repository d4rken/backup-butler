package eu.darken.bb

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.SFile
import eu.darken.bb.common.moshi.*
import eu.darken.bb.storage.core.RevisionConfig
import eu.darken.bb.storage.core.StorageConfig
import eu.darken.bb.storage.core.StorageRef
import eu.darken.bb.task.core.BackupTask


@Module
class AppModule {
    @Provides
    @PerApp
    fun moshi(): Moshi = Moshi.Builder()
            .add(BackupSpec.MOSHI_FACTORY)
            .add(Generator.Config.MOSHI_FACTORY)
            .add(SFile.MOSHI_FACTORY)
            .add(StorageRef.MOSHI_FACTORY)
            .add(StorageConfig.MOSHI_FACTORY)
            .add(RevisionConfig.MOSHI_FACTORY)
            .add(BackupTask.MOSHI_FACTORY)
            .add(FileAdapter())
            .add(DateAdapter())
            .add(BackupIdAdapter())
            .add(UUIDAdapter())
            .add(GeneratorIdAdapter())
            .add(StorageIdAdapter())
            .add(TaskIdAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()

}
