package eu.darken.bb.processor.core.mm.archive

import eu.darken.bb.processor.core.mm.Props
import kotlinx.coroutines.flow.Flow
import okio.Source

interface ArchiveRef {
    suspend fun getProps(): ArchiveProps

    suspend fun extract(): Flow<Pair<Props, Source?>>
}