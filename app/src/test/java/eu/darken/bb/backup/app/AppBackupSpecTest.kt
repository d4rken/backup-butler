package eu.darken.bb.backup.app

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.app.AppBackupSpec
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class AppBackupSpecTest {

    @Test
    fun `test serialization`() {
        val original = AppBackupSpec("test.package")

        val moshi = AppModule().moshi()

        val expectedJson = "{" +
                "\"packageName\":\"test.package\"," +
                "\"specId\":\"pkg-test.package\"," +
                "\"revisionLimit\":3," +
                "\"backupType\":\"APP\"" +
                "}"

        val adapterPoly = moshi.adapter(BackupSpec::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly shouldBe expectedJson
        adapterPoly.fromJson(jsonPoly) shouldBe original

        val adapterDirect = moshi.adapter(AppBackupSpec::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect shouldBe expectedJson
        adapterDirect.fromJson(jsonDirect) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val original = AppBackupSpec("test.package")
        original.backupType shouldBe Backup.Type.APP
        original.backupType = Backup.Type.FILES
        original.backupType shouldBe Backup.Type.APP
    }

}