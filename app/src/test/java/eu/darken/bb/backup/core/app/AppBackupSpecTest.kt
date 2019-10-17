package eu.darken.bb.backup.core.app

import com.squareup.moshi.JsonDataException
import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.files.FilesBackupSpec
import eu.darken.bb.common.file.SimplePath
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
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

    @Test
    fun `force typing`() {
        val original = FilesBackupSpec(label = "test", path = SimplePath.build("path"))

        val moshi = AppModule().moshi()

        shouldThrow<JsonDataException> {
            val json = moshi.adapter(FilesBackupSpec::class.java).toJson(original)
            moshi.adapter(AppBackupSpec::class.java).fromJson(json)
        }
    }

}