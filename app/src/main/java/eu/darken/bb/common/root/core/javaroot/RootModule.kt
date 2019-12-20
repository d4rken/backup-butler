package eu.darken.bb.common.root.core.javaroot

import android.content.Context
import dagger.Module
import dagger.Provides
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.shell.SharedShell

@Module
class RootModule {

    @Provides
    @PerApp
    fun sharedShell(): SharedShell {
        return SharedShell(JavaRootHost.TAG)
    }

    @Provides
    @PerApp
    @AppContext
    fun rootContext(context: Context): Context {
        return context
    }

}