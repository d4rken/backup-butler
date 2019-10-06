package eu.darken.bb

import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioManager
import android.preference.PreferenceManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp


@Module
class AndroidModule {

    @Provides
    @AppContext
    @PerApp
    fun appContext(app: App): Context = app.applicationContext

    @Provides
    @PerApp
    fun preferences(@AppContext context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @PerApp
    fun packageManager(@AppContext context: Context): PackageManager = context.packageManager

    @Provides
    @PerApp
    fun contentResolver(@AppContext context: Context): ContentResolver = context.contentResolver

    @Provides
    @PerApp
    fun audioManager(@AppContext context: Context): AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @Provides
    @PerApp
    fun notificationManager(@AppContext context: Context): NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @PerApp
    fun workManager(@AppContext context: Context) = WorkManager.getInstance(context)
}
