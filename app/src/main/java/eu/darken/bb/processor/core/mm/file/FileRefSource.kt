package eu.darken.bb.processor.core.mm.file

import eu.darken.bb.common.files.core.local.performLookup
import eu.darken.bb.common.files.core.local.toLocalPath
import eu.darken.bb.processor.core.mm.BaseRefSource
import eu.darken.bb.processor.core.mm.Props
import okio.Source
import okio.source
import java.io.File

class FileRefSource(
        private val file: File,
        providedProps: Props? = null
) : BaseRefSource() {

    private val autoGenProps: Props by lazy {
        file.toLocalPath().performLookup().toProps()
    }

    override val props: Props = providedProps ?: autoGenProps

    override fun doOpen(): Source {
        return file.source()
    }
}