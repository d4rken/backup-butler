package eu.darken.bb.user.core

import eu.darken.bb.common.BuildConfigWrap
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.flow.setupCommonEventHandlers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpgradeControl @Inject constructor(
    @AppScope private val scope: CoroutineScope,
) {
    private val internalState = DynamicStateFlow(TAG, scope) {
        UpgradeInfo(
            state = when (BuildConfigWrap.isBetaBuild) {
                true -> UpgradeInfo.State.PRO
                false -> UpgradeInfo.State.BASIC
            },
        )
    }

    val state: Flow<UpgradeInfo> = internalState.flow
        .setupCommonEventHandlers(TAG) { "state" }

    companion object {
        val TAG = logTag("UpgradeControl")
    }
}

