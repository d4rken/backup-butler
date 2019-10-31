package eu.darken.bb.backup.ui.generator.list.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.task.core.TaskRepo
import javax.inject.Inject

class GeneratorsActionDialog : BottomSheetDialogFragment(), AutoInject {
    val navArgs by navArgs<GeneratorsActionDialogArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: GeneratorsActionDialogVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as GeneratorsActionDialogVDC.Factory
        factory.create(handle, navArgs.generatorId)
    })

    @Inject lateinit var taskRepo: TaskRepo

    @Inject lateinit var actionsAdapter: ActionsAdapter

    @BindView(R.id.type_label) lateinit var typeLabel: TextView
    @BindView(R.id.label) lateinit var generatorName: TextView
    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.progress_circular) lateinit var progressBar: ProgressBar

    private var unbinder: Unbinder? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.generator_list_action_dialog, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(actionsAdapter)

        actionsAdapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int ->
            vdc.generatorAction(actionsAdapter.data[i])
        })

        vdc.state.observe(this, Observer { state ->
            typeLabel.setText(state.config?.generatorType?.labelRes ?: R.string.label_unknown)
            generatorName.text = state.config?.label ?: getString(R.string.progress_loading_label)

            actionsAdapter.update(state.allowedActions)

            recyclerView.visibility = if (state.loading) View.INVISIBLE else View.VISIBLE
            progressBar.visibility = if (state.loading) View.VISIBLE else View.INVISIBLE
            if (state.finished) dismissAllowingStateLoss()
        })
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(generatorId: Generator.Id): BottomSheetDialogFragment = GeneratorsActionDialog().apply {
            arguments = GeneratorsActionDialogArgs(generatorId = generatorId).toBundle()
        }
    }
}