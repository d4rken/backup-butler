package eu.darken.bb.repos.ui.list

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class RepoListFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(RepoListFragmentVDC::class)
    abstract fun repolistVDC(model: RepoListFragmentVDC.Factory): VDCFactory<out VDC>
}

