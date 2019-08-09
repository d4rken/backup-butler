package eu.darken.bb.backup

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
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
}