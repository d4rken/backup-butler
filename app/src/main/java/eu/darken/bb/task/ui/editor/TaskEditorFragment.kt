package eu.darken.bb.task.ui.editor

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.smart.SmartFragment

@AndroidEntryPoint
class TaskEditorFragment : SmartFragment() {

    private val vdc: TaskEditorFragmentVDC by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vdc.navEvents.observe(this) { doNavigate(it) }
    }
}
