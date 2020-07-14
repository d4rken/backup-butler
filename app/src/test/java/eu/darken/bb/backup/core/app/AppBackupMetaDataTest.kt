package eu.darken.bb.backup.core.app

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.files.FilesBackupMetaData
import eu.darken.bb.common.TypeMissMatchException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson
import java.util.*

class AppBackupMetaDataTest {

    @Test
    fun `test serialization`() {
        val original = AppBackupMetaData(
                backupId = Backup.Id()
        )

        val moshi = AppModule().moshi()
        val dateAdapter = moshi.adapter(Date::class.java)

        val dateJson = dateAdapter.toJson(original.createdAt)

        val expectedJson = """
            {
                "backupId":  "${original.backupId.idString}",
                "createdAt": $dateJson,
                "backupType": "APP"
            }
        """.toFormattedJson()

        val adapterPoly = moshi.adapter(Backup.MetaData::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly.toFormattedJson() shouldBe expectedJson
        adapterPoly.fromJson(jsonPoly) shouldBe original

        val adapterDirect = moshi.adapter(AppBackupMetaData::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect.toFormattedJson() shouldBe expectedJson
        adapterDirect.fromJson(jsonDirect) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val original = AppBackupMetaData(backupId = Backup.Id())
        original.backupType shouldBe Backup.Type.APP
        shouldThrow<IllegalArgumentException> {
            original.backupType = Backup.Type.FILES
            Any()
        }
        original.backupType shouldBe Backup.Type.APP
    }

    @Test
    fun `force typing`() {
        val original = FilesBackupMetaData(
                backupId = Backup.Id()
        )

        val moshi = AppModule().moshi()

        shouldThrow<TypeMissMatchException> {
            val json = moshi.adapter(FilesBackupMetaData::class.java).toJson(original)
            moshi.adapter(AppBackupMetaData::class.java).fromJson(json)
        }
    }

}