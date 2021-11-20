package eu.darken.bb.common.sharedresource

data class Resource<T>(private val _item: T, val keepAlive: KeepAlive) : KeepAlive by keepAlive {
    val item: T
        get() {
            check(!keepAlive.isClosed) { "Trying to access closed resource!" }
            return _item
        }
}