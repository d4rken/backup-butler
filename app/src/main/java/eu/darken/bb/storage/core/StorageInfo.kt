package eu.darken.bb.storage.core

data class StorageInfo(
        val ref: Storage.Ref,
        val config: Storage.Config? = null,
        val status: Status? = null,
        val error: Throwable? = null
) {

    class Status

}