package eu.darken.bb.common

import eu.darken.bb.common.lists.differ.DifferItem

interface DialogActionEnum : DifferItem {
    override val stableId: Long
        get() = (this as Enum<*>).ordinal.toLong()
}