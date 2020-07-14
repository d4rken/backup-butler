package eu.darken.bb.storage.core

import com.squareup.moshi.Types
import eu.darken.bb.AppModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson
import java.util.*

class StorageIdTest {
    @Test
    fun testSerialization() {
        val uuid = UUID.randomUUID()
        val orig = Storage.Id(uuid)
        orig.toString() shouldBe "StorageId($uuid)"

        val adapter = AppModule().moshi().adapter(Storage.Id::class.java)

        val json = adapter.toJson(orig)
        json shouldContain uuid.toString()

        adapter.fromJson(json) shouldBe orig
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

        json.toFormattedJson() shouldBe """
            {
                "${idKey.idString}": "${idValue.idString}"
            }
        """.toFormattedJson()
        testMap shouldBe adapter.fromJson(json)
    }
}