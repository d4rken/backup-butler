package eu.darken.bb.processor.core.mm.file

import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.APathGateway
import eu.darken.bb.common.file.core.APathLookup
import eu.darken.bb.processor.core.mm.BaseRefSource
import eu.darken.bb.processor.core.mm.Props
import okio.Source

class APathRefResource<PathType : APath, GateType : APathGateway<in PathType, out APathLookup<PathType>>>(
        private val gateway: GateType,
        private val path: PathType,
        providedProps: Props? = null
) : BaseRefSource() {

    private val autoGenProps: Props by lazy {
        gateway.lookup(path).toProps()
    }

    override val props: Props = providedProps ?: autoGenProps

    override fun doOpen(): Source {
        return gateway.read(path)
    }

}