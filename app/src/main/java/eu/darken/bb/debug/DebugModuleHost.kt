package eu.darken.bb.debug

import android.content.SharedPreferences
import io.reactivex.Observable

interface DebugModuleHost {

    fun observeOptions(): Observable<DebugOptions>

    fun getSettings(): SharedPreferences

    fun submit(update: (DebugOptions) -> DebugOptions)

}