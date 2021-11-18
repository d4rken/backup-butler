package eu.darken.bb.main.ui.debug

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.root.javaroot.JavaRootClient
import eu.darken.bb.common.smart.SmartVDC
import eu.darken.rxshell.cmd.Cmd
import eu.darken.rxshell.cmd.RxCmdShell
import javax.inject.Inject

@HiltViewModel
class DebugFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val javaRootClient: JavaRootClient,
    private val dispatcherProvider: DispatcherProvider,
) : SmartVDC(dispatcherProvider) {

    data class RootCheckState(
        val isWorking: Boolean = false,
        val output: String = "",
        val success: Int = 0,
    )

    val rootResult = MutableLiveData(RootCheckState())

    fun performJavaRootCheck() = launch {
        rootResult.postValue(RootCheckState(isWorking = true))
        try {
            val result = javaRootClient.runSessionAction {
                log { "runSessionAction { checkBase() }" }
                it.ipc.checkBase()
            }
            log { "performJavaRootCheck(): $result" }
            rootResult.postValue(
                RootCheckState(
                    output = result,
                    success = 1
                )
            )
        } catch (e: Exception) {
            log { "Java check failed: ${e.asLog()}" }
            rootResult.postValue(RootCheckState(output = e.asLog(), success = -1))
        }
    }

    fun performShellRootCheck() = launch {
        rootResult.postValue(RootCheckState(isWorking = true))
        try {
            val shell = RxCmdShell.builder().root(true).build()
            val result = Cmd.builder("id").submit(shell).blockingGet()

            log { "performShellRootCheck(): $result" }

            rootResult.postValue(
                RootCheckState(
                    output = result.merge().joinToString("\n"),
                    success = when (result.exitCode) {
                        Cmd.ExitCode.OK -> 1
                        else -> -1
                    }
                )
            )
        } catch (e: Exception) {
            log { "Shell check failed: ${e.asLog()}" }
            rootResult.postValue(RootCheckState(output = e.asLog(), success = -1))
        }

    }
}