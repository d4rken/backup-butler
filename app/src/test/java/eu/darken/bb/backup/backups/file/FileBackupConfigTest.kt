package eu.darken.bb.backup.backups.file

import eu.darken.bb.AppModule
import eu.darken.bb.backup.backups.Backup
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FileBackupConfigTest {
    @Test
    fun testSerialization() {
        val config = FileBackup.Config(listOf("test/file"))

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(Backup.Config::class.java)

        val json = adapter.toJson(config)
        assertThat(json).contains("{\"backupType\":\"FILE\"").contains("\"paths\":[\"test/file\"]}")

        val configRestored = adapter.fromJson(json)
        assertThat(configRestored).isInstanceOf(FileBackup.Config::class.java)
    }
}