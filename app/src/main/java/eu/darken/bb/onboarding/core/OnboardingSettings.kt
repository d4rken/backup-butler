package eu.darken.bb.onboarding.core

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.preference.PreferenceStoreMapper
import eu.darken.bb.settings.core.Settings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingSettings @Inject constructor(
    @ApplicationContext private val context: Context
) : Settings() {

    override val preferences: SharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    var lastBetaDisclaimerVersion: Long
        get() = preferences.getLong(PKEY_BETA_DISCLAIMER, 0L)
        set(value) = preferences.edit().putLong(PKEY_BETA_DISCLAIMER, value).apply()

    var lastOnboardingVersion: Long
        get() = preferences.getLong(PKEY_ONBOARDING, 0L)
        set(value) = preferences.edit().putLong(PKEY_ONBOARDING, value).apply()

    var lastChangelogVersion: Long
        get() = preferences.getLong(PKEY_CHANGELOG, 0L)
        set(value) = preferences.edit().putLong(PKEY_CHANGELOG, value).apply()

    override val preferenceDataStore: PreferenceDataStore = object : PreferenceStoreMapper() {
        override fun getLong(key: String?, defValue: Long): Long = when (key) {
            PKEY_BETA_DISCLAIMER -> lastBetaDisclaimerVersion
            PKEY_ONBOARDING -> lastBetaDisclaimerVersion
            PKEY_CHANGELOG -> lastBetaDisclaimerVersion
            else -> super.getLong(key, defValue)
        }

        override fun putLong(key: String?, value: Long) = when (key) {
            PKEY_BETA_DISCLAIMER -> lastBetaDisclaimerVersion = value
            PKEY_ONBOARDING -> lastBetaDisclaimerVersion = value
            PKEY_CHANGELOG -> lastBetaDisclaimerVersion = value
            else -> super.putLong(key, value)
        }
    }

    companion object {
        internal val TAG = logTag("Onboarding", "Settings")
        const val PREF_FILE = "settings_onboarding"
        const val PKEY_BETA_DISCLAIMER = "onboarding.beta.disclaimer.versionCode"
        const val PKEY_ONBOARDING = "onboarding.general.versionCode"
        const val PKEY_CHANGELOG = "onboarding.changelog.versionCode"
    }
}