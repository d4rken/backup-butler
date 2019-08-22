package eu.darken.bb.common

import java.util.*

data class WorkId(val id: String = UUID.randomUUID().toString()) {

    interface State {
        val workIds: Set<WorkId>

        val isWorking: Boolean
            get() = workIds.isNotEmpty()
    }

    companion object {
        val DEFAULT = WorkId("0b92cbea-84f5-4a47-94cb-a0a7e1601ec0")
        val ID1 = WorkId("0b92cbea-84f5-4a47-94cb-a0a7e1601ec1")
        val ID2 = WorkId("0b92cbea-84f5-4a47-94cb-a0a7e1601ec2")
        val ID3 = WorkId("0b92cbea-84f5-4a47-94cb-a0a7e1601ec3")
        val ID4 = WorkId("0b92cbea-84f5-4a47-94cb-a0a7e1601ec4")
        val ID5 = WorkId("0b92cbea-84f5-4a47-94cb-a0a7e1601ec5")
    }
}

fun WorkId.State.addWorkId(id: String): Set<WorkId> {
    return workIds.toMutableSet().apply { add(WorkId(id)) }.toSet()
}

fun WorkId.State.addWorkId(id: WorkId = WorkId.DEFAULT): Set<WorkId> {
    return workIds.toMutableSet().apply { add(id) }.toSet()
}

fun WorkId.State.clearWorkId(id: String): Set<WorkId> {
    return workIds.toMutableSet().apply { remove(WorkId(id)) }.toSet()
}

fun WorkId.State.clearWorkId(id: WorkId = WorkId.DEFAULT): Set<WorkId> {
    return workIds.toMutableSet().apply { remove(id) }.toSet()
}