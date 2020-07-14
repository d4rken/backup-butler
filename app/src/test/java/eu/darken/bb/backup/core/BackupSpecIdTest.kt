package eu.darken.bb.backup.core

import com.squareup.moshi.Types
import eu.darken.bb.AppModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson

class BackupSpecIdTest {
    @Test
    fun testSerialization() {
        val orig = BackupSpec.Id("cake")
        orig.toString() shouldBe "Identifier(cake)"

        val adapter = AppModule().moshi().adapter(BackupSpec.Id::class.java)

        val json = adapter.toJson(orig)
        json shouldContain "cake"

        adapter.fromJson(json) shouldBe orig
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

        json.toFormattedJson() shouldBe """
            {
                "${idKey.idString}": "${idValue.idString}"
            }
        """.toFormattedJson()
        testMap shouldBe adapter.fromJson(json)
    }
}