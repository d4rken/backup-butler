package eu.darken.bb.common.debug

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class DebugCoroutineScope @Inject constructor() : CoroutineScope {
    // TODO use extra threads
    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Default
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class DebugScope
