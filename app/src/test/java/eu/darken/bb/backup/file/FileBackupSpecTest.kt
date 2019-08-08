package eu.darken.bb.backup.file

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.file.FileBackupSpec
import eu.darken.bb.common.file.JavaFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FileBackupSpecTest {
    @Test
    fun testSerialization() {
        val config = FileBackupSpec(
                "TestName",
                listOf(JavaFile.build("test/file"))
        )

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(BackupSpec::class.java)

        val json = adapter.toJson(config)
        assertThat(json)
                .contains("\"backupType\":\"FILE\"")
                .contains("\"label\":\"files-TestName\"")
                .contains("\"paths\":[")

        val configRestored = adapter.fromJson(json)
        assertThat(configRestored).isInstanceOf(FileBackupSpec::class.java)
    }
}