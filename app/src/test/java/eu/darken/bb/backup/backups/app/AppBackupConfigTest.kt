package eu.darken.bb.backup.backups.app

import eu.darken.bb.AppModule
import eu.darken.bb.backup.backups.BackupConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AppBackupConfigTest {
    @Test
    fun testSerialization() {
        val config = AppBackupConfig("test.package")

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(BackupConfig::class.java)

        val json = adapter.toJson(config)
        assertThat(json)
                .contains("{\"revisionType\":\"APP_BACKUP\"")
                .contains("\"packageName\":\"test.package\"")
                .contains("\"backupName\":\"pkg-test.package\"")

        val configRestored = adapter.fromJson(json)
        assertThat(configRestored).isInstanceOf(AppBackupConfig::class.java)
    }
}