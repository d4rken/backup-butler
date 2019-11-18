package eu.darken.bb.common.file.ui.picker.local

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.snackbar.Snackbar
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.ui.picker.SharedPickerVM
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.common.ui.BreadCrumbBar
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import java.io.File
import javax.inject.Inject


class LocalPickerFragment : SmartFragment(), AutoInject {

    val navArgs by navArgs<LocalPickerFragmentArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: LocalPickerFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as LocalPickerFragmentVDC.Factory
        factory.create(handle, navArgs.options)
    })

    @BindView(R.id.breadcrumb_bar) lateinit var breadCrumbBar: BreadCrumbBar<APath>
    @BindView(R.id.files_list) lateinit var filesList: RecyclerView
    @BindView(R.id.select_action) lateinit var selectAction: Button

    @Inject lateinit var adapter: APathLookupAdapter
    private val sharedVM by lazy { ViewModelProvider(requireActivity()).get(SharedPickerVM::class.java) }

    private var allowCreateDir = false

    init {
        layoutRes = R.layout.pathpicker_local_fragment
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel)

        breadCrumbBar.crumbNamer = {
            if (it.name.isEmpty()) File.separator else it.name
        }
        breadCrumbBar.crumbListener = {
            vdc.selectItem(it)
        }

        filesList.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.selectItem(adapter.data[i]) })

        vdc.state.observe2(this) { state ->
            breadCrumbBar.setCrumbs(state.currentCrumbs)
            adapter.update(state.currentListing)

            allowCreateDir = state.allowCreateDir
            invalidateOptionsMenu()
        }

        vdc.errorEvents.observe2(this) { error ->
            Snackbar.make(requireView(), error.tryLocalizedErrorMessage(requireContext()), Snackbar.LENGTH_LONG).show()
        }

        selectAction.clicksDebounced().subscribe { vdc.finishSelection() }

        vdc.resultEvents.observe2(this) {
            sharedVM.postResult(it)
        }

        vdc.createDirEvent.observe2(this) {
            val alertLayout = layoutInflater.inflate(R.layout.view_alertdialog_edittext, null)
            val input = alertLayout.findViewById<EditText>(R.id.input_text)
            val dialog = AlertDialog.Builder(requireContext())
                    .setView(alertLayout)
                    .setTitle(R.string.general_create_dir)
                    .setPositiveButton(R.string.general_create_action) { _, _ ->
                        vdc.createDir(input.text.toString())
                    }
                    .setNegativeButton(R.string.general_cancel_action) { _, _ ->

                    }
                    .create()

            dialog.setOnShowListener {
                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.isEnabled = false
                input.userTextChangeEvents().subscribe {
                    positiveButton.isEnabled = it.text.isNotEmpty()
                }
            }

            dialog.show()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_pathpicker_local, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_create_dir).isVisible = allowCreateDir
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_create_dir -> {
            vdc.createDirRequest()
            true
        }
        R.id.action_home -> {
            vdc.goHome()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}
