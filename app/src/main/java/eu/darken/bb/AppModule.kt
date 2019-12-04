package eu.darken.bb

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import dagger.Module
import dagger.Provides
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.moshi.*
import eu.darken.bb.processor.core.mm.CachePath
import eu.darken.bb.processor.core.mm.Props
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.Task
import java.util.*


@Module
class AppModule {

    @Provides
    @PerApp
    fun moshi(): Moshi = Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .add(BackupSpec.MOSHI_FACTORY)
            .add(Restore.Config.MOSHI_FACTORY)
            .add(Generator.Config.MOSHI_FACTORY)
            .add(APath.MOSHI_FACTORY)
            .add(Storage.Ref.MOSHI_FACTORY)
            .add(Storage.Config.MOSHI_FACTORY)
            .add(Storage.Strategy.MOSHI_FACTORY)
            .add(Backup.MetaData.MOSHI_FACTORY)
            .add(Props.MOSHI_FACTORY)
            .add(Task.MOSHI_FACTORY)
            .add(FileAdapter())
            .add(BackupIdAdapter())
            .add(UUIDAdapter())
            .add(GeneratorIdAdapter())
            .add(StorageIdAdapter())
            .add(TaskIdAdapter())
            .add(TaskResultIdAdapter())
            .add(MMRefIdAdapter())
            .add(UriAdapter())
            .add(BackupSpecIdentifierAdapter())
            .build()

    @Provides
    @PerApp
    @CachePath
    fun mmCachePath(@AppContext context: Context) = context.cacheDir
}
