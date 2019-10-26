package eu.darken.bb.task.core

import com.squareup.moshi.Types
import eu.darken.bb.AppModule
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TaskIdResultTest {
//    @Test
//    fun testSerialization() {
//        val uuid = UUID.randomUUID()
//        val orig = Task.Id(uuid)
//        assertThat(orig.toString()).isEqualTo("TaskId($uuid)")
//
//        val adapter = AppModule().moshi().adapter(Task.Id::class.java)
//
//        val json = adapter.toJson(orig)
//        assertThat(json).contains(uuid.toString())
//
//        assertThat(adapter.fromJson(json)).isEqualTo(orig)
//    }

    @Test
    fun `test serialization within map`() {
        val moshi = AppModule().moshi()

        val type = Types.newParameterizedType(Map::class.java, Task.Result.Id::class.java, Task.Result.Id::class.java)
        val adapter = moshi.adapter<Map<Task.Result.Id, Task.Result.Id>>(type)

        val idKey = Task.Result.Id()
        val idValue = Task.Result.Id()
        val testMap = mapOf(idKey to idValue)
        val json = adapter.toJson(testMap)

        json shouldBe "{\"${idKey.idString}\":\"${idValue.idString}\"}"
        testMap shouldBe adapter.fromJson(json)
    }
}