package eu.darken.bb

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.common.root.core.javaroot.pkgops.InstallerReceiver

@Module
internal abstract class ReceiverBinder {
    @ContributesAndroidInjector
    internal abstract fun installerReceiver(): InstallerReceiver

}