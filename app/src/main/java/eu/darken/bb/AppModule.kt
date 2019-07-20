package eu.darken.bb

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.common.file.SFile
import eu.darken.bb.common.moshi.BackupIdAdapter
import eu.darken.bb.common.moshi.DateAdapter
import eu.darken.bb.common.moshi.FileAdapter


@Module
class AppModule {
    @Provides
    @AppComponent.Scope
    fun moshi(): Moshi = Moshi.Builder()
            .add(Backup.Config.MOSHI_FACTORY)
            .add(Backup.MOSHI_FACTORY)
            .add(SFile.MOSHI_FACTORY)
            .add(BackupRepo.RevisionConfig.MOSHI_FACTORY)
            .add(FileAdapter())
            .add(DateAdapter())
            .add(BackupIdAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()

}
