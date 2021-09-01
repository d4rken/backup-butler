package eu.darken.bb.main.core


import android.content.SharedPreferences
import eu.darken.bb.App

import timber.log.Timber


object MigTool {
    internal val TAG = App.logTag("MigTool")

    enum class Type {
        STRING, STRING_SET, BOOLEAN, INTEGER
    }

    class StringAct(
        oldKey: String,
        newKey: String,
        defaultValue: String? = null
    ) : Act(MigTool.Type.STRING, oldKey, MigTool.Type.STRING, newKey, defaultValue)

    class BoolAct(
        oldKey: String,
        newKey: String,
        defaultValue: Boolean = false
    ) : Act(MigTool.Type.BOOLEAN, oldKey, MigTool.Type.BOOLEAN, newKey, defaultValue)

    open class Act(
        val oldType: Type,
        val oldKey: String,
        val newType: Type,
        val newKey: String,
        val defaultValue: Any? = null
    )

    @JvmStatic fun act(oldType: Type, oldKey: String, newType: Type, newKey: String): Act {
        return Act(oldType, oldKey, newType, newKey)
    }

    @JvmStatic fun migrate(oldPrefs: SharedPreferences, newPrefs: SharedPreferences, act: Act) {
        if (!oldPrefs.contains(act.oldKey)) return
        Timber.tag(TAG).i(
            "Migrating {%s}%s(%s) to {%s}%s(%s)",
            act.oldType,
            act.oldKey,
            oldPrefs,
            act.newType,
            act.newKey,
            newPrefs
        )

        if (act.oldType == MigTool.Type.STRING && act.newType == MigTool.Type.STRING) {
            val defaultValue = if (act is StringAct) act.defaultValue as? String else null
            newPrefs.edit().putString(act.newKey, oldPrefs.getString(act.oldKey, defaultValue)).apply()
        } else if (act.oldType == MigTool.Type.STRING_SET && act.newType == MigTool.Type.STRING_SET) {
            newPrefs.edit().putStringSet(act.newKey, oldPrefs.getStringSet(act.oldKey, null)).apply()
        } else if (act.oldType == MigTool.Type.BOOLEAN && act.newType == MigTool.Type.BOOLEAN) {
            val defaultValue = if (act is BoolAct) act.defaultValue as Boolean else false
            newPrefs.edit().putBoolean(act.newKey, oldPrefs.getBoolean(act.oldKey, defaultValue)).apply()
        } else if (act.oldType == MigTool.Type.INTEGER && act.newType == MigTool.Type.INTEGER) {
            newPrefs.edit().putInt(act.newKey, oldPrefs.getInt(act.oldKey, 0)).apply()
        } else {
            throw IllegalArgumentException()
        }
        oldPrefs.edit().remove(act.oldKey).apply()
    }


}

