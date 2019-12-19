package eu.darken.bb

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.common.pkgs.pkgops.installer.InstallerReceiver

@Module
internal abstract class ReceiverBinder {
    @ContributesAndroidInjector
    internal abstract fun installerReceiver(): InstallerReceiver

}