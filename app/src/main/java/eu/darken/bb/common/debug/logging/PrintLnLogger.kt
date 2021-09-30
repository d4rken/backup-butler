package eu.darken.bb.common.debug.logging

class PrintLnLogger : Logging.Logger {

    override fun isLoggable(priority: Logging.Priority): Boolean = true

    override fun log(priority: Logging.Priority, tag: String, message: String, metaData: Map<String, Any>?) {
        println("${System.currentTimeMillis()} ${priority.shortLabel}/$tag: $message")
    }
}