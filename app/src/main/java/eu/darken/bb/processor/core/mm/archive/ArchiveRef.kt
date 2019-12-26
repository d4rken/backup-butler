package eu.darken.bb.processor.core.mm.archive

import eu.darken.bb.processor.core.mm.Props
import okio.Source

interface ArchiveRef {
    val props: ArchiveProps

    fun openArchive(): Sequence<Pair<Props, Source?>>
}