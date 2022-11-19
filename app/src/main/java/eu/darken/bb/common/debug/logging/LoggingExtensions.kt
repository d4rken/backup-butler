package eu.darken.bb.common.debug.logging

/**
 * Inspired by https://github.com/PaulWoitaschek/Slimber
 */

//inline fun ifTrees(block: () -> Unit) {
//    if (Timber.treeCount() == 0) return
//    block()
//}
//
//inline fun log(
//    tag: String? = null,
//    throwable: Throwable? = null,
//    priority: Int,
//    message: () -> String,
//) = ifTrees {
//    tag?.let { Timber.tag(it) }
//    Timber.log(priority, throwable, message())
//}
//
//inline fun v(
//    tag: String? = null,
//    throwable: Throwable? = null,
//    message: () -> String,
//) = log(
//    tag = tag,
//    priority = Log.VERBOSE,
//    throwable = throwable,
//    message = message
//)
//
//inline fun d(
//    tag: String? = null,
//    throwable: Throwable? = null,
//    message: () -> String,
//) = log(
//    tag = tag,
//    priority = Log.DEBUG,
//    throwable = throwable,
//    message = message
//)
//
//inline fun i(
//    tag: String? = null,
//    throwable: Throwable? = null,
//    message: () -> String,
//) = log(
//    tag = tag,
//    priority = Log.INFO,
//    throwable = throwable,
//    message = message
//)
//
//inline fun w(
//    tag: String? = null,
//    throwable: Throwable? = null,
//    message: () -> String,
//) = log(
//    tag = tag,
//    priority = Log.WARN,
//    throwable = throwable,
//    message = message
//)
//
//inline fun e(
//    tag: String? = null,
//    throwable: Throwable? = null,
//    message: () -> String,
//) = log(
//    tag = tag,
//    priority = Log.ERROR,
//    throwable = throwable,
//    message = message
//)
