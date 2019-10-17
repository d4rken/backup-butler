package eu.darken.bb.backup.core

import eu.darken.bb.AppModule
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
}