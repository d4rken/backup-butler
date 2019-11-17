package eu.darken.bb.main.ui.start.debug

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import butterknife.BindView
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.root.javaroot.JavaRootClient
import eu.darken.bb.common.root.javaroot.fileops.toInputStream
import eu.darken.bb.common.root.javaroot.fileops.toOutputStream
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import io.reactivex.disposables.Disposables
import timber.log.Timber
import javax.inject.Inject


class DebugFragment : SmartFragment(), AutoInject {
    companion object {
        fun newInstance(): Fragment = DebugFragment()
        val TAG = App.logTag("Debug", "Fragment")
    }


    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: DebugFragmentVDC by vdcs { vdcSource }

    init {
        layoutRes = R.layout.debug_fragment
    }

    @Inject lateinit var javaRootClient: JavaRootClient

    @BindView(R.id.librootjava_start) lateinit var librootJavaTest: Button

    var javaRootDisp = Disposables.disposed()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        librootJavaTest.setOnClickListener {
            if (!javaRootDisp.isDisposed) {
                javaRootDisp.dispose()
            } else {
                javaRootDisp = javaRootClient.session.subscribe { session ->
                    Timber.tag(TAG).i("The binder is here: %s", session)
                    session.ipc.sayHi()
                    val fileOps = session.ipc.fileOps
                    val ris = fileOps.readFile("/data/data/eu.thedarken.sdm/shared_prefs/global_preferences.xml")
//                    ris.toInputStream().bufferedReader().lines().forEach {
//                        Timber.tag(TAG).i(it)
//                    }
                    val ros = fileOps.writeFile("/data/data/eu.thedarken.sdm/shared_prefs/global_preferences.xml.bak")
                    ris.toInputStream().use { inStream ->
                        ros.toOutputStream().use { outStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                }
            }

        }

        super.onViewCreated(view, savedInstanceState)
    }
//
//    override fun onDestroy() {
//        /* Disconnect and release resources. If onConnect() is still running, disconnect will occur
//           after its completion, but this call will return immediately. */
//        ipcReceiver.release()
//
//        super.onDestroy()
//    }
//
//    private fun uiLog(msg: String?) {
//        // Log a string to our textView, making sure the addition runs on the UI thread
//        requireActivity().runOnUiThread(Runnable { textView.append(msg + "\n") })
//        Timber.tag("uilog").i("%s", msg)
//    }
//
//    private val ipcReceiver by lazy<RootIPCReceiver<IIPC>> {
//        object : RootIPCReceiver<IIPC>(requireActivity(), 0) {
//            override fun onConnect(ipc: IIPC) {
//                /* This is always called from a background thread, so you can do blocking operations
//               here without issue. Keep in mind that due to this, the activity may actually be
//               destroyed while this callback is still running.
//
//               As always long-running code should be executed in a service rather than in an
//               activity, but that is beyond the scope of this example.
//
//               If release() is called from onDestroy, this will schedule a disconnect, and
//               you can use the isDisconnectScheduled() call as a trigger to abort.
//
//               If you're done with the IPC interface at the end of this method, call disconnect().
//               You shouldn't store the interface itself, but if you don't disconnect() you can call
//               RootIPCReceiver.getIPC() later.
//            */
//                Timber.d("IPC", "onConnect")
//                try {
//                    uiLog("")
//                    uiLog("Connected")
//
//                    // Get the other end's PID
//                    uiLog("")
//                    uiLog(String.format(Locale.ENGLISH, "Remote pid: %d", ipc.pid))
//                    uiLog("")
//
//                    // This file is actually readable directly from your app, but it's a nice short
//                    // text file that serves well as an example
//                    uiLog("Example InputStream:")
//                    val `is` = RemoteInputStream.toInputStream(ipc.openFileForReading("/system/bin/am"))
//                    if (`is` != null) {
//                        try {
//                            val br = BufferedReader(InputStreamReader(`is`))
//                            try {
//                                while (br.ready()) {
//                                    uiLog(br.readLine())
//                                }
//                            } catch (e: IOException) {
//                                uiLog(e.message)
//                                Timber.e(e)
//                            }
//
//                        } finally {
//                            try {
//                                `is`.close()
//                            } catch (e: IOException) {
//                                // no action required
//                            }
//
//                        }
//                    }
//                    uiLog("")
//
//                    // Receive an automatically reconstructed PassedObject. This is a copy of the
//                    // object on the other end, so changing it here does not change it there.
//                    val pd = ipc.someData
//                    uiLog(String.format(Locale.ENGLISH, "getSomeData(): %d %d %s", pd.a, pd.b, pd.c))
//
//                    // Run a command on the root end and get the output back
//                    val output = ipc.run("ls -l /init")
//                    if (output != null) {
//                        for (line in output) {
//                            // should show the same output as exampleWork1 and exampleWork2
//                            uiLog("exampleWorkX: $line")
//                        }
//                    }
//
//                    // Get our icon through root
//                    val bitmap = ipc.icon
//                    requireActivity().runOnUiThread(Runnable { imageView.setImageBitmap(bitmap) })
//
//                    // Ping-pong, get some thread info through a callback
//                    val ipcThreadId = Thread.currentThread().id
//                    requireActivity().runOnUiThread(Runnable { uiLog(String.format(Locale.ENGLISH, "Ping: thisUI[%d] thisIPC[%d]", Thread.currentThread().id, ipcThreadId)) })
//                    // Note that we implement IPingCallback.Stub rather than IPingCallback
//                    ipc.ping(object : IPingCallback.Stub() {
//                        /* The pong callback may be executed on the same thread as the ping call is
//                       made, but only when the root end calls the callback while the ping call is
//                       still blocking. It is best to assume it will run on a different thread and
//                       guard variable and method access accordingly.
//
//                       In this example you are likely to see thisCallback[%d] returning the same
//                       value for as thisIPC[%d] the first pong, and a different value the second
//                       pong. */
//                        override fun pong(rootMainThreadId: Long, rootCallThreadId: Long) {
//                            uiLog(String.format(Locale.ENGLISH, "Pong: rootMain[%d] rootCall[%d] thisCallback[%d]", rootMainThreadId, rootCallThreadId, Thread.currentThread().id))
//                        }
//                    })
//
//                    try {
//                        // give the root end some time to send pong replies
//                        Thread.sleep(1000)
//                    } catch (e: InterruptedException) {
//                        // no action required
//                    }
//
//                    // Our work here is done
//                    disconnect()
//                } catch (e: RemoteException) {
//                    uiLog("RemoteException during IPC. Connection lost?")
//                    Timber.e(e)
//                }
//
//            }
//
//            override fun onDisconnect(ipc: IIPC) {
//                // May or may not be called from a background thread
//                uiLog("")
//                uiLog("Disconnected")
//                Timber.tag("IPC").d("onDisconnect")
//            }
//        }
//
//    }

    fun onRunClick(v: View) {


//        button.isEnabled = false
//
//        uiLog("Executing script:")
//        val script = RootMain.getLaunchScript(requireContext(), null, null)
//        for (line in script) {
//            Timber.d("%s", line)
//            uiLog(line)
//        }
//        uiLog("")
//
//        // Open a root shell if we don't have one yet
//        if (shell == null || !shell!!.isRunning) {
//            shell = Shell.Builder()
//                    .useSU()
//                    .open { commandCode, exitCode, output ->
//                        if (exitCode != 0) {
//                            // we couldn't open the shell, enable the button
//                            requireActivity().runOnUiThread(Runnable { button.isEnabled = true })
//                        }
//                    }
//        }
//
//        // Execute the script (asynchronously)
//        shell!!.addCommand(script, 0, object : Shell.OnCommandLineListener {
//            override fun onCommandResult(commandCode: Int, exitCode: Int) {
//                // execution finished, enable the button
//                requireActivity().runOnUiThread(Runnable { button.isEnabled = true })
//            }
//
//            override fun onLine(line: String) {
//                // we receive the output of exampleWork1/2 here
//                uiLog(line)
//            }
//        })
//
//        /*
//        If this method was not running on the main thread, and you wanted to use the IPC class
//        serially rather than using the onConnect callback, you could do it like this:
//
//        IIPC ipc = ipcReceiver.getIPC(30 * 1000);
//        if (ipc != null) {
//            int remotePid = ipc.getPid();
//            // ...
//            ipc.disconnect();
//        }
//
//     */
    }

}
