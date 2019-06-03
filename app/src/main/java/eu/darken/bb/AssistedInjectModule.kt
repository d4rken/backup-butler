package eu.darken.bb

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module


@AssistedModule
@Module(includes = [AssistedInject_AssistedInjectModule::class])
internal abstract class AssistedInjectModule