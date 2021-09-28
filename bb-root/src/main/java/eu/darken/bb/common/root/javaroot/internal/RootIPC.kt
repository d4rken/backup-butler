/* Copyright 2018 Jorrit 'Chainfire' Jongma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.darken.bb.common.root.javaroot.internal

import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.IBinder.DeathRecipient
import android.os.RemoteException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.debug.logging.Logging.Priority.*
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.root.javaroot.IRootIPC
import java.util.*
import java.util.concurrent.TimeoutException

/**
 * Binder-based IPC server for the root process<br></br>
 * <br></br>
 * This class wraps the supplied Binder interface in its own helper (primarily to keep track of
 * the non-root processes' state), and broadcasts the wrapper to the non-root process.

 * @param packageName           Package name of process to send Binder to. Use BuildConfig.APPLICATION_ID (double check you're importing the correct BuildConfig!) for convenience
 * @param ipc                   Binder object to wrap and send out
 * @param code                  User-value, should be unique per Binder
 * @param timeout               How long to wait for the other process to initiate the connection, 0 to wait forever
 * @throws TimeoutException If the connection times out

 * @see RootIPCReceiver
 */
class RootIPC @AssistedInject constructor(
    @Assisted private val packageName: String,
    @Assisted private val userBinder: IBinder,
    @Assisted private val code: Int,
    @Assisted private val timeout: Long,
    @Assisted private val blocking: Boolean,
    private val reflectionBroadcast: ReflectionBroadcast,
) {

    private val helloWaiter = Object()
    private val byeWaiter = Object()

    data class Connection(val binder: IBinder, val deathRecipient: DeathRecipient)

    val connections = mutableListOf<Connection>()

    @Volatile private var connectionSeen = false

    /**
     * Our own wrapper around the supplied Binder interface, which allows us to keep track of
     * non-root process' state and connection state.
     */
    private val internalBinder: IBinder = object : IRootIPC.Stub() {
        override fun hello(_self: IBinder) {
            log(TAG) { "hello(self=$_self)" }
            // incoming connection from the non-root process
            var self: IBinder? = _self

            // receive notifications when that process dies
            val deathRecipient: DeathRecipient = object : DeathRecipient {
                override fun binderDied() {
                    getConnection(this)?.let { bye(it.binder) }
                }
            }
            try {
                log(TAG) { "linkToDeath(deathRecipient=$deathRecipient)" }
                self!!.linkToDeath(deathRecipient, 0)
            } catch (e: RemoteException) {
                log(TAG) { "linkToDeath() failed, dead? ${e.asLog()}" }
                // it's already dead!
                self = null
            }

            // if still alive, record the connection
            self?.let {
                log(TAG) { "Adding new connection..." }
                synchronized(connections) {
                    connections.add(Connection(it, deathRecipient))
                    connectionSeen = true
                }
                log(TAG) { "Notifying hello waiters" }
                synchronized(helloWaiter) { helloWaiter.notifyAll() }
            }
        }

        // this is the originally supplied Binder interface
        override fun getUserIPC(): IBinder = this@RootIPC.userBinder.also {
            log(TAG, VERBOSE) { "getUserIPC($it)" }
        }

        override fun bye(self: IBinder) {
            log(TAG) { "self(self=$self)" }
            // The non-root process is either informing us it is going away, or it already died
            synchronized(connections) {
                getConnection(self)?.let { conn ->
                    try {
                        conn.binder.unlinkToDeath(conn.deathRecipient, 0)
                    } catch (e: Exception) {
                        log(TAG) { "unlinkToDeath() failed: ${e.asLog()}" }
                    }
                    connections.remove(conn)
                }
            }
            synchronized(byeWaiter) { byeWaiter.notifyAll() }
        }
    }

    init {
        log(TAG) { "init(): $packageName, $userBinder, $code, $timeout, $reflectionBroadcast" }
        require(timeout >= 0L) { "Timeout can't be negative: $timeout" }
    }

    fun broadcast() {
        log(TAG) { "broadcast()" }

        broadcastIPC()

        if (timeout > 0) {
            synchronized(helloWaiter) {
                if (!haveClientsConnected()) {
                    try {
                        log(TAG) { "Waiting for clients (on timeout)" }
                        helloWaiter.wait(timeout)
                    } catch (e: InterruptedException) {
                        // expected, do nothing
                    }
                }
                if (!haveClientsConnected()) {
                    throw TimeoutException("Timeout waiting for IPC connection")
                }
            }
        }

        if (!blocking) return

        // this will loop until all connections have said goodbye or their processes have died
        synchronized(byeWaiter) {
            while (!haveAllClientsDisconnected()) {
                try {
                    log(TAG) { "Waiting for clients to disconnect (no timeout)" }
                    byeWaiter.wait()
                } catch (e: InterruptedException) {
                    log(TAG) { "Finished due to interrupt." }
                    return@synchronized
                }
            }

            log(TAG) { "Finished as all clients have disconnected." }
        }
    }

    fun haveClientsConnected(): Boolean = synchronized(connections) { connectionSeen }

    fun haveAllClientsDisconnected(): Boolean = synchronized(connections) { connectionSeen && connectionCount == 0 }

    /**
     * Wrap the binder in an intent and broadcast it to packageName
     * Uses the reflected sendBroadcast method that doesn't require us to have a context
     * You may call this manually to re-broadcast the interface
     */
    private fun broadcastIPC() {
        val bundle = Bundle().apply {
            putBinder(RootIPCReceiver.BROADCAST_BINDER, internalBinder)
            putInt(RootIPCReceiver.BROADCAST_CODE, code)
        }

        val intent = Intent().apply {
            setPackage(packageName)
            action = RootIPCReceiver.BROADCAST_ACTION
            flags = Intent.FLAG_RECEIVER_FOREGROUND
            putExtra(RootIPCReceiver.BROADCAST_EXTRA, bundle)
        }

        reflectionBroadcast.sendBroadcast(intent)
    }


    val connectionCount: Int
        get() = synchronized(connections) {
            pruneConnections()
            connections.size
        }

    /**
     *This should never actually have any effect due to our DeathRecipients.
     */
    private fun pruneConnections() {
        synchronized(connections) {
            if (connections.size == 0) return
            connections.removeAll { con ->
                !con.binder.isBinderAlive.also {
                    log { "pruneConnections() $con: isBinderAlive=$it" }
                }
            }
            if (!connectionSeen && connections.size > 0) {
                connectionSeen = true
                synchronized(helloWaiter) { helloWaiter.notifyAll() }
            }
            if (connections.size == 0) {
                synchronized(byeWaiter) { byeWaiter.notifyAll() }
            }
        }
    }

    internal fun getConnection(binder: IBinder): Connection? = synchronized(connections) {
        pruneConnections()
        connections.singleOrNull { it.binder === binder }
    }

    internal fun getConnection(deathRecipient: DeathRecipient): Connection? = synchronized(connections) {
        pruneConnections()
        connections.singleOrNull { it.deathRecipient === deathRecipient }
    }

    companion object {
        private val TAG = logTag("Root", "IPC")
    }

    @AssistedFactory
    interface Factory {
        fun create(
            packageName: String,
            userProvidedBinder: IBinder,
            code: Int = 0,
            timeout: Long = 30 * 1000,
            blocking: Boolean = true,
        ): RootIPC
    }
}