package eu.darken.bb

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.main.core.service.ExampleService


@Module
internal abstract class ServiceBinder {
    @ContributesAndroidInjector
    internal abstract fun exampleService(): ExampleService

}