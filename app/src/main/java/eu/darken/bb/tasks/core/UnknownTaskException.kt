package eu.darken.bb.tasks.core

class UnknownTaskException : Exception {
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
}