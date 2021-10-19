package eu.darken.bb.quickmode.ui.wizard.common

interface StorageItem : WizardAdapter.Item {
    override val stableId: Long get() = StorageItem::class.hashCode().toLong()
}