package eu.darken.bb.backups.file

import eu.darken.bb.AppModule
import eu.darken.bb.backups.core.BackupConfig
import eu.darken.bb.backups.core.file.FileBackupConfig
import eu.darken.bb.common.file.JavaFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FileBackupConfigTest {
    @Test
    fun testSerialization() {
        val config = FileBackupConfig(
                "TestName",
                listOf(JavaFile.build("test/file"))
        )

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(BackupConfig::class.java)

        val json = adapter.toJson(config)
        assertThat(json)
                .contains("\"configType\":\"FILE\"")
                .contains("\"label\":\"files-TestName\"")
                .contains("\"paths\":[")

        val configRestored = adapter.fromJson(json)
        assertThat(configRestored).isInstanceOf(FileBackupConfig::class.java)
    }
}