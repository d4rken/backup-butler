package eu.darken.bb.common.root.javaroot

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.migration.DisableInstallInCheck
import eu.darken.bb.common.shell.RootProcessShell
import eu.darken.bb.common.shell.SharedShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.plus
import javax.inject.Singleton

/**
 * Installed in non-hilt [RootComponent]
 */
@DisableInstallInCheck
@Module
class RootModule {

    @Provides
    @Singleton
    @RootProcessShell
    fun sharedShell(): SharedShell {
        return SharedShell(JavaRootHost.TAG, GlobalScope + Dispatchers.IO)
    }

    @Provides
    @Singleton
    @ApplicationContext
    fun rootContext(context: Context): Context {
        return context
    }

}