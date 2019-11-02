package eu.darken.bb.backup.core.files

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.app.AppBackupMetaData
import eu.darken.bb.common.TypeMissMatchException
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import java.util.*

class FilesBackupMetaDataTest {

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
        shouldThrow<IllegalArgumentException> {
            original.backupType = Backup.Type.APP
            Any()
        }
        original.backupType shouldBe Backup.Type.FILES
    }

    @Test
    fun `force typing`() {
        val original = AppBackupMetaData(
                backupId = Backup.Id()
        )

        val moshi = AppModule().moshi()

        shouldThrow<TypeMissMatchException> {
            val json = moshi.adapter(AppBackupMetaData::class.java).toJson(original)
            moshi.adapter(FilesBackupMetaData::class.java).fromJson(json)
        }
    }

}