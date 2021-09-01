package eu.darken.bb.backup.core

import eu.darken.bb.AppModule
import eu.darken.bb.storage.core.Storage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson

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
        json.toFormattedJson() shouldBe """
            {
                "storageId": "${orig.storageId.idString}",
                "backupSpecId": "strawberry",
                "backupId": "${orig.backupId.idString}",
                "backupType": "${orig.backupType.name}"
            }
        """.toFormattedJson()

        adapter.fromJson(json) shouldBe orig
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