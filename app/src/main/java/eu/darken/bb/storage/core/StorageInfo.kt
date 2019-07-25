package eu.darken.bb.storage.core

data class StorageInfo(
        val ref: StorageRef,
        val config: StorageConfig? = null,
        val status: Status? = null,
        val error: Throwable? = null
) {

    class Status

}