package eu.darken.bb.backup

import eu.darken.bb.common.Jsonable
import java.util.*

interface BackupTask : Jsonable {
    val id: UUID

    val sources: List<Source>

    val destinations: List<Destination>

    interface Source {

        fun provide(): Pair<MetaData, List<SFile>>

        interface MetaData
        interface SFile
    }

    interface Destination

    interface Result
}