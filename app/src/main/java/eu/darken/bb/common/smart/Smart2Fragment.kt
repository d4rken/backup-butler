package eu.darken.bb.common.smart

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.error.asErrorDialogBuilder
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.navigation.popBackStack
import eu.darken.bb.common.observe2


abstract class Smart2Fragment(@LayoutRes layoutRes: Int?) : SmartFragment(layoutRes ?: 0) {

    constructor() : this(null)

    abstract val ui: ViewBinding?
    abstract val vdc: Smart2VDC

    var onErrorEvent: ((Throwable) -> Boolean)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vdc.navEvents.observe2(this, ui) {
            log { "navEvents: $it" }
            // TODO add alternative null value behavior "finish behavior"
            it?.run { doNavigate(this) } ?: popBackStack()
        }

        vdc.errorEvents.observe2(this, ui) {
            val showDialog = onErrorEvent?.invoke(it) ?: true
            if (showDialog) it.asErrorDialogBuilder(requireContext()).show()
        }
    }
}
