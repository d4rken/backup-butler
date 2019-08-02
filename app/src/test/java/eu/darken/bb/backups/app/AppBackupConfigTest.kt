package eu.darken.bb.backups.app

import eu.darken.bb.AppModule
import eu.darken.bb.backups.core.BackupConfig
import eu.darken.bb.backups.core.app.AppBackupConfig
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
                .contains("{\"configType\":\"APP\"")
                .contains("\"packageName\":\"test.package\"")
                .contains("\"label\":\"pkg-test.package\"")

        val configRestored = adapter.fromJson(json)
        assertThat(configRestored).isInstanceOf(AppBackupConfig::class.java)
    }
}