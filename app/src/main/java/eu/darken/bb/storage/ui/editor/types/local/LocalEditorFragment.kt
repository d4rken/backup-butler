package eu.darken.bb.storage.ui.editor.types.local

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.BindView
import com.jakewharton.rxbinding3.widget.textChangeEvents
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcsAssisted
import eu.darken.bb.storage.core.getStorageId
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject


class LocalEditorFragment : SmartFragment(), AutoInject {
    companion object {
        fun newInstance(): Fragment = LocalEditorFragment()
    }

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: LocalEditorFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as LocalEditorFragmentVDC.Factory
        factory.create(handle, arguments!!.getStorageId()!!)
    })


    @Inject lateinit var adapter: StorageAdapter

    @BindView(R.id.path_input) lateinit var pathInput: TextView


    init {
        layoutRes = R.layout.storageeditor_local_fragment
    }

    override fun onAttach(context: Context) {
        setHasOptionsMenu(true)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                vdc.onGoBack()
            }
        })
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        vdc.state.observe(this, Observer { state ->
            if (pathInput.text.toString() != state.path) {
                pathInput.text = state.path
            }

            if (state.path.isNotEmpty()) {
                pathInput.setTextColor(getColorForAttr(if (state.validPath) R.attr.colorOnPrimarySurface else R.attr.colorError))
            }

            requireActivityActionBar().setDisplayHomeAsUpEnabled(state.canGoBack)
        })

        pathInput.textChangeEvents().subscribe { vdc.updatePath(it.text) }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> vdc.onGoBack()
        else -> super.onOptionsItemSelected(item)
    }
}
