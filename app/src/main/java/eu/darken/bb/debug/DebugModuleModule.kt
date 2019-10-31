package eu.darken.bb.debug

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import eu.darken.bb.debug.modules.EnvPrinter
import eu.darken.bb.debug.modules.RXSDebugModule
import eu.darken.bb.debug.modules.SysInfoModule
import eu.darken.bb.debug.modules.UriPermissions
import eu.darken.bb.debug.recording.core.RecorderModule


@Module
abstract class DebugModuleModule {

    @Binds
    abstract fun debugModule(bbDebug: BBDebug): DebugModuleHost

    @Binds @IntoSet
    abstract fun recorder(recorderModule: RecorderModule.Factory): DebugModule.Factory<out DebugModule>

    @Binds @IntoSet
    abstract fun acsDebug(recorderModule: ACSDebugModule.Factory): DebugModule.Factory<out DebugModule>

    @Binds @IntoSet
    abstract fun apkDebug(recorderModule: ApkInfoModule.Factory): DebugModule.Factory<out DebugModule>

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
