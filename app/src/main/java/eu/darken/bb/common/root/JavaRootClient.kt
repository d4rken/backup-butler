package eu.darken.bb.common.root

import android.content.Context
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.root.librootjava.RootIPCReceiver
import javax.inject.Inject

@PerApp
class JavaRootClient @Inject constructor(
        @AppContext private val context: Context
) {
//    data class Session()
//
//    fun startSession() : Observable<Session> {
//
//    }

    private val ipcReceiver = object : RootIPCReceiver<IIPC>(context, 0) {
        override fun onConnect(ipc: IIPC?) {
            TODO("not implemented")
        }

        override fun onDisconnect(ipc: IIPC?) {
            TODO("not implemented")
        }

    }
}