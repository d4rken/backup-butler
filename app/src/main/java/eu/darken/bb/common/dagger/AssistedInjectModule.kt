package eu.darken.bb.common.dagger

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module


@AssistedModule
@Module(includes = [AssistedInject_AssistedInjectModule::class])
interface AssistedInjectModule