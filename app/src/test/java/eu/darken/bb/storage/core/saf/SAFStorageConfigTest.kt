package eu.darken.bb.storage.core.saf

import eu.darken.bb.AppModule
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.local.LocalStorageConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson

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
        json.toFormattedJson() shouldBe """{
            "label": "testlabel",
            "storageId": "${testID.idString}",
            "strategy": {
                "type": "SIMPLE"
            },
            "storageType": "${Storage.Type.SAF}"
        }""".toFormattedJson()

        val restored = adapter.fromJson(json)
        restored.shouldBeTypeOf<SAFStorageConfig>()
        restored shouldBe original
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
        json.toFormattedJson() shouldBe """{
            "label": "testlabel",
            "storageId": "${testID.idString}",
            "strategy": {
                "type": "SIMPLE"
            },
            "storageType":"${Storage.Type.SAF}"
        }""".toFormattedJson()

        val restored = adapter.fromJson(json)
        restored.shouldBeTypeOf<SAFStorageConfig>()
        restored shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val original = SAFStorageConfig(storageId = Storage.Id())
        original.storageType shouldBe Storage.Type.SAF
        shouldThrow<IllegalArgumentException> {
            original.storageType = Storage.Type.LOCAL
            Any()
        }
        original.storageType shouldBe Storage.Type.SAF
    }

    @Test
    fun `force typing`() {
        val original = SAFStorageConfig(
                label = "testlabel",
                storageId = Storage.Id()
        )

        val moshi = AppModule().moshi()

        shouldThrow<Exception> {
            val json = moshi.adapter(SAFStorageConfig::class.java).toJson(original)
            moshi.adapter(LocalStorageConfig::class.java).fromJson(json)
        }
    }
}