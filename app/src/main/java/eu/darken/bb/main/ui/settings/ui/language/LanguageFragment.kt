package eu.darken.bb.main.ui.settings.ui.language

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.SettingsUiLanguageFragmentBinding
import javax.inject.Inject

@AndroidEntryPoint
class LanguageFragment : SmartFragment(R.layout.settings_ui_language_fragment) {

    private val ui: SettingsUiLanguageFragmentBinding by viewBinding()
    private val vdc: LanguageFragmentVDC by viewModels()

    @Inject lateinit var adapter: LanguageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.languageList.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int ->
            vdc.selectLanguage(adapter.data[i].language, resources)
        }
        )
        vdc.state.observe2(this) { state ->
            adapter.update(state.languages)
        }
        vdc.finishEvent.observe2(this) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
