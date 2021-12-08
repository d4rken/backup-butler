package eu.darken.bb.common.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2

fun ViewPager2.getCurrentFragment(fragmentManager: FragmentManager): Fragment? {
    return fragmentManager.findFragmentByTag("f$currentItem")
}

fun ViewPager2.getFragment(fragmentManager: FragmentManager, position: Int): Fragment? {
    return fragmentManager.findFragmentByTag("f$position")
}