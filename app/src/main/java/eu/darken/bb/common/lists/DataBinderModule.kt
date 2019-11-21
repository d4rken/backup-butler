package eu.darken.bb.common.lists

class DataBinderModule<ItemT, HolderT> constructor(
        private val data: List<ItemT>,
        private val customBinder: ((adapter: ModularAdapter<HolderT>, vh: HolderT, pos: Int) -> Unit)? = null
) : ModularAdapter.BinderModule<HolderT> where HolderT : BindableVH<ItemT>, HolderT : ModularAdapter.VH {

    override fun onBindModularVH(adapter: ModularAdapter<HolderT>, vh: HolderT, pos: Int) {
        customBinder?.invoke(adapter, vh, pos) ?: vh.bind(data[pos])
    }

}