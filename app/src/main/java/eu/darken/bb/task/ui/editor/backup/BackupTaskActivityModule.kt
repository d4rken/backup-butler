package eu.darken.bb.task.ui.editor.backup

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.task.ui.editor.backup.destinations.DestinationsFragment
import eu.darken.bb.task.ui.editor.backup.destinations.DestinationsFragmentModule
import eu.darken.bb.task.ui.editor.backup.intro.IntroFragment
import eu.darken.bb.task.ui.editor.backup.intro.IntroFragmentModule
import eu.darken.bb.task.ui.editor.backup.sources.SourcesFragment
import eu.darken.bb.task.ui.editor.backup.sources.SourcesFragmentModule

@Module
abstract class BackupTaskActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(BackupTaskActivityVDC::class)
    abstract fun taskActivity(factory: BackupTaskActivityVDC.Factory): VDCFactory<out VDC>

    @PerFragment
    @ContributesAndroidInjector(modules = [IntroFragmentModule::class])
    abstract fun introFragment(): IntroFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [SourcesFragmentModule::class])
    abstract fun sourcesFragment(): SourcesFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [DestinationsFragmentModule::class])
    abstract fun destinationsFragment(): DestinationsFragment
}