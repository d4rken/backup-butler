package eu.darken.bb.common.smart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import butterknife.ButterKnife
import butterknife.Unbinder
import eu.darken.bb.App
import eu.darken.bb.common.debug.logging.v
import eu.darken.bb.common.navigation.doNavigate
import java.util.*


abstract class SmartFragment(@LayoutRes val layoutRes: Int) : Fragment(layoutRes) {
    internal val tag: String =
        App.logTag("Fragment", "${this.javaClass.simpleName}(${Integer.toHexString(hashCode())})")

    private val unbinders = HashSet<Unbinder>()

    fun addUnbinder(unbinder: Unbinder) {
        unbinders.add(unbinder)
    }

    override fun onAttach(context: Context) {
        v(tag) { "onAttach(context=$context)" }
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        v(tag) { "onCreate(savedInstanceState=$savedInstanceState)" }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v(tag) { "onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState" }
        return layoutRes.let {
            val layout = inflater.inflate(it, container, false)
            addUnbinder(ButterKnife.bind(this, layout))
            layout
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        v(tag) { "onViewCreated(view=$view, savedInstanceState=$savedInstanceState)" }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        v(tag) { "onActivityCreated(savedInstanceState=$savedInstanceState)" }
        super.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        v(tag) { "onResume()" }
        super.onResume()
    }

    override fun onPause() {
        v(tag) { "onPause()" }
        super.onPause()
    }

    override fun onDestroyView() {
        v(tag) { "onDestroyView()" }
        super.onDestroyView()

        for (u in unbinders) u.unbind()
        unbinders.clear()
    }

    override fun onDetach() {
        v(tag) { "onDetach()" }
        super.onDetach()
    }

    override fun onDestroy() {
        v(tag) { "onDestroy()" }
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        v(tag) { "onActivityResult(requestCode=$requestCode, resultCode=$resultCode, data=$data)" }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun invalidateOptionsMenu() {
        requireActivity().invalidateOptionsMenu()
    }

    fun finishActivity() = requireActivity().finish()

    fun NavDirections.navigateTo() {
        doNavigate(this)
    }
}
