package eu.darken.bb.backup.core

import eu.darken.bb.AppModule
import eu.darken.bb.storage.core.Storage
import io.kotlintest.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BackupTargetTest {
    @Test
    fun `test serialization`() {
        val orig = Backup.Target(
                storageId = Storage.Id(),
                backupSpecId = BackupSpec.Id("strawberry"),
                backupId = Backup.Id(),
                backupType = Backup.Type.FILES
        )

        val adapter = AppModule().moshi().adapter(Backup.Target::class.java)

        val json = adapter.toJson(orig)
        json shouldBe "{" +
                "\"storageId\":\"${orig.storageId.idString}\"," +
                "\"backupSpecId\":\"strawberry\"," +
                "\"backupId\":\"${orig.backupId.idString}\"," +
                "\"backupType\":\"${orig.backupType.name}\"" +
                "}"

        assertThat(adapter.fromJson(json)).isEqualTo(orig)
    }

    @Test
    fun `sets should disalllow duplicate targets`() {
        val set = mutableSetOf<Backup.Target>()
        val orig = Backup.Target(
                storageId = Storage.Id(),
                backupSpecId = BackupSpec.Id("strawberry"),
                backupId = Backup.Id(),
                backupType = Backup.Type.FILES
        )
        set.add(orig)
        set.add(orig)
        set.size shouldBe 1
        set.first() shouldBe orig
    }
}