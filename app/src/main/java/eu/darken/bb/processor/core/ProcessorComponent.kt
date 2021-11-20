package eu.darken.bb.processor.core

import dagger.BindsInstance
import dagger.hilt.DefineComponent
import dagger.hilt.components.SingletonComponent

@ProcessorScope
@DefineComponent(parent = SingletonComponent::class)
interface ProcessorComponent {

    @DefineComponent.Builder
    interface Builder {

        fun coroutineScope(@BindsInstance coroutineScope: ProcessorCoroutineScope): Builder

        fun build(): ProcessorComponent
    }
}