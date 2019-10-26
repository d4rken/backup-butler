package eu.darken.bb.backup.core.files

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.jupiter.api.Test

class FilesRestoreConfigTest {

    @Test
    fun `test serialization`() {
        val original = FilesRestoreConfig()

        val moshi = AppModule().moshi()

        val expectedJson = "{" +
                "\"replaceFiles\":false," +
                "\"restoreType\":\"FILES\"" +
                "}"

        val adapterPoly = moshi.adapter(Restore.Config::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly shouldBe expectedJson
        adapterPoly.fromJson(jsonPoly) shouldBe original

        val adapterDirect = moshi.adapter(FilesRestoreConfig::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect shouldBe expectedJson
        adapterDirect.fromJson(jsonDirect) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val original = FilesRestoreConfig()
        original.restoreType shouldBe Backup.Type.FILES
        original.restoreType = Backup.Type.APP
        original.restoreType shouldBe Backup.Type.FILES
    }

    @Test
    fun `test equals and hash`() {
        val one = FilesRestoreConfig(replaceFiles = false)
        val two = FilesRestoreConfig()
        val three = FilesRestoreConfig(replaceFiles = true)

        one shouldBe two
        one.hashCode() shouldBe two.hashCode()

        one shouldNotBe three
        one.hashCode() shouldNotBe three.hashCode()
    }

}