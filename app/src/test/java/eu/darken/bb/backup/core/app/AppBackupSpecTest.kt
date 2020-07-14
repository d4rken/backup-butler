package eu.darken.bb.backup.core.app

import com.squareup.moshi.JsonDataException
import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.files.FilesBackupSpec
import eu.darken.bb.common.files.core.RawPath
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson

class AppBackupSpecTest {

    val original = AppBackupSpec(
            packageName = "test.package",
            backupApk = true,
            backupData = true,
            backupCache = false,
            extraPaths = setOf(RawPath.build("testraw", "path"))
    )

    @Test
    fun `test serialization`() {

        val moshi = AppModule().moshi()

        val expectedJson = """
            {
                "packageName": "test.package",
                "specId": "pkg-test.package",
                "revisionLimit": 3,
                "backupApk": true,
                "backupData": true,
                "backupCache": false,
                "extraPaths": [
                    {
                        "path": "testraw/path",
                        "pathType": "RAW"
                    }
                ],
                "backupType": "APP"
            }
        """.toFormattedJson()

        val adapterPoly = moshi.adapter(BackupSpec::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly.toFormattedJson() shouldBe expectedJson
        adapterPoly.fromJson(jsonPoly) shouldBe original

        val adapterDirect = moshi.adapter(AppBackupSpec::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect.toFormattedJson() shouldBe expectedJson
        adapterDirect.fromJson(jsonDirect) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        original.backupType shouldBe Backup.Type.APP
        original.backupType = Backup.Type.FILES
        original.backupType shouldBe Backup.Type.APP
    }

    @Test
    fun `force typing`() {
        val original = FilesBackupSpec(label = "test", path = RawPath.build("path"))

        val moshi = AppModule().moshi()

        shouldThrow<JsonDataException> {
            val json = moshi.adapter(FilesBackupSpec::class.java).toJson(original)
            moshi.adapter(AppBackupSpec::class.java).fromJson(json)
        }
    }

}