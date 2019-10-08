package eu.darken.bb.storage.core

data class SimpleStrategy(
        override val type: Storage.Strategy.Type = Storage.Strategy.Type.SIMPLE
) : Storage.Strategy {

    init {
        require(type == Storage.Strategy.Type.SIMPLE)
    }

}