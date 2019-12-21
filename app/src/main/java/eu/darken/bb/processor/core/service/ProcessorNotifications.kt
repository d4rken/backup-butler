package eu.darken.bb.processor.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.processor.ui.ProcessorActivity
import io.reactivex.disposables.Disposables
import timber.log.Timber
import javax.inject.Inject

class ProcessorNotifications @Inject constructor(
        @AppContext private val context: Context,
        private val notificationManager: NotificationManager
) {

    companion object {
        val TAG = App.logTag("Processor", "Service", "Notifications")
        private const val NOTIFICATION_CHANNEL_ID = "notif.chan.process.progress"
        private const val NOTIFICATION_ID = 1
    }

    private val builder: NotificationCompat.Builder
    private var progressSub = Disposables.disposed()

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.notification_channel_backup_progress_label), NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, ProcessorActivity::class.java)
        val openPi = PendingIntent.getActivity(context, 0, openIntent, 0)

        builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setContentIntent(openPi)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_notification_backup_icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.progress_preparing_label)))

    }

    fun start(service: Service) {
        service.startForeground(NOTIFICATION_ID, builder.build())

        progressSub.dispose()
        if (service is Progress.Host) {
            progressSub = service.progress
                    .distinct { it.primary }
                    .subscribe {
                        builder.setContentTitle(it.primary.get(context))
                        builder.setStyle(NotificationCompat.BigTextStyle().bigText(it.secondary.get(context)))
                        Timber.tag(TAG).v("updatingNotification(): %s", it)
                        notificationManager.notify(NOTIFICATION_ID, builder.build())
                    }
        }
    }

    fun stop(service: Service) {
        progressSub.dispose()
        service.stopForeground(true)
    }
}
