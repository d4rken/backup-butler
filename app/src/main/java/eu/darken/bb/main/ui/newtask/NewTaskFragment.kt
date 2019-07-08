package eu.darken.bb.main.ui.newtask

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import butterknife.BindView
import butterknife.ButterKnife
import dagger.android.support.AndroidSupportInjection
import eu.darken.androidkotlinstarter.common.dagger.VDCSource
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartFragment
import javax.inject.Inject


class NewTaskFragment : SmartFragment() {
    companion object {
        fun newInstance(): Fragment = NewTaskFragment()
    }

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val viewModel: NewTaskFragmentViewModel by viewModels { vdcSource.create(this, null) }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.overview_fragment, container, false)
        addUnbinder(ButterKnife.bind(this, layout))
        return layout
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.appState.observe(this, Observer {

        })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.setTitle(R.string.label_new_task)
        toolbar.subtitle = null
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

}
