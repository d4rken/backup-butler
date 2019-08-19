package eu.darken.bb.common.lists

import android.view.ViewGroup
import androidx.annotation.CallSuper

abstract class ModularAdapter<VH : ModularAdapter.VH> : BaseAdapter<VH>() {
    val modules = mutableListOf<Module>()
    @CallSuper
    override fun getItemViewType(position: Int): Int {
        modules.filterIsInstance<TypeModule>().forEach {
            val type = it.onGetItemType(this, position)
            if (type != -1) return type
        }
        return super.getItemViewType(position)
    }

    @CallSuper
    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): VH {
        modules.filterIsInstance<CreatorModule<VH>>().forEach {
            val vh = it.onCreateModularVH(this, parent, viewType)
            if (vh != null) return vh
        }
        throw IllegalStateException("Couldn't create VH for type $viewType with $parent")
    }

    @CallSuper
    override fun onBindBaseVH(holder: VH, position: Int) {
        modules.filterIsInstance<BinderModule<VH>>().forEach { it.onBindModularVH(this, holder, position) }
    }

    abstract class VH(layoutRes: Int, parent: ViewGroup) : BaseAdapter.VH(layoutRes, parent)

    interface Module

    interface CreatorModule<T : VH> : Module {
        fun onCreateModularVH(adapter: ModularAdapter<T>, parent: ViewGroup, viewType: Int): T?
    }

    interface BinderModule<T : VH> : Module {
        fun onBindModularVH(adapter: ModularAdapter<T>, vh: T, pos: Int)
    }

    interface TypeModule {
        fun onGetItemType(adapter: ModularAdapter<*>, pos: Int): Int
    }
}