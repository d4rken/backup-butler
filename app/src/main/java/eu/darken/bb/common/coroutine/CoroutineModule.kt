package eu.darken.bb.common.coroutine

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eu.darken.bb.common.root.javaroot.RootComponent
import kotlinx.coroutines.CoroutineScope

/**
 * Also installed in non-hilt [RootComponent]
 */
@InstallIn(SingletonComponent::class)
@Module
abstract class CoroutineModule {

    @Binds
    abstract fun dispatcherProvider(defaultProvider: DefaultDispatcherProvider): DispatcherProvider

    @Binds
    @AppScope
    abstract fun appscope(appCoroutineScope: AppCoroutineScope): CoroutineScope
}
