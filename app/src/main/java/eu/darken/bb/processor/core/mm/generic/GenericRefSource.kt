package eu.darken.bb.processor.core.mm.generic

import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.GatewaySwitch
import eu.darken.bb.common.files.core.lookup
import eu.darken.bb.common.files.core.read
import eu.darken.bb.processor.core.mm.BaseRefSource
import eu.darken.bb.processor.core.mm.Props
import okio.Source
import timber.log.Timber

class GenericRefSource(
    private val sourceGenerator: suspend (Props) -> Source,
    private val propGenerator: suspend () -> Props
) : BaseRefSource() {

    // TODO caching
    override suspend fun getProps() = propGenerator()

    override suspend fun doOpen(): Source {
        Timber.tag(TAG).v("Opening source for %s", getProps())
        return sourceGenerator(getProps())
    }

    companion object {
        val TAG = logTag("MMDataRepo", "MMRef", "GenericRefSource")

        fun create(
            gateway: GatewaySwitch,
            path: APath,
            label: String? = null,
            providedProps: Props? = null
        ) = GenericRefSource(
            sourceGenerator = { path.read(gateway) },
            propGenerator = { providedProps ?: path.lookup(gateway).toProps(label) }
        )
    }
}