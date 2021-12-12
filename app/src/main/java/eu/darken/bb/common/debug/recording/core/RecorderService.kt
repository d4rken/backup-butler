package eu.darken.bb.common.debug.recording.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.main.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class RecorderService : Service() {
    private lateinit var builder: NotificationCompat.Builder

    @Inject lateinit var bbDebug: BBDebug
    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var dispatcherProvider: DispatcherProvider
    private val recorderScope by lazy {
        CoroutineScope(SupervisorJob() + dispatcherProvider.IO)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIF_CHANID_DEBUG,
                getString(R.string.general_bugreporting_label),
                NotificationManager.IMPORTANCE_MIN
            )
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java)
        val openPi = PendingIntent.getActivity(this, 0, openIntent, 0)

        val stopIntent = Intent(this, RecorderService::class.java)
        stopIntent.action = STOP_ACTION
        val stopPi = PendingIntent.getService(this, 0, stopIntent, 0)

        builder = NotificationCompat.Builder(this, NOTIF_CHANID_DEBUG)
            .setChannelId(NOTIF_CHANID_DEBUG)
            .setContentIntent(openPi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_bug_report)
            .setContentText("Idle")
            .setContentTitle("Debug log is recording...")
            .addAction(NotificationCompat.Action.Builder(0, getString(R.string.general_done_action), stopPi).build())

        startForeground(NOTIFICATION_ID, builder.build())

        bbDebug.observeOptions()
            .onEach {
                if (it.isRecording) {
                    builder.setContentText(it.recorderPath)
                    notificationManager.notify(NOTIFICATION_ID, builder.build())
                } else {
                    stopForeground(true)
                    stopSelf()
                }
            }
            .launchIn(recorderScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag(TAG).d("onStartCommand(intent=$intent, flags=$flags, startId=$startId")
        if (intent?.action == STOP_ACTION) {
            bbDebug.setRecording(false)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        recorderScope.coroutineContext.cancel()
        super.onDestroy()
    }

    companion object {
        private val TAG = logTag("Debug", "RecorderService")
        private const val NOTIF_CHANID_DEBUG = "bb.notifchan.debug"
        private const val STOP_ACTION = "STOP_SERVICE"
        private const val NOTIFICATION_ID = 53
    }
}