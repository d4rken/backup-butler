package eu.darken.bb.backup

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.BackupSpec
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
}