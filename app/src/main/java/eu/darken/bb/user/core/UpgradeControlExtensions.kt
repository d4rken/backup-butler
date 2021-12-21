package eu.darken.bb.user.core

import eu.darken.bb.user.core.UpgradeInfo.Status.PRO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


suspend fun UpgradeControl.isPro(): Boolean = this.state.map { it.status == PRO }.first()