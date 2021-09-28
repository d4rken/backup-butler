package eu.darken.bb.main.ui.advanced.debug

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.root.javaroot.JavaRootClient
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.DebugFragmentBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

@AndroidEntryPoint
class DebugFragment : SmartFragment(R.layout.debug_fragment) {
    private val vdc: DebugFragmentVDC by viewModels()
    private val binding: DebugFragmentBinding by viewBinding()

    @Inject lateinit var javaRootClient: JavaRootClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.librootjavaStart.clicksDebounced().subscribe {
            Single.fromCallable { javaRootClient.runSessionAction { it.ipc.checkBase() } }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    log { "checkBase(): $result" }
                    binding.librootjavaOutput.text = result
                }, { error ->
                    log { "Root check failed: ${error.asLog()}" }
                    Toast.makeText(requireContext(), error.toString(), Toast.LENGTH_LONG).show()
                })
        }

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        val TAG = logTag("Debug", "Fragment")
    }
}
