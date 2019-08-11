package eu.darken.bb.debug

import eu.thedarken.sdm.tools.debug.DebugModuleHost

interface DebugModule {

    interface Factory<T : DebugModule> {
        fun create(host: DebugModuleHost): T
    }

}