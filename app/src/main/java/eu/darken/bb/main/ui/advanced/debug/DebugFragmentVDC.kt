package eu.darken.bb.main.ui.advanced.debug

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.root.javaroot.JavaRootClient
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.rxshell.cmd.Cmd
import eu.darken.rxshell.cmd.RxCmdShell
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class DebugFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val javaRootClient: JavaRootClient,
) : SmartVDC() {

    data class RootCheckState(
        val isWorking: Boolean = false,
        val output: String = "",
        val success: Int = 0,
    )

    val rootResult = MutableLiveData(RootCheckState())

    fun performJavaRootCheck() {
        Single
            .fromCallable {
                javaRootClient.runSessionAction {
                    log { "runSessionAction { checkBase() }" }
                    it.ipc.checkBase()
                }
            }
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { rootResult.postValue(RootCheckState(isWorking = true)) }
            .subscribe({ result ->
                log { "performJavaRootCheck(): $result" }
                rootResult.postValue(
                    RootCheckState(
                        output = result,
                        success = 1
                    )
                )
            }, { error ->
                log { "Java check failed: ${error.asLog()}" }
                rootResult.postValue(RootCheckState(output = error.asLog(), success = -1))
            })
            .withScopeVDC(this)
    }

    fun performShellRootCheck() {
        Cmd.builder("id")
            .submit(RxCmdShell.builder().root(true).build())
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                rootResult.postValue(RootCheckState(isWorking = true))
            }
            .subscribe({ result ->
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
            }, { error ->
                log { "Shell check failed: ${error.asLog()}" }
                rootResult.postValue(RootCheckState(output = error.asLog(), success = -1))
            })
            .withScopeVDC(this)
    }
}