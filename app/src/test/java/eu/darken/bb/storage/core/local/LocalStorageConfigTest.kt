package eu.darken.bb.storage.core.local

import eu.darken.bb.AppModule
import eu.darken.bb.storage.core.Storage
import io.kotlintest.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocalStorageConfigTest {
    @Test
    fun `test direct serialization`() {
        val testID = Storage.Id()
        val original = LocalStorageConfig(
                label = "testlabel",
                storageId = testID
        )

        val moshi = AppModule().moshi()
        val strategyAdapter = moshi.adapter(Storage.Strategy::class.java)
        val adapter = moshi.adapter(LocalStorageConfig::class.java)

        val strategyJson = strategyAdapter.toJson(original.strategy)
        val json = adapter.toJson(original)
        json shouldBe "{" +
                "\"storageId\":\"${testID.id}\"," +
                "\"label\":\"testlabel\"," +
                "\"strategy\":$strategyJson," +
                "\"storageType\":\"${Storage.Type.LOCAL}\"" +
                "}"

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(LocalStorageConfig::class.java)
        assertThat(restored).isEqualTo(original)
    }

    @Test
    fun `test polymorph serialization`() {
        val testID = Storage.Id()
        val original = LocalStorageConfig(
                label = "testlabel",
                storageId = testID
        )

        val moshi = AppModule().moshi()
        val strategyAdapter = moshi.adapter(Storage.Strategy::class.java)
        val adapter = moshi.adapter(Storage.Config::class.java)

        val strategyJson = strategyAdapter.toJson(original.strategy)
        val json = adapter.toJson(original)

        json shouldBe "{" +
                "\"storageId\":\"${testID.id}\"," +
                "\"label\":\"testlabel\"," +
                "\"strategy\":$strategyJson," +
                "\"storageType\":\"${Storage.Type.LOCAL}\"" +
                "}"

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(LocalStorageConfig::class.java)
        assertThat(restored).isEqualTo(original)
    }

    @Test
    fun `test fixed type`() {
        val original = LocalStorageConfig(storageId = Storage.Id())
        original.storageType shouldBe Storage.Type.LOCAL
        original.storageType = Storage.Type.SAF
        original.storageType shouldBe Storage.Type.LOCAL
    }
}