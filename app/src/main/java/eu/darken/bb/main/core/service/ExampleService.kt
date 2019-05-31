package eu.darken.bb.main.core.service

import android.content.Intent
import android.os.IBinder
import dagger.android.AndroidInjection
import eu.darken.bb.common.smart.SmartService
import javax.inject.Inject

class ExampleService : SmartService() {

    @Inject lateinit var binder: ExampleBinder

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder = binder

}