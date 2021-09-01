package eu.darken.bb.common.root.core.javaroot

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import eu.darken.bb.common.dagger.PerApp


@PerApp
@Component(
    modules = [
        RootModule::class
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