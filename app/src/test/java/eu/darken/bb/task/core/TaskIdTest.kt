package eu.darken.bb.task.core

import com.squareup.moshi.Types
import eu.darken.bb.AppModule
import io.kotlintest.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class TaskIdTest {
    @Test
    fun testSerialization() {
        val uuid = UUID.randomUUID()
        val orig = Task.Id(uuid)
        assertThat(orig.toString()).isEqualTo("TaskId($uuid)")

        val adapter = AppModule().moshi().adapter(Task.Id::class.java)

        val json = adapter.toJson(orig)
        assertThat(json).contains(uuid.toString())

        assertThat(adapter.fromJson(json)).isEqualTo(orig)
    }

    @Test
    fun `test serialization within map`() {
        val moshi = AppModule().moshi()

        val type = Types.newParameterizedType(Map::class.java, Task.Id::class.java, Task.Id::class.java)
        val adapter = moshi.adapter<Map<Task.Id, Task.Id>>(type)

        val idKey = Task.Id()
        val idValue = Task.Id()
        val testMap = mapOf(idKey to idValue)
        val json = adapter.toJson(testMap)

        json shouldBe "{\"${idKey.idString}\":\"${idValue.idString}\"}"
        testMap shouldBe adapter.fromJson(json)
    }
}