package eu.darken.bb

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.SFile
import eu.darken.bb.common.moshi.*
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.Versioning
import eu.darken.bb.task.core.Task
import java.util.*


@Module
class AppModule {
    @Provides
    @PerApp
    fun moshi(): Moshi = Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .add(BackupSpec.MOSHI_FACTORY)
            .add(Generator.Config.MOSHI_FACTORY)
            .add(SFile.MOSHI_FACTORY)
            .add(Storage.Ref.MOSHI_FACTORY)
            .add(Storage.Config.MOSHI_FACTORY)
            .add(Versioning.MOSHI_FACTORY)
            .add(Task.MOSHI_FACTORY)
            .add(FileAdapter())
            .add(BackupIdAdapter())
            .add(UUIDAdapter())
            .add(GeneratorIdAdapter())
            .add(StorageIdAdapter())
            .add(TaskIdAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()

}
