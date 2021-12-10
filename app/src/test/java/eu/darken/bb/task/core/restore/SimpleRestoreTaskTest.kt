package eu.darken.bb.task.core.restore

import eu.darken.bb.AppModule
import eu.darken.bb.task.core.Task
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson

class SimpleRestoreTaskTest {

    @Test
    fun `test serialization`() {
        val original = SimpleRestoreTask(
            taskId = Task.Id(),
            label = "BackupTaskName",
            isSingleUse = false,
            customConfigs = emptyMap(),
            defaultConfigs = emptyMap(),
            backupTargets = emptySet()
        )

        val expectedOutput = """{
            "taskId": "${original.taskId.idString}",
            "label": "BackupTaskName",
            "isSingleUse": false,
            "defaultConfigs": {},
            "customConfigs": {},
            "backupTargets": [],
            "taskType":"RESTORE_SIMPLE"
        }""".toFormattedJson()

        val adapterDirect = AppModule().moshi().adapter(SimpleRestoreTask::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect.toFormattedJson() shouldBe expectedOutput
        adapterDirect.fromJson(jsonDirect) shouldBe original

        val adapterPoly = AppModule().moshi().adapter(Task::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly.toFormattedJson() shouldBe expectedOutput
        adapterPoly.fromJson(jsonPoly) shouldBe original
    }

    @Test
    fun `test version fixed type`() {
        val original = SimpleRestoreTask(taskId = Task.Id())
        original.taskType shouldBe Task.Type.RESTORE_SIMPLE
        original.taskType = Task.Type.BACKUP_SIMPLE
        original.taskType shouldBe Task.Type.RESTORE_SIMPLE
    }

}