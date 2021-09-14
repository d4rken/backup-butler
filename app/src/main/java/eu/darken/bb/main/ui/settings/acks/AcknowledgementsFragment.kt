package eu.darken.bb.main.ui.settings.acks

import androidx.annotation.Keep
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.GeneralSettings
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.main.ui.settings.general.GeneralSettingsFragmentVDC
import javax.inject.Inject

@Keep
@AndroidEntryPoint
class AcknowledgementsFragment : SmartPreferenceFragment() {

    private val vdc: GeneralSettingsFragmentVDC by viewModels()

    override val preferenceFile: Int = R.xml.preferences_acknowledgements
    @Inject lateinit var debugSettings: GeneralSettings

    override val settings: GeneralSettings by lazy { debugSettings }

}