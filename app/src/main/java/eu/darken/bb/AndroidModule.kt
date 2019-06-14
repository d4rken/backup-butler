package eu.darken.bb

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.preference.PreferenceManager
import androidx.work.WorkManager

import dagger.Module
import dagger.Provides
import eu.darken.bb.common.dagger.ApplicationContext


@Module
class AndroidModule {

    @Provides
    @ApplicationContext
    @AppComponent.Scope
    fun context(app: Application): Context = app.applicationContext

    @Provides
    @AppComponent.Scope
    fun preferences(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @AppComponent.Scope
    fun audioManager(context: Context): AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @Provides
    @AppComponent.Scope
    fun notificationManager(context: Context): NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @AppComponent.Scope
    fun workManager() = WorkManager.getInstance()
}
