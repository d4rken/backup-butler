package eu.darken.bb.backup.destination

import eu.darken.bb.backup.Destination
import java.io.File

data class LocalStorageDestination(val path: File) : Destination