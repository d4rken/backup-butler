package eu.darken.bb.common.root.core.javaroot

import android.content.Context
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.files.core.local.root.FileOpsConnection
import eu.darken.bb.common.files.core.local.root.FileOpsHost
import eu.darken.bb.common.pkgs.pkgops.root.PkgOpsConnection
import eu.darken.bb.common.pkgs.pkgops.root.PkgOpsHost
import eu.darken.rxshell.cmd.Cmd
import eu.darken.rxshell.cmd.RxCmdShell
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JavaRootConnectionImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileOpsHost: Lazy<FileOpsHost>,
    private val pkgOpsHost: Lazy<PkgOpsHost>
) : JavaRootConnection.Stub() {

    override fun checkBase(): String {
        val sb = StringBuilder()
        sb.append("Our pkg: ${context.packageName}\n")
        val ids = Cmd.builder("id").submit(RxCmdShell.Builder().build()).blockingGet()
        sb.append("Shell ids are: ${ids.merge()}\n")
        val result = sb.toString()
        Timber.tag(JavaRootHost.TAG).i("checkBase(): %s", result)
        return result
    }

    override fun getFileOps(): FileOpsConnection = fileOpsHost.get()

    override fun getPkgOps(): PkgOpsConnection = pkgOpsHost.get()
}