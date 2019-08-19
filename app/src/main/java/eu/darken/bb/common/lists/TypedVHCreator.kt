package eu.darken.bb.common.lists

import android.view.ViewGroup

class TypedVHCreator<HolderT> constructor(
        private val viewType: Int = 0,
        private val typeResolver: (Int) -> Boolean,
        private val factory: (ViewGroup) -> HolderT
) : ModularAdapter.TypeModule, ModularAdapter.CreatorModule<HolderT> where HolderT : ModularAdapter.VH {

    override fun onGetItemType(adapter: ModularAdapter<*>, pos: Int): Int {
        return if (typeResolver.invoke(pos)) viewType else -1
    }

    override fun onCreateModularVH(adapter: ModularAdapter<HolderT>, parent: ViewGroup, viewType: Int): HolderT? {
        if (this.viewType != viewType) return null
        return factory.invoke(parent)
    }
}