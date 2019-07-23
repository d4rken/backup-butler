package eu.darken.bb

import android.content.Context
import android.content.SharedPreferences
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import javax.inject.Inject

@PerApp
class CoreSettings @Inject constructor(@AppContext private val context: Context) {
    companion object {
        private const val PK_ROOT_DISABLED = "core.root.disabled"
    }

    private val preferences: SharedPreferences = context.getSharedPreferences("settings_core", Context.MODE_PRIVATE)

    var isRootDisabled: Boolean
        get() = preferences.getBoolean(PK_ROOT_DISABLED, false)
        set(value) = preferences.edit().putBoolean(PK_ROOT_DISABLED, value).apply()

}