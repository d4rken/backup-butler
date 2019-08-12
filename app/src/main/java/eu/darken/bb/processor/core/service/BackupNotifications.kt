package eu.darken.bb.processor.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.main.ui.MainActivity
import io.reactivex.disposables.Disposables
import timber.log.Timber
import javax.inject.Inject

class BackupNotifications @Inject constructor(
        @AppContext context: Context,
        private val notificationManager: NotificationManager
) {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "notif.chan.backup.progress"
        private const val NOTIFICATION_ID = 1
    }

    private val builder: NotificationCompat.Builder
    private var progressSub = Disposables.disposed()

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.label_notification_channel_backup_progress), NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java)
        val openPi = PendingIntent.getActivity(context, 0, openIntent, 0)

        builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setContentIntent(openPi)
                .setSmallIcon(R.drawable.ic_notification_backup_icon)
                .setContentText(context.getString(R.string.label_progress_preparing))
                .setContentTitle(context.getString(R.string.app_name))
    }

    fun start(service: Service) {
        service.startForeground(NOTIFICATION_ID, builder.build())

        progressSub.dispose()
        if (service is ProgressHost) {
            progressSub = service.progress
                    .subscribe {
                        builder.setContentText(it.primary)
                        Timber.v("updatingNotification(): %s", it)
                        notificationManager.notify(NOTIFICATION_ID, builder.build())
                    }
        }
    }

    fun stop(service: Service) {
        progressSub.dispose()
        service.stopForeground(true)
    }
}
