package eu.darken.bb.backup.core.app

import com.squareup.moshi.JsonDataException
import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.backup.core.files.FilesBackupSpec
import eu.darken.bb.common.files.core.RawPath
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson

class AppRestoreConfigTest {

    @Test
    fun `test serialization`() {
        val original = AppRestoreConfig(
            skipExistingApps = true,
            restoreApk = true,
            restoreData = true,
            restoreCache = false,
            overwriteExisting = false
        )

        val moshi = AppModule().moshi()

        val expectedJson = """
            {
                "skipExistingApps": true,
                "restoreApk": true,
                "restoreData": true,
                "restoreCache": false,
                "overwriteExisting": false,
                "restoreType": "APP"
            }
        """.toFormattedJson()

        val adapterPoly = moshi.adapter(Restore.Config::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly.toFormattedJson() shouldBe expectedJson
        adapterPoly.fromJson(jsonPoly) shouldBe original

        val adapterDirect = moshi.adapter(AppRestoreConfig::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect.toFormattedJson() shouldBe expectedJson
        adapterDirect.fromJson(jsonDirect) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val original = AppRestoreConfig(
            skipExistingApps = true,
            restoreApk = true,
            restoreData = true,
            restoreCache = false,
            overwriteExisting = false
        )
        original.restoreType shouldBe Backup.Type.APP
        original.restoreType = Backup.Type.FILES
        original.restoreType shouldBe Backup.Type.APP
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

    @Test
    fun `test equals and hash`() {
        val one = AppRestoreConfig(
            skipExistingApps = true,
            restoreApk = true,
            restoreData = true,
            restoreCache = false,
            overwriteExisting = false
        )
        val two = AppRestoreConfig(
            skipExistingApps = true,
            restoreApk = true,
            restoreData = true,
            restoreCache = false,
            overwriteExisting = false
        )
        val three = AppRestoreConfig(
            skipExistingApps = true,
            restoreApk = true,
            restoreData = true,
            restoreCache = false,
            overwriteExisting = true
        )

        one shouldBe two
        one.hashCode() shouldBe two.hashCode()

        one shouldNotBe three
        one.hashCode() shouldNotBe three.hashCode()
    }
}