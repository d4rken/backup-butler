package eu.darken.bb.common.smart

import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding

abstract class BindingFragment<VB : ViewBinding>(
    @LayoutRes layoutRes: Int
) : SmartFragment(layoutRes) {

    lateinit var binding: VB

}