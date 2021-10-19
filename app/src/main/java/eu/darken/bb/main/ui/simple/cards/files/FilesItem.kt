package eu.darken.bb.main.ui.simple.cards.files

import eu.darken.bb.main.ui.simple.SimpleModeAdapter

interface FilesItem : SimpleModeAdapter.Item {
    override val stableId: Long
        get() = FilesItem::class.hashCode().toLong()
}