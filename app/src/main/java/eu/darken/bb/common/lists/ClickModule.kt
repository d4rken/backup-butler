package eu.darken.bb.common.lists

import eu.darken.bb.common.rx.clicksDebounced

class ClickModule<VHT : ModularAdapter.VH> constructor(
    private val listener: (VHT, Int) -> Unit
) : ModularAdapter.BinderModule<VHT> {

    override fun onBindModularVH(adapter: ModularAdapter<VHT>, vh: VHT, pos: Int) {
        vh.itemView.clicksDebounced().subscribe { listener.invoke(vh, pos) }
    }
}