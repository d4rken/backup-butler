package eu.darken.bb.backup.source.app

import eu.darken.bb.backup.Source

data class AppSourceConfig(private val packages: List<String>) : Source.Config {
    override val sourceType: Source.Type = Source.Type.APP_BACKUP


}