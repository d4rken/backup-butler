package eu.darken.bb.backup.ui.generator.list

import eu.darken.bb.backup.core.Generator

data class GeneratorConfigOpt(
        val generatorId: Generator.Id,
        val config: Generator.Config?
) {
    constructor(generatorId: Generator.Id) : this(generatorId, null)
    constructor(config: Generator.Config) : this(config.generatorId, config)
}