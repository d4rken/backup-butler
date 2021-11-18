package eu.darken.bb.common.debug

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import eu.darken.bb.common.debug.modules.*
import eu.darken.bb.common.debug.recording.core.RecorderModule
import kotlinx.coroutines.CoroutineScope

@InstallIn(SingletonComponent::class)
@Module
abstract class DebugModuleModule {

    @Binds
    @DebugScope
    abstract fun debugScope(scope: DebugCoroutineScope): CoroutineScope

    @Binds
    abstract fun debugModule(bbDebug: BBDebug): DebugModuleHost

    @Binds @IntoSet
    abstract fun recorder(recorderModule: RecorderModule.Factory): DebugModule.Factory<out DebugModule>

    @Binds @IntoSet
    abstract fun acsDebug(recorderModule: ACSDebugModule.Factory): DebugModule.Factory<out DebugModule>

    @Binds @IntoSet
    abstract fun apkDebug(recorderModule: AppInfoModule.Factory): DebugModule.Factory<out DebugModule>

    @Binds @IntoSet
    abstract fun buildPropPrinter(recorderModule: BuildPropPrinter.Factory): DebugModule.Factory<out DebugModule>

    @Binds @IntoSet
    abstract fun envPrinter(recorderModule: EnvPrinter.Factory): DebugModule.Factory<out DebugModule>

    @Binds @IntoSet
    abstract fun rxsDebug(recorderModule: RXSDebugModule.Factory): DebugModule.Factory<out DebugModule>

    @Binds @IntoSet
    abstract fun sysInfo(recorderModule: SysInfoModule.Factory): DebugModule.Factory<out DebugModule>

    @Binds @IntoSet
    abstract fun uriPermissions(uriPermissions: UriPermissions.Factory): DebugModule.Factory<out DebugModule>

}
