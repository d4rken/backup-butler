package eu.darken.bb.backup

interface Destination {
    enum class Type {
        LOCAL_STORAGE
    }

    interface Processor {
        fun store(piece: Source.Piece): Result

        fun load(config: Config): List<Source.Piece>
    }

    interface Config {
        val destinationType: Type
    }

    interface Result
}