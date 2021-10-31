package eu.darken.bb.backup.ui.generator.list

import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.lists.differ.DifferItem

data class GeneratorConfigOpt(
    val generatorId: Generator.Id,
    val config: Generator.Config?
) : DifferItem {
    constructor(generatorId: Generator.Id) : this(generatorId, null)
    constructor(config: Generator.Config) : this(config.generatorId, config)

    override val stableId: Long = generatorId.hashCode().toLong()
}