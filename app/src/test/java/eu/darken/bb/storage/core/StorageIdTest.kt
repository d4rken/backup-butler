package eu.darken.bb.storage.core

import eu.darken.bb.AppModule
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
}