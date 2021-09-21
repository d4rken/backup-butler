package eu.darken.bb.main.ui.settings.ui.language

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.RecyclerViewWrapperLayout
import javax.inject.Inject

@AndroidEntryPoint
class LanguageFragment : SmartFragment(R.layout.settings_ui_language_fragment) {

    @BindView(R.id.language_list) lateinit var languageList: RecyclerView
    @BindView(R.id.language_list_wrapper) lateinit var languageListWrapper: RecyclerViewWrapperLayout

    private val vdc: LanguageFragmentVDC by viewModels()

    @Inject lateinit var adapter: LanguageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        languageList.setupDefaults(adapter)

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
