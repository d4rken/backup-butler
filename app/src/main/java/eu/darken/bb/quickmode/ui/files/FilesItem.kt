package eu.darken.bb.quickmode.ui.files

import eu.darken.bb.quickmode.ui.QuickModeAdapter

interface FilesItem : QuickModeAdapter.Item {
    override val stableId: Long
        get() = FilesItem::class.hashCode().toLong()
}