package eu.darken.bb.storage.core.local

import eu.darken.bb.AppModule
import eu.darken.bb.storage.core.Storage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocalStorageConfigTest {
    @Test
    fun testSerialization() {
        val testID = Storage.Id()
        val original = LocalStorageConfig(
                label = "testlabel",
                storageId = testID
        )

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(Storage.Config::class.java)

        val json = adapter.toJson(original)
        assertThat(json)
                .contains("\"label\":\"testlabel\"")
                .contains("\"storageType\":\"${Storage.Type.LOCAL}\"")
                .contains("\"storageId\":\"${testID.id}\"")

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(LocalStorageConfig::class.java)
        assertThat(restored).isEqualTo(original)
    }
}