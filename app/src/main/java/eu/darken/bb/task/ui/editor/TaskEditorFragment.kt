package eu.darken.bb.task.ui.editor

import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.common.smart.Smart2Fragment

@AndroidEntryPoint
class TaskEditorFragment : Smart2Fragment() {

    override val vdc: TaskEditorFragmentVDC by viewModels()
    override val ui: ViewBinding? = null

}
