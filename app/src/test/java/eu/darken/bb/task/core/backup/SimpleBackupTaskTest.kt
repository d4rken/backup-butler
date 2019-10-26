package eu.darken.bb.task.core.backup

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.Task
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class SimpleBackupTaskTest {

    @Test
    fun `test serialization`() {
        val original = SimpleBackupTask(
                taskId = Task.Id(),
                label = "BackupTaskName",
                isOneTimeTask = false,
                sources = setOf(Generator.Id()),
                destinations = setOf(Storage.Id())
        )

        val expectedOutput = "{" +
                "\"taskId\":\"${original.taskId.idString}\"," +
                "\"label\":\"BackupTaskName\"," +
                "\"sources\":[\"${original.sources.first().idString}\"]," +
                "\"destinations\":[\"${original.destinations.first().idString}\"]," +
                "\"isOneTimeTask\":false," +
                "\"taskType\":\"BACKUP_SIMPLE\"" +
                "}"

        val adapterDirect = AppModule().moshi().adapter(SimpleBackupTask::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect shouldBe expectedOutput
        adapterDirect.fromJson(jsonDirect) shouldBe original

        val adapterPoly = AppModule().moshi().adapter(Task::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly shouldBe expectedOutput
        adapterPoly.fromJson(jsonPoly) shouldBe original
    }

    @Test
    fun `test version fixed type`() {
        val original = SimpleBackupTask(taskId = Task.Id())
        original.taskType shouldBe Task.Type.BACKUP_SIMPLE
        original.taskType = Task.Type.RESTORE_SIMPLE
        original.taskType shouldBe Task.Type.BACKUP_SIMPLE
    }

}