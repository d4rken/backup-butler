package eu.darken.bb.common

import eu.darken.bb.common.WorkId.Companion.EMPTY
import java.util.*

data class WorkId(val id: UUID = UUID.randomUUID()) {

    interface State {
        val workId: WorkId

        val isWorking: Boolean
            get() = workId != EMPTY
    }

    companion object {
        val DEFAULT = WorkId(UUID.fromString("0b92cbea-84f5-4a47-94cb-a0a7e1601ec0"))
        val EMPTY = WorkId(UUID(0L, 0L))
    }
}

fun WorkId.State.tryClearWorkId(id: WorkId = WorkId.DEFAULT): WorkId {
    return if (this.workId == id) EMPTY else workId
}