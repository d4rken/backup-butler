package eu.darken.bb.common.navigation

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import eu.darken.bb.common.debug.timber.v

fun NavController.navigateIfNotThere(@IdRes resId: Int, args: Bundle? = null) {
    if (currentDestination?.id == resId) return
    navigate(resId, args)
}

fun NavController.isGraphSet(): Boolean = try {
    graph
    true
} catch (e: IllegalStateException) {
    false
}

fun NavController.doNavigate(direction: NavDirections) {
    val action = currentDestination?.getAction(direction.actionId)
    v { "doNavigate(direction=$direction) using $action" }
    action?.let { navigate(direction) }
}
