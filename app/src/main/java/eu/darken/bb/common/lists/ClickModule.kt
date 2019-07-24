package eu.darken.bb.common.lists

class ClickModule<VHT : ModularAdapter.VH> constructor(
        private val listener: (VHT, Int) -> Unit
) : ModularAdapter.BinderModule<VHT> {

    override fun onBindModularVH(adapter: ModularAdapter<VHT>, vh: VHT, pos: Int) {
        vh.itemView.setOnClickListener {
            listener.invoke(vh, pos)
        }
    }
}