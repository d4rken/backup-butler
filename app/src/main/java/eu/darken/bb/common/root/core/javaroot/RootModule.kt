package eu.darken.bb.common.root.core.javaroot

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eu.darken.bb.common.shell.SharedShell
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RootModule {

    @Provides
    @Singleton
    fun sharedShell(): SharedShell {
        return SharedShell(JavaRootHost.TAG)
    }
}