package eu.darken.bb.backup.backups.app

import eu.darken.bb.AppModule
import eu.darken.bb.backup.backups.Backup
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AppBackupConfigTest {
    @Test
    fun testSerialization() {
        val config = AppBackup.Config("test.package")

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(Backup.Config::class.java)

        val json = adapter.toJson(config)
        assertThat(json).contains("{\"backupType\":\"APP_BACKUP\"").contains("\"packages\":[\"test.package\"]}")

        val configRestored = adapter.fromJson(json)
        assertThat(configRestored).isInstanceOf(AppBackup.Config::class.java)
    }
}