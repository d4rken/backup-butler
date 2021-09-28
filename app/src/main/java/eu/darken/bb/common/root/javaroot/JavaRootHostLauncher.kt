package eu.darken.bb.common.root.javaroot

import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.local.root.FileOpsClient
import eu.darken.bb.common.pkgs.pkgops.root.PkgOpsClient
import eu.darken.bb.common.root.javaroot.internal.RootHostLauncher
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class JavaRootHostLauncher @Inject constructor(
    private val rootHostLauncher: RootHostLauncher,
    private val fileOpsClientFactory: FileOpsClient.Factory,
    private val pkgOpsClientFactory: PkgOpsClient.Factory,
) {
    fun create(): Observable<JavaRootClient.Connection> = rootHostLauncher
        .createConnection(JavaRootConnection::class, JavaRootHost::class)
        .doOnSubscribe { log(TAG) { "Initiating connection to host." } }
        .map { ipc ->
            JavaRootClient.Connection(
                ipc = ipc,
                clientModules = listOf(
                    fileOpsClientFactory.create(ipc.fileOps),
                    pkgOpsClientFactory.create(ipc.pkgOps)
                )
            )
        }
        .doOnNext { log(TAG) { "Connection available." } }
        .doFinally { log(TAG) { "Connection unavailable." } }

    companion object {
        private val TAG = logTag("Root", "Java", "Host", "Launcher")
    }
}