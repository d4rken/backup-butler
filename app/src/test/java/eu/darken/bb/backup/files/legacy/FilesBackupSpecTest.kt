package eu.darken.bb.backup.files.legacy

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.files.FilesBackupSpec
import eu.darken.bb.common.CheckSummer
import eu.darken.bb.common.file.AFile
import eu.darken.bb.common.file.SimpleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FilesBackupSpecTest {
    @Test
    fun testSerialization() {
        val config = FilesBackupSpec(
                "TestName",
                SimpleFile.build(AFile.Type.FILE, "test/file")
        )

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(BackupSpec::class.java)

        val json = adapter.toJson(config)
        val expectedSpecId = BackupSpec.Id(CheckSummer.calculate(config.label + config.path.path, CheckSummer.Type.MD5)).value
        assertThat(json)
                .contains("\"backupType\":\"FILES\"")
                .contains("\"path\":{")
                .contains("\"revisionLimit\":3")
                .contains("\"specId\":\"$expectedSpecId\"")

        val configRestored = adapter.fromJson(json)
        assertThat(configRestored).isInstanceOf(FilesBackupSpec::class.java)
        assertThat(configRestored).isEqualTo(config)
    }
}