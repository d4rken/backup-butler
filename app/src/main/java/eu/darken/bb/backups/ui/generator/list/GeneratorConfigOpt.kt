package eu.darken.bb.backups.ui.generator.list

import eu.darken.bb.backups.core.SpecGenerator
import java.util.*

data class GeneratorConfigOpt(
        val generatorId: UUID,
        val config: SpecGenerator.Config?
) {
    constructor(generatorId: UUID) : this(generatorId, null)
    constructor(config: SpecGenerator.Config) : this(config.generatorId, config)
}