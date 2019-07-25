package eu.darken.bb.storage.core.local

import eu.darken.bb.AppModule
import eu.darken.bb.storage.core.BackupStorage
import eu.darken.bb.storage.core.StorageConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocalStorageConfigTest {
    @Test
    fun testSerialization() {
        val original = LocalStorageConfig(
                label = "testlabel"
        )

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(StorageConfig::class.java)

        val json = adapter.toJson(original)
        assertThat(json)
                .contains("\"label\":\"testlabel\"")
                .contains("\"storageType\":\"${BackupStorage.Type.LOCAL_STORAGE}\"")

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(LocalStorageConfig::class.java)
        assertThat(restored).isEqualTo(original)
    }
}