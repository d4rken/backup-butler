package eu.darken.bb.common.file.picker.types

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.file.picker.SharedPickerVM
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.ui.editor.types.TypeSelectionAdapter
import javax.inject.Inject


class TypesPickerFragment : SmartFragment(), AutoInject {

    val navArgs by navArgs<TypesPickerFragmentArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: TypesPickerFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as TypesPickerFragmentVDC.Factory
        factory.create(handle, navArgs.options)
    })

    @BindView(R.id.picktype_list) lateinit var typeList: RecyclerView

    @Inject lateinit var adapter: TypeSelectionAdapter

    init {
        layoutRes = R.layout.pathpicker_types_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.selectType(adapter.data[i]) })
        typeList.setupDefaults(adapter)

        vdc.state.observe2(this) {
            adapter.update(it.allowedTypes)
        }

        val sharedVM = ViewModelProvider(requireActivity()).get(SharedPickerVM::class.java)
        vdc.typeEvents.observe2(this) {
            sharedVM.launchType(it)
        }
        super.onViewCreated(view, savedInstanceState)
    }

}
