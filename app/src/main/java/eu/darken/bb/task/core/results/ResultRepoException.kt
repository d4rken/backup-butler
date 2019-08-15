package eu.darken.bb.task.core.results

class ResultRepoException(
        override val message: String?,
        override val cause: Throwable?
) : Exception(message, cause)