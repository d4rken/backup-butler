package eu.darken.bb.main.ui.advanced.debug

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import butterknife.BindView
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DebugFragment : SmartFragment() {
    companion object {
        fun newInstance(): Fragment = DebugFragment()
        val TAG = App.logTag("Debug", "Fragment")
    }

    private val vdc: DebugFragmentVDC by viewModels()

    init {
        layoutRes = R.layout.debug_fragment
    }

    @Inject lateinit var javaRootClient: JavaRootClient

    @BindView(R.id.librootjava_start) lateinit var librootJavaTest: Button
    @BindView(R.id.librootjava_output) lateinit var librootJavaOutput: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        librootJavaTest.clicksDebounced().subscribe {
            Single.fromCallable { javaRootClient.runSessionAction { it.ipc.checkBase() } }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result ->
                    Timber.tag(TAG).d("checkBase(): %s", result)
                    librootJavaOutput.text = result
                }
        }

        super.onViewCreated(view, savedInstanceState)
    }

}
