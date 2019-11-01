package eu.darken.bb.common.debug

interface DebugModule {

    interface Factory<T : DebugModule> {
        fun create(host: DebugModuleHost): T
    }

}