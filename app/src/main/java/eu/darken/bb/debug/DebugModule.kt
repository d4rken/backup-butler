package eu.darken.bb.debug

interface DebugModule {

    interface Factory<T : DebugModule> {
        fun create(host: DebugModuleHost): T
    }

}