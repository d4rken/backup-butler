package eu.darken.bb

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import eu.darken.bb.backup.backups.BackupConfig
import eu.darken.bb.common.file.SFile
import eu.darken.bb.common.moshi.BackupIdAdapter
import eu.darken.bb.common.moshi.DateAdapter
import eu.darken.bb.common.moshi.FileAdapter
import eu.darken.bb.common.moshi.UUIDAdapter
import eu.darken.bb.repos.core.RepoConfig
import eu.darken.bb.repos.core.RepoRef
import eu.darken.bb.repos.core.RevisionConfig
import eu.darken.bb.tasks.core.BackupTask


@Module
class AppModule {
    @Provides
    @AppComponent.Scope
    fun moshi(): Moshi = Moshi.Builder()
            .add(BackupConfig.MOSHI_FACTORY)
            .add(SFile.MOSHI_FACTORY)
            .add(RepoRef.MOSHI_FACTORY)
            .add(RepoConfig.MOSHI_FACTORY)
            .add(RevisionConfig.MOSHI_FACTORY)
            .add(BackupTask.MOSHI_FACTORY)
            .add(FileAdapter())
            .add(DateAdapter())
            .add(BackupIdAdapter())
            .add(UUIDAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()

}
