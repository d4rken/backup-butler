package eu.darken.bb.quickmode.ui.config.common

interface StorageItem : ConfigAdapter.Item {
    override val stableId: Long get() = StorageItem::class.hashCode().toLong()
}