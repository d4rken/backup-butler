package eu.darken.bb.common.lists.modular.mods

import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.rx.clicksDebounced

class ClickMod<VHT : ModularAdapter.VH> constructor(
    private val listener: (VHT, Int) -> Unit
) : ModularAdapter.Module.Binder<VHT> {

    override fun onBindModularVH(adapter: ModularAdapter<VHT>, vh: VHT, pos: Int, payloads: MutableList<Any>) {
        vh.itemView.clicksDebounced().subscribe { listener.invoke(vh, pos) }
    }
}