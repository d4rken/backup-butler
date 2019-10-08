package eu.darken.bb.backup.files

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.files.FilesBackupMetaData
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.util.*

class FilesBackupSpecTest {

    @Test
    fun `test serialization`() {
        val original = FilesBackupMetaData(
                backupId = Backup.Id()
        )

        val moshi = AppModule().moshi()
        val dateAdapter = moshi.adapter(Date::class.java)

        val dateJson = dateAdapter.toJson(original.createdAt)

        val expectedJson = "{" +
                "\"backupId\":\"${original.backupId.idString}\"," +
                "\"createdAt\":$dateJson," +
                "\"backupType\":\"FILES\"" +
                "}"

        val adapterPoly = moshi.adapter(Backup.MetaData::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly shouldBe expectedJson
        adapterPoly.fromJson(jsonPoly) shouldBe original

        val adapterDirect = moshi.adapter(FilesBackupMetaData::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect shouldBe expectedJson
        adapterDirect.fromJson(jsonDirect) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val original = FilesBackupMetaData(backupId = Backup.Id())
        original.backupType shouldBe Backup.Type.FILES
        original.backupType = Backup.Type.APP
        original.backupType shouldBe Backup.Type.FILES
    }

}