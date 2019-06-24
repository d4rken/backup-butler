package eu.darken.bb.backup.source.file

import eu.darken.bb.backup.Source

data class FileSourceConfig(private val paths: List<String>) : Source.Config {
    override val sourceType: Source.Type = Source.Type.FILE
}