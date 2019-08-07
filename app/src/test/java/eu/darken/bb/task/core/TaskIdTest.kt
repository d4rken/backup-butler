package eu.darken.bb.task.core

import eu.darken.bb.AppModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class TaskIdTest {
    @Test
    fun testSerialization() {
        val uuid = UUID.randomUUID()
        val orig = BackupTask.Id(uuid)
        assertThat(orig.toString()).isEqualTo("TaskId($uuid)")

        val adapter = AppModule().moshi().adapter(BackupTask.Id::class.java)

        val json = adapter.toJson(orig)
        assertThat(json).contains(uuid.toString())

        assertThat(adapter.fromJson(json)).isEqualTo(orig)
    }
}