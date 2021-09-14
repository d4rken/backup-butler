package eu.darken.bb.user.ui.settings

import androidx.annotation.Keep
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.user.core.UserSettings
import javax.inject.Inject

@AndroidEntryPoint
@Keep
class UserSettingsFragment : SmartPreferenceFragment() {

    private val vdc: UserSettingsFragmentVDC by viewModels()
    @Inject lateinit var userSettings: UserSettings

    override val settings: UserSettings by lazy { userSettings }
    override val preferenceFile: Int = R.xml.preferences_user

}