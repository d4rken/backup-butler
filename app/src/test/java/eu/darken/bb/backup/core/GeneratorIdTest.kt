package eu.darken.bb.backup.core

import com.squareup.moshi.Types
import eu.darken.bb.AppModule
import io.kotlintest.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class GeneratorIdTest {
    @Test
    fun testSerialization() {
        val uuid = UUID.randomUUID()
        val orig = Generator.Id(uuid)
        assertThat(orig.toString()).isEqualTo("GeneratorId($uuid)")

        val adapter = AppModule().moshi().adapter(Generator.Id::class.java)

        val json = adapter.toJson(orig)
        assertThat(json).contains(uuid.toString())

        assertThat(adapter.fromJson(json)).isEqualTo(orig)
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

        json shouldBe "{\"${idKey.idString}\":\"${idValue.idString}\"}"
        testMap shouldBe adapter.fromJson(json)
    }
}