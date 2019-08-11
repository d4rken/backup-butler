package eu.thedarken.sdm.tools.debug

import android.content.SharedPreferences
import eu.darken.bb.debug.DebugOptions
import io.reactivex.Observable

interface DebugModuleHost {

    fun observeOptions(): Observable<DebugOptions>

    fun getSettings(): SharedPreferences

    fun submit(options: DebugOptions)

}