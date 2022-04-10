package eu.darken.bb.settings.ui.acks

import androidx.annotation.Keep
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.GeneralSettings
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartPreferenceFragment
import javax.inject.Inject

@Keep
@AndroidEntryPoint
class AcknowledgementsFragment : SmartPreferenceFragment() {

    private val vdc: AcknowledgementsFragmentVDC by viewModels()

    override val preferenceFile: Int = R.xml.preferences_acknowledgements
    @Inject lateinit var debugSettings: GeneralSettings

    override val settings: GeneralSettings by lazy { debugSettings }

}