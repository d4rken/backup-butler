package eu.darken.bb.repos.core

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import eu.darken.bb.repos.core.local.LocalStorageFactory

@Module
abstract class RepoModule {

    @Binds
    @IntoSet
    @RepoFactory
    abstract fun localRepo(repo: LocalStorageFactory): BackupRepo.Factory
}