package eu.darken.bb.processor.ui.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.common.BuildVersion
import eu.darken.bb.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.hasAPILevel
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.processor.ui.ProcessorActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ProcessorNotifications @Inject constructor(
    @ApplicationContext private val context: Context,
    notificationManager: NotificationManager,
) {

    private val builder: NotificationCompat.Builder

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.notification_channel_backup_progress_label),
                NotificationManager.IMPORTANCE_LOW
            )
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

    fun getInfos(progressHost: Progress.Host): Flow<ForegroundInfo> = progressHost.progress
        .distinctUntilChangedBy { it.primary }
        .map {
            builder.setContentTitle(it.primary.get(context))
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(it.secondary.get(context)))
            log(TAG, VERBOSE) { "updatingNotification(): $it" }
            builder.toForegroundInfo()
        }
        .onStart {
            log(TAG) { "Initial notification" }
            builder.toForegroundInfo()
        }

    @SuppressLint("InlinedApi")
    private fun NotificationCompat.Builder.toForegroundInfo(): ForegroundInfo = if (BuildVersion.hasAPILevel(29)) {
        ForegroundInfo(
            NOTIFICATION_ID,
            this.build(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    } else {
        ForegroundInfo(
            NOTIFICATION_ID,
            this.build()
        )
    }

    companion object {
        val TAG = logTag("Processor", "Service", "Notifications")
        private const val NOTIFICATION_CHANNEL_ID = "eu.darken.bb.notification.channel.process.progress"
        private const val NOTIFICATION_ID = 1
    }
}
