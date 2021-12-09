package eu.darken.bb.common.flow

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn

fun <T> Flow<T>.launchInView(fragment: Fragment) = this.launchIn(fragment.viewLifecycleOwner.lifecycleScope)
