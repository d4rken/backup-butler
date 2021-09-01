package eu.darken.bb.common.lists

import android.view.ViewGroup

class SimpleVHCreator<HolderT> constructor(
    private val viewType: Int = 0,
    private val factory: (ViewGroup) -> HolderT
) : ModularAdapter.CreatorModule<HolderT> where HolderT : ModularAdapter.VH {

    override fun onCreateModularVH(adapter: ModularAdapter<HolderT>, parent: ViewGroup, viewType: Int): HolderT? {
        if (this.viewType != viewType) return null
        return factory.invoke(parent)
    }
}