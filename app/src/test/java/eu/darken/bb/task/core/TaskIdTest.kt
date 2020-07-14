package eu.darken.bb.task.core

import com.squareup.moshi.Types
import eu.darken.bb.AppModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson
import java.util.*

class TaskIdTest {
    @Test
    fun testSerialization() {
        val uuid = UUID.randomUUID()
        val orig = Task.Id(uuid)
        orig.toString() shouldBe "TaskId($uuid)"

        val adapter = AppModule().moshi().adapter(Task.Id::class.java)

        val json = adapter.toJson(orig)
        json shouldContain uuid.toString()

        adapter.fromJson(json) shouldBe orig
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

        json.toFormattedJson() shouldBe """
            {
                "${idKey.idString}": "${idValue.idString}"
            }
        """.toFormattedJson()
        testMap shouldBe adapter.fromJson(json)
    }
}