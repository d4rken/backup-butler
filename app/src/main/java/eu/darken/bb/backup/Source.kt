package eu.darken.bb.backup

import eu.darken.bb.common.file.SFile


interface Source {
    enum class Type {
        APP_BACKUP
    }

    interface Processor {
        fun backup(config: Config): Piece

        fun restore(piece: Piece)
    }

    interface Piece {
        interface MetaData {
            val sourceType: Type
        }

        val metadata: MetaData
        val files: List<SFile>
    }

    interface Config {
        val sourceType: Type
    }
}