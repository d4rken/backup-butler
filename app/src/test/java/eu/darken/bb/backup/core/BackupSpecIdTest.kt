package eu.darken.bb.backup.core

import com.squareup.moshi.Types
import eu.darken.bb.AppModule
import io.kotlintest.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BackupSpecIdTest {
    @Test
    fun testSerialization() {
        val orig = BackupSpec.Id("cake")
        assertThat(orig.toString()).isEqualTo("Identifier(cake)")

        val adapter = AppModule().moshi().adapter(BackupSpec.Id::class.java)

        val json = adapter.toJson(orig)
        assertThat(json).contains("cake")

        assertThat(adapter.fromJson(json)).isEqualTo(orig)
    }

    @Test
    fun `test serialization within map`() {
        val moshi = AppModule().moshi()

        val type = Types.newParameterizedType(Map::class.java, BackupSpec.Id::class.java, BackupSpec.Id::class.java)
        val adapter = moshi.adapter<Map<BackupSpec.Id, BackupSpec.Id>>(type)

        val idKey = BackupSpec.Id("123")
        val idValue = BackupSpec.Id("ABC")
        val testMap = mapOf(idKey to idValue)
        val json = adapter.toJson(testMap)

        json shouldBe "{\"${idKey.idString}\":\"${idValue.idString}\"}"
        testMap shouldBe adapter.fromJson(json)
    }
}