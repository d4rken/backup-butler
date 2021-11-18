package eu.darken.bb.common

import kotlinx.coroutines.Job

data class Operation(
    val label: CaString,
    val description: CaString? = null,
    val job: Job
)