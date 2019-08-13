package eu.darken.bb.processor.ui.progress

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.BindView
import com.airbnb.lottie.LottieAnimationView
import dagger.android.DispatchingAndroidInjector
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.tryTextElseHide
import eu.darken.bb.common.vdcs
import javax.inject.Inject


class ProgressFragment : SmartFragment(), AutoInject {
    companion object {
        fun newInstance(): Fragment = ProgressFragment()
    }

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: ProgressFragmentVDC by vdcs { vdcSource }

    @BindView(R.id.process_progress_animation) lateinit var processProgressAnimation: LottieAnimationView
    @BindView(R.id.task_label) lateinit var taskName: TextView
    @BindView(R.id.generator_label) lateinit var generatorLabel: TextView
    @BindView(R.id.backupspec_label) lateinit var backupSpecLabel: TextView
    @BindView(R.id.process_progress_counter) lateinit var processProgressCounter: TextView

    @BindView(R.id.child_progress_container) lateinit var progressContainer: ViewGroup
    @BindView(R.id.progress_icon) lateinit var progressIcon: ImageView
    @BindView(R.id.progress_primary) lateinit var progressPrimary: TextView
    @BindView(R.id.progress_secondary) lateinit var progressSecondary: TextView
    @BindView(R.id.progress_bar) lateinit var progressBar: ProgressBar
    @BindView(R.id.progress_counter) lateinit var progressCounter: TextView

    init {
        layoutRes = R.layout.processor_progress_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe(this, Observer { state ->
            taskName.text = state.taskProgress.primary
            generatorLabel.tryTextElseHide(state.taskProgress.secondary)
            backupSpecLabel.tryTextElseHide(state.taskProgress.tertiary, View.GONE)
            processProgressCounter.tryTextElseHide(state.taskProgress.count.displayValue(requireContext()))

            progressContainer.setGone(state.actionProgress == null)
            progressIcon.setGone(state.actionProgress == null)
            progressPrimary.setGone(state.actionProgress == null)
            progressSecondary.setGone(state.actionProgress == null)
            progressBar.setGone(state.actionProgress == null)
            progressCounter.setGone(state.actionProgress == null)
            if (state.actionProgress != null) {
                progressPrimary.tryTextElseHide(state.actionProgress.primary)
                progressSecondary.tryTextElseHide(state.actionProgress.secondary)
                progressCounter.tryTextElseHide(state.actionProgress.count.displayValue(requireContext()))
                when (state.actionProgress.count) {
                    is Progress.Count.Indeterminate -> {
                        progressBar.visibility = View.VISIBLE
                        progressBar.isIndeterminate = true
                    }
                    is Progress.Count.None -> {
                        progressBar.visibility = View.GONE
                    }
                    else -> {
                        progressBar.visibility = View.VISIBLE
                        progressBar.isIndeterminate = false
                        progressBar.progress = state.actionProgress.count.current.toInt()
                        progressBar.max = state.actionProgress.count.max.toInt()
                    }
                }
            }
        })
        vdc.finishEvent.observe(this, Observer {
            activity?.finish()
        })
        super.onViewCreated(view, savedInstanceState)
    }
}
