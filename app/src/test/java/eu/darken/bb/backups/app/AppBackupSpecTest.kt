package eu.darken.bb.backups.app

import eu.darken.bb.AppModule
import eu.darken.bb.backups.core.BackupSpec
import eu.darken.bb.backups.core.app.AppBackupSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AppBackupSpecTest {
    @Test
    fun testSerialization() {
        val config = AppBackupSpec("test.package")

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(BackupSpec::class.java)

        val json = adapter.toJson(config)
        assertThat(json)
                .contains("{\"configType\":\"APP\"")
                .contains("\"packageName\":\"test.package\"")
                .contains("\"label\":\"pkg-test.package\"")

        val configRestored = adapter.fromJson(json)
        assertThat(configRestored).isInstanceOf(AppBackupSpec::class.java)
    }
}