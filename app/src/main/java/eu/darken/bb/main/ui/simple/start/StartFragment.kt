package eu.darken.bb.main.ui.simple.start

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StartFragmentBinding

@AndroidEntryPoint
class StartFragment : SmartFragment(R.layout.start_fragment) {

    private val vdc: StartFragmentVDC by viewModels()
    private val binding: StartFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
//            appVersion
//            upgradeInfos
//
//            updateCard
//            changelogAction
//            updateAction
        }
        super.onViewCreated(view, savedInstanceState)
    }
}
