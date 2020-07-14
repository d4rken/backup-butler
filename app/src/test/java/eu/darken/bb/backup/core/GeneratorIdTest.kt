package eu.darken.bb.backup.core

import com.squareup.moshi.Types
import eu.darken.bb.AppModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson
import java.util.*

class GeneratorIdTest {
    @Test
    fun testSerialization() {
        val uuid = UUID.randomUUID()
        val orig = Generator.Id(uuid)
        orig.toString() shouldBe "GeneratorId($uuid)"

        val adapter = AppModule().moshi().adapter(Generator.Id::class.java)

        val json = adapter.toJson(orig)
        json shouldContain uuid.toString()

        adapter.fromJson(json) shouldBe orig
    }

    @Test
    fun `test serialization within map`() {
        val moshi = AppModule().moshi()

        val type = Types.newParameterizedType(Map::class.java, Generator.Id::class.java, Generator.Id::class.java)
        val adapter = moshi.adapter<Map<Generator.Id, Generator.Id>>(type)

        val idKey = Generator.Id()
        val idValue = Generator.Id()
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