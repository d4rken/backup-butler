package eu.darken.bb.main.ui.settings.ui.language

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.RecyclerViewWrapperLayout
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import javax.inject.Inject

@AndroidEntryPoint
class LanguageFragment : SmartFragment() {

    @BindView(R.id.language_list) lateinit var languageList: RecyclerView
    @BindView(R.id.language_list_wrapper) lateinit var languageListWrapper: RecyclerViewWrapperLayout

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: LanguageFragmentVDC by vdcs { vdcSource }

    @Inject lateinit var adapter: LanguageAdapter

    init {
        layoutRes = R.layout.settings_ui_language_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        languageList.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int ->
            vdc.selectLanguage(adapter.data[i], resources)
        }
        )
        vdc.state.observe2(this) { state ->
            adapter.update(state.languages, state.current)
        }
        vdc.finishEvent.observe2(this) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
