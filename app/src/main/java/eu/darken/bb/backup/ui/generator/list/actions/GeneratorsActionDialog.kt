package eu.darken.bb.backup.ui.generator.list.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import butterknife.ButterKnife
import butterknife.Unbinder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.GeneratorListActionDialogBinding
import eu.darken.bb.task.core.TaskRepo
import javax.inject.Inject

@AndroidEntryPoint
class GeneratorsActionDialog : BottomSheetDialogFragment() {
    val navArgs by navArgs<GeneratorsActionDialogArgs>()

    private val vdc: GeneratorsActionDialogVDC by viewModels()
    @Inject lateinit var taskRepo: TaskRepo
    @Inject lateinit var actionsAdapter: ActionsAdapter

    private val ui: GeneratorListActionDialogBinding by viewBinding()
    private var unbinder: Unbinder? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.generator_list_action_dialog, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.recyclerview.setupDefaults(actionsAdapter)

        actionsAdapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int ->
            actionsAdapter.data[i].guardedAction { vdc.generatorAction(it) }
            actionsAdapter.notifyItemChanged(i)
        })

        vdc.state.observe2(this) { state ->
            ui.typeLabel.setText(state.config?.generatorType?.labelRes ?: R.string.general_unknown_label)
            ui.label.text = state.config?.label ?: getString(R.string.progress_loading_label)

            actionsAdapter.update(state.allowedActions)

            ui.recyclerview.visibility = if (state.loading) View.INVISIBLE else View.VISIBLE
            ui.progressCircular.visibility = if (state.loading) View.VISIBLE else View.INVISIBLE
            if (state.finished) dismissAllowingStateLoss()
        }
        super.onViewCreated(view, savedInstanceState)
    }
}