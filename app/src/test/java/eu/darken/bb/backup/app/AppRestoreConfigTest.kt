package eu.darken.bb.backup.app

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.backup.core.app.AppRestoreConfig
import io.kotlintest.shouldBe
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

}