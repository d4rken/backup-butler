package eu.darken.bb.common.debug

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow

interface DebugModuleHost {

    fun observeOptions(): Flow<DebugOptions>

    fun getSettings(): SharedPreferences

    fun submit(update: suspend (DebugOptions) -> DebugOptions)

}