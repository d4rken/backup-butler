package eu.darken.bb.main.ui.settings.support

import androidx.annotation.Keep
import androidx.preference.Preference
import com.google.android.material.snackbar.Snackbar
import eu.darken.bb.GeneralSettings
import eu.darken.bb.R
import eu.darken.bb.common.ClipboardHelper
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.debug.InstallId
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import javax.inject.Inject

@Keep
class SupportFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: SupportFragmentVDC by vdcs { vdcSource }

    override val preferenceFile: Int = R.xml.preferences_support
    @Inject lateinit var generalSettings: GeneralSettings

    override val settings: GeneralSettings by lazy { generalSettings }

    @Inject lateinit var installId: InstallId
    @Inject lateinit var clipboardHelper: ClipboardHelper

    private val installIdPref by lazy { findPreference<Preference>("support.installid")!! }

    override fun onPreferencesCreated() {

        installIdPref.setOnPreferenceClickListener {
            val theId = installId.installId.toString()
            Snackbar.make(requireView(), theId, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.general_copy_action) {
                        clipboardHelper.copyToClipboard(theId)
                    }
                    .show()
            true
        }
        super.onPreferencesCreated()
    }
}