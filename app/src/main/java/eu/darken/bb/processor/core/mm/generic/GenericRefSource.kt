package eu.darken.bb.processor.core.mm.generic

import eu.darken.bb.App
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.GatewaySwitch
import eu.darken.bb.common.files.core.lookup
import eu.darken.bb.common.files.core.read
import eu.darken.bb.processor.core.mm.BaseRefSource
import eu.darken.bb.processor.core.mm.Props
import eu.darken.bb.processor.core.mm.archive.ArchiveRefSource
import okio.Source
import timber.log.Timber

class GenericRefSource(
        private val sourceGenerator: (Props) -> Source,
        propGenerator: () -> Props
) : BaseRefSource() {

    constructor(
            gateway: GatewaySwitch,
            path: APath,
            providedProps: Props? = null
    ) : this(
            sourceGenerator = { path.read(gateway) },
            propGenerator = { providedProps ?: path.lookup(gateway).toProps() }
    )

    override val props: Props by lazy { propGenerator() }

    override fun doOpen(): Source {
        Timber.tag(ArchiveRefSource.TAG).v("Opening source for %s", props)
        return sourceGenerator(props)
    }

    companion object {
        val TAG = App.logTag("MMDataRepo", "MMRef", "GenericRefSource")
    }
}