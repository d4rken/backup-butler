package eu.darken.bb.storage.core.local

import eu.darken.bb.AppModule
import eu.darken.bb.common.file.SimpleFile
import eu.darken.bb.storage.core.BackupStorage
import eu.darken.bb.storage.core.StorageRef
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocalStorageStorageRefTest {
    @Test
    fun testSerialization() {
        val original = LocalStorageStorageRef(
                SimpleFile.build("test", "path")
        )

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(StorageRef::class.java)

        val json = adapter.toJson(original)
        assertThat(json)
                .contains("\"path\":\"test/path\"")
                .contains("\"storageType\":\"${BackupStorage.Type.LOCAL_STORAGE.name}\"")
                .contains("\"storageId\":\"${original.storageId}\"")

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(LocalStorageStorageRef::class.java)
        assertThat(restored).isEqualTo(original)
    }
}