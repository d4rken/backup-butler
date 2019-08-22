package eu.darken.bb.common.smart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder
import eu.darken.bb.App
import timber.log.Timber
import java.util.*


abstract class SmartFragment : Fragment() {
    internal val tag: String = App.logTag("Fragment", this.javaClass.simpleName + "(" + Integer.toHexString(hashCode()) + ")")

    private val unbinders = HashSet<Unbinder>()

    fun addUnbinder(unbinder: Unbinder) {
        unbinders.add(unbinder)
    }

    override fun onAttach(context: Context) {
        Timber.tag(tag).v("onAttach(context=$context)")
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(tag).v("onCreate(savedInstanceState=$savedInstanceState)")
        super.onCreate(savedInstanceState)
    }

    @LayoutRes var layoutRes: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutRes?.let {
            val layout = inflater.inflate(it, container, false)
            addUnbinder(ButterKnife.bind(this, layout))
            layout
        } ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.tag(tag).v("onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Timber.tag(tag).v("onActivityCreated(savedInstanceState=$savedInstanceState)")
        super.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        Timber.tag(tag).v("onResume()")
        super.onResume()
    }

    override fun onPause() {
        Timber.tag(tag).v("onPause()")
        super.onPause()
    }

    override fun onDestroyView() {
        Timber.tag(tag).v("onDestroyView()")
        super.onDestroyView()

        for (u in unbinders) u.unbind()
        unbinders.clear()
    }

    override fun onDetach() {
        Timber.tag(tag).v("onDetach()")
        super.onDetach()
    }

    override fun onDestroy() {
        Timber.tag(tag).v("onDestroy()")
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.tag(tag).v("onActivityResult(requestCode=%d, resultCode=%d, data=%s)", requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun invalidateOptionsMenu() {
        requireActivity().invalidateOptionsMenu()
    }
}
