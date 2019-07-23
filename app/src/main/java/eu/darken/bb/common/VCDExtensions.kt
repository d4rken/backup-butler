package eu.darken.bb.common

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCSource

@MainThread
inline fun <reified VM : VDC> Fragment.vdcs(
        noinline factoryProducer: (() -> VDCSource.Factory)
) = viewModels<VM> { factoryProducer.invoke().create(this, arguments) }

@MainThread
inline fun <reified VM : VDC> Fragment.vdcsAssisted(
        noinline factoryProducer: (() -> VDCSource.Factory),
        noinline constructorCall: ((VDCFactory<out VDC>, SavedStateHandle) -> VDC)
) = viewModels<VM> { factoryProducer.invoke().create(this, arguments, constructorCall) }

@MainThread
inline fun <reified VM : VDC> ComponentActivity.vdcs(
        noinline factoryProducer: (() -> VDCSource.Factory)
) = viewModels<VM> { factoryProducer.invoke().create(this, intent.extras) }

@MainThread
inline fun <reified VM : VDC> ComponentActivity.vdcsAssisted(
        noinline factoryProducer: (() -> VDCSource.Factory),
        noinline constructorCall: ((VDCFactory<out VDC>, SavedStateHandle) -> VDC)
) = viewModels<VM> { factoryProducer.invoke().create(this, intent.extras, constructorCall) }