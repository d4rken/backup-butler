package eu.darken.bb.main.ui.advanced

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlin.reflect.KClass


class PagerAdapter constructor(
    private val fragmentActivity: FragmentActivity,
    val pages: List<Page>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment {
        val fragmentFactory = fragmentActivity.supportFragmentManager.fragmentFactory
        return fragmentFactory.instantiate(this.javaClass.classLoader!!, pages[position].fragmentClazz.qualifiedName!!)
    }

    data class Page(
        val fragmentClazz: KClass<out Fragment>,
        @StringRes val titleRes: Int
    )
}
