package eu.darken.bb.backup.core

import com.squareup.moshi.Types.newParameterizedType
import eu.darken.bb.AppModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson
import java.util.*


class BackupIdTest {
    @Test
    fun testSerialization() {
        val uuid = UUID.randomUUID()
        val orig = Backup.Id(uuid)
        orig.toString() shouldBe "BackupId($uuid)"

        val adapter = AppModule().moshi().adapter(Backup.Id::class.java)

        val json = adapter.toJson(orig)
        json shouldContain uuid.toString()

        adapter.fromJson(json) shouldBe orig
    }

    @Test
    fun `test serialization within map`() {
        val moshi = AppModule().moshi()

        val type = newParameterizedType(Map::class.java, Backup.Id::class.java, Backup.Id::class.java)
        val adapter = moshi.adapter<Map<Backup.Id, Backup.Id>>(type)

        val idKey = Backup.Id()
        val idValue = Backup.Id()
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