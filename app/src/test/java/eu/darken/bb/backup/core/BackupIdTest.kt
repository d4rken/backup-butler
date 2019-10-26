package eu.darken.bb.backup.core

import com.squareup.moshi.Types.newParameterizedType
import eu.darken.bb.AppModule
import io.kotlintest.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*


class BackupIdTest {
    @Test
    fun testSerialization() {
        val uuid = UUID.randomUUID()
        val orig = Backup.Id(uuid)
        assertThat(orig.toString()).isEqualTo("BackupId($uuid)")

        val adapter = AppModule().moshi().adapter(Backup.Id::class.java)

        val json = adapter.toJson(orig)
        assertThat(json).contains(uuid.toString())

        assertThat(adapter.fromJson(json)).isEqualTo(orig)
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

        json shouldBe "{\"${idKey.idString}\":\"${idValue.idString}\"}"
        testMap shouldBe adapter.fromJson(json)
    }
}