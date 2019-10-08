package eu.darken.bb.storage.core.saf

import eu.darken.bb.AppModule
import eu.darken.bb.storage.core.Storage
import io.kotlintest.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SAFStorageConfigTest {

    @Test
    fun `test direct serialization`() {
        val testID = Storage.Id()
        val original = SAFStorageConfig(
                label = "testlabel",
                storageId = testID
        )

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(SAFStorageConfig::class.java)

        val json = adapter.toJson(original)
        json shouldBe "{" +
                "\"label\":\"testlabel\"," +
                "\"storageId\":\"${testID.id}\"," +
                "\"strategy\":{\"type\":\"SIMPLE\"}," +
                "\"storageType\":\"${Storage.Type.SAF}\"" +
                "}"

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(SAFStorageConfig::class.java)
        assertThat(restored).isEqualTo(original)
    }

    @Test
    fun `test poly serialization`() {
        val testID = Storage.Id()
        val original = SAFStorageConfig(
                label = "testlabel",
                storageId = testID
        )

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(Storage.Config::class.java)

        val json = adapter.toJson(original)
        json shouldBe "{" +
                "\"label\":\"testlabel\"," +
                "\"storageId\":\"${testID.id}\"," +
                "\"strategy\":{\"type\":\"SIMPLE\"}," +
                "\"storageType\":\"${Storage.Type.SAF}\"" +
                "}"

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(SAFStorageConfig::class.java)
        assertThat(restored).isEqualTo(original)
    }

    @Test
    fun `test fixed type`() {
        val original = SAFStorageConfig(storageId = Storage.Id())
        original.storageType shouldBe Storage.Type.SAF
        original.storageType = Storage.Type.LOCAL
        original.storageType shouldBe Storage.Type.SAF
    }
}