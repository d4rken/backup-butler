package eu.darken.bb.main.ui.simple.wizard.common

interface StorageItem : WizardAdapter.Item {
    override val stableId: Long get() = StorageItem::class.hashCode().toLong()
}