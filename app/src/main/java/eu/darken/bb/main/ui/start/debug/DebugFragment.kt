package eu.darken.bb.main.ui.start.debug

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import butterknife.BindView
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject


class DebugFragment : SmartFragment(), AutoInject {
    companion object {
        fun newInstance(): Fragment = DebugFragment()
        val TAG = App.logTag("Debug", "Fragment")
    }


    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: DebugFragmentVDC by vdcs { vdcSource }

    init {
        layoutRes = R.layout.debug_fragment
    }

    @Inject lateinit var javaRootClient: JavaRootClient

    @BindView(R.id.librootjava_start) lateinit var librootJavaTest: Button
    @BindView(R.id.librootjava_output) lateinit var librootJavaOutput: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        librootJavaTest.clicksDebounced().subscribe {
            javaRootClient.session.map { it.ipc }.map { it.checkBase() }.take(1)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { result ->
                        Timber.tag(TAG).d("checkBase(): %s", result)
                        librootJavaOutput.text = result
                    }
        }

        super.onViewCreated(view, savedInstanceState)
    }

}
