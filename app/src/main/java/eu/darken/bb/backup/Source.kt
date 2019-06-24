package eu.darken.bb.backup

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.source.app.AppSourceConfig
import eu.darken.bb.backup.source.file.FileSourceConfig
import eu.darken.bb.common.file.SFile


interface Source {


    enum class Type {
        APP_BACKUP, FILE
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
        companion object {
            val MOSHI_FACTORY = PolymorphicJsonAdapterFactory.of(Source.Config::class.java, "sourceType")
                    .withSubtype(AppSourceConfig::class.java, Source.Type.APP_BACKUP.name)
                    .withSubtype(FileSourceConfig::class.java, Source.Type.FILE.name)
        }

        val sourceType: Type
    }
}