package eu.darken.bb.storage.core

import com.squareup.moshi.Types
import eu.darken.bb.AppModule
import io.kotlintest.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class StorageIdTest {
    @Test
    fun testSerialization() {
        val uuid = UUID.randomUUID()
        val orig = Storage.Id(uuid)
        assertThat(orig.toString()).isEqualTo("StorageId($uuid)")

        val adapter = AppModule().moshi().adapter(Storage.Id::class.java)

        val json = adapter.toJson(orig)
        assertThat(json).contains(uuid.toString())

        assertThat(adapter.fromJson(json)).isEqualTo(orig)
    }

    @Test
    fun `test serialization within map`() {
        val moshi = AppModule().moshi()

        val type = Types.newParameterizedType(Map::class.java, Storage.Id::class.java, Storage.Id::class.java)
        val adapter = moshi.adapter<Map<Storage.Id, Storage.Id>>(type)

        val idKey = Storage.Id()
        val idValue = Storage.Id()
        val testMap = mapOf(idKey to idValue)
        val json = adapter.toJson(testMap)

        json shouldBe "{\"${idKey.idString}\":\"${idValue.idString}\"}"
        testMap shouldBe adapter.fromJson(json)
    }
}