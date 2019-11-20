package eu.darken.bb.common.debug.recording.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.android.AndroidInjection
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.main.ui.advanced.AdvancedActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import timber.log.Timber
import javax.inject.Inject

class RecorderService : Service() {
    companion object {
        private val TAG = App.logTag("Debug", "RecorderService")
        private const val NOTIF_CHANID_DEBUG = "bb.notifchan.debug"
        private const val STOP_ACTION = "STOP_SERVICE"
        private const val NOTIFICATION_ID = 53
    }

    private lateinit var subscription: Disposable
    private lateinit var builder: NotificationCompat.Builder
    @Inject lateinit var bbDebug: BBDebug
    @Inject lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        AndroidInjection.inject(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIF_CHANID_DEBUG, getString(R.string.general_bugreporting_label), NotificationManager.IMPORTANCE_MIN)
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, AdvancedActivity::class.java)
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

        subscription = bbDebug.observeOptions()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.isRecording) {
                        builder.setContentText(it.recorderPath)
                        notificationManager.notify(NOTIFICATION_ID, builder.build())
                    } else {
                        stopForeground(true)
                        stopSelf()
                    }
                }
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag(TAG).d("onStartCommand(intent=$intent, flags=$flags, startId=$startId")
        if (intent?.action == STOP_ACTION) {
            bbDebug.setRecording(false)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        subscription.dispose()
        super.onDestroy()
    }
}