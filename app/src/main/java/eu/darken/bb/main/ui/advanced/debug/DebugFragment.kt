package eu.darken.bb.main.ui.advanced.debug

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.DebugFragmentBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import timber.log.Timber
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
                .subscribe { result ->
                    Timber.tag(TAG).d("checkBase(): %s", result)
                    binding.librootjavaOutput.text = result
                }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        val TAG = App.logTag("Debug", "Fragment")
    }
}
