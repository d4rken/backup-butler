package eu.darken.bb.common.root.javaroot

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import eu.darken.bb.common.coroutine.CoroutineModule
import javax.inject.Singleton

/**
 * Injected into java process run by root via su shell, see [JavaRootHost]
 */
@Singleton
@Component(
    modules = [
        RootModule::class,
        CoroutineModule::class
    ]
)
interface RootComponent {

    fun inject(main: JavaRootHost)

    @Component.Builder
    interface Builder {
        fun build(): RootComponent

        @BindsInstance fun application(context: Context): Builder
    }

}