package eu.darken.bb.common.dagger

import eu.darken.bb.common.VDC

interface SimpleVDCFactory<T : VDC> : VDCFactory<T> {
    fun create(): T
}