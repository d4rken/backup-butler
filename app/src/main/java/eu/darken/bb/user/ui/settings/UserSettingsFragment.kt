package eu.darken.bb.user.ui.settings

import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.SettingsUserFragmentBinding
import eu.darken.bb.user.core.UpgradeInfo.Status.BASIC
import eu.darken.bb.user.core.UpgradeInfo.Status.PRO

@AndroidEntryPoint
@Keep
class UserSettingsFragment : Smart2Fragment(R.layout.settings_user_fragment) {

    override val vdc: UserSettingsFragmentVDC by viewModels()
    override val ui: SettingsUserFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        vdc.state.observe2(ui) { info ->
            loadingOverlay.isGone = true
            dataContainer.isGone = false

            statusText.text = when (info.status) {
                BASIC -> getString(R.string.upgrade_basicversion_label)
                PRO -> getString(R.string.upgrade_proversion_label)
            }
            statusDetailsText.text = getString(R.string.upgrade_status_pro_version_due_to_beta)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        val TAG = logTag("Settings", "User", "Fragment")
    }

}