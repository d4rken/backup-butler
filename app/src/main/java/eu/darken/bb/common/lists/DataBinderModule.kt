package eu.darken.bb.common.lists

class DataBinderModule<ItemT, HolderT> constructor(
        private val data: List<ItemT>
) : ModularAdapter.BinderModule<HolderT> where HolderT : BindableVH<ItemT>, HolderT : ModularAdapter.VH {

    override fun onBindModularVH(adapter: ModularAdapter<HolderT>, vh: HolderT, pos: Int) {
        vh.bind(data[pos])
    }

}