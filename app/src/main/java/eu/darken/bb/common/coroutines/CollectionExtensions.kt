package eu.darken.bb.common.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun <A> Collection<A>.forEachParallel(f: suspend (A) -> Unit): Unit = runBlocking {
    map { async(Dispatchers.Default) { f(it) } }.forEach { it.await() }
}