package eu.darken.bb.common.vdc

interface SimpleVDCFactory<T : VDC> : VDCFactory<T> {
    fun create(): T
}