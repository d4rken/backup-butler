package eu.darken.bb.repos.core

data class RepoStatus(
        val ref: RepoRef,
        val info: RepoInfo?,
        val error: Throwable? = null
)