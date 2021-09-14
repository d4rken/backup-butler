package eu.darken.bb.main.ui.settings.support

import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.GeneralSettings
import eu.darken.bb.R
import eu.darken.bb.common.ClipboardHelper
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartPreferenceFragment
import javax.inject.Inject

@Keep
@AndroidEntryPoint
class SupportFragment : SmartPreferenceFragment() {

    private val vdc: SupportFragmentVDC by viewModels()

    override val preferenceFile: Int = R.xml.preferences_support
    @Inject lateinit var generalSettings: GeneralSettings

    override val settings: GeneralSettings by lazy { generalSettings }

    @Inject lateinit var clipboardHelper: ClipboardHelper

    private val installIdPref by lazy { findPreference<Preference>("support.installid")!! }
    private val supportMailPref by lazy { findPreference<Preference>("support.email.darken")!! }

    override fun onPreferencesCreated() {

        installIdPref.setOnPreferenceClickListener {
            vdc.copyInstallID()
            true
        }
        supportMailPref.setOnPreferenceClickListener {
            vdc.sendSupportMail()
            true
        }

        super.onPreferencesCreated()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.clipboardEvent.observe2(this) { installId ->
            Snackbar.make(requireView(), installId, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.general_copy_action) {
                    clipboardHelper.copyToClipboard(installId)
                }
                .show()
        }

        vdc.emailEvent.observe2(this) {
            startActivity(it)
        }
        super.onViewCreated(view, savedInstanceState)
    }
}