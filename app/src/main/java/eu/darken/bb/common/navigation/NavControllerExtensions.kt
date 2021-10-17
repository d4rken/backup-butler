package eu.darken.bb.common.navigation

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import eu.darken.bb.common.debug.logging.Logging.Priority.ERROR
import eu.darken.bb.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.bb.common.debug.logging.log

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

fun NavController.doNavigate(direction: NavDirections): Boolean {
    val action = currentDestination?.getAction(direction.actionId)
    log(
        priority = if (action != null) VERBOSE else ERROR
    ) {
        "doNavigate(direction=$direction) using $action"
    }
    return action?.let {
        navigate(direction)
        true
    } ?: false
}
