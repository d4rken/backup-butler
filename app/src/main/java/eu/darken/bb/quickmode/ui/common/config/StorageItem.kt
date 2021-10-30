package eu.darken.bb.quickmode.ui.common.config

interface StorageItem : ConfigAdapter.Item {
    override val stableId: Long get() = StorageItem::class.hashCode().toLong()
}