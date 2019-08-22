package eu.darken.bb.common.ui

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import eu.darken.bb.R
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment

abstract class BaseEditorFragment
    : SmartFragment() {
    abstract val vdc: VDC

    interface VDC {
        interface State {
            val isExisting: Boolean
        }

        val state: LiveData<out State>
        val finishActivityEvent: SingleLiveEvent<Any>

        fun onNavigateBack(): Boolean
    }

    override fun onAttach(context: Context) {
        setHasOptionsMenu(true)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                vdc.onNavigateBack()
            }
        })
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().setDisplayHomeAsUpEnabled(true)

        vdc.state.observe(this, Observer(this::onBaseStateUpdate))
        vdc.finishActivityEvent.observe(this, Observer { requireActivity().finish() })

        super.onViewCreated(view, savedInstanceState)
    }

    open fun onBaseStateUpdate(state: VDC.State) {
        requireActivityActionBar().setHomeAsUpIndicator(if (state.isExisting) R.drawable.ic_cancel else R.drawable.ic_arrow_back)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> vdc.onNavigateBack()
        else -> super.onOptionsItemSelected(item)
    }
}