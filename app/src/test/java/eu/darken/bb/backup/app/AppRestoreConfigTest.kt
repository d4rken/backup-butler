package eu.darken.bb.backup.app

import com.squareup.moshi.JsonDataException
import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesBackupSpec
import eu.darken.bb.common.file.SimplePath
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test

class AppRestoreConfigTest {

    @Test
    fun `test serialization`() {
        val original = AppRestoreConfig()

        val moshi = AppModule().moshi()

        val expectedJson = "{" +
                "\"skipExistingApps\":false," +
                "\"restoreApk\":true," +
                "\"restoreData\":true," +
                "\"restoreType\":\"APP\"" +
                "}"

        val adapterPoly = moshi.adapter(Restore.Config::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly shouldBe expectedJson
        adapterPoly.fromJson(jsonPoly) shouldBe original

        val adapterDirect = moshi.adapter(AppRestoreConfig::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect shouldBe expectedJson
        adapterDirect.fromJson(jsonDirect) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val original = AppRestoreConfig()
        original.restoreType shouldBe Backup.Type.APP
        original.restoreType = Backup.Type.FILES
        original.restoreType shouldBe Backup.Type.APP
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