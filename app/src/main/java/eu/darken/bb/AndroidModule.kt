package eu.darken.bb

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioManager
import android.preference.PreferenceManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import eu.darken.bb.common.dagger.AppContext


@Module
class AndroidModule {

    @Provides
    @AppContext
    @AppComponent.Scope
    fun appContext(app: App): Context = app.applicationContext

    @Provides
    @AppComponent.Scope
    fun preferences(@AppContext context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @AppComponent.Scope
    fun packageManager(@AppContext context: Context): PackageManager = context.packageManager

    @Provides
    @AppComponent.Scope
    fun audioManager(@AppContext context: Context): AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @Provides
    @AppComponent.Scope
    fun notificationManager(@AppContext context: Context): NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @AppComponent.Scope
    fun workManager() = WorkManager.getInstance()
}
