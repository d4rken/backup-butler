package eu.darken.bb.storage.core.local

import eu.darken.bb.AppModule
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.storage.core.Storage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson

class LocalStorageRefTest {

    @Test
    fun `test poly serialization`() {
        val moshi = AppModule().moshi()

        val original = LocalStorageRef(
            LocalPath.build("test", "path")
        )

        val pathAdapter = moshi.adapter(APath::class.java)
        val adapter = moshi.adapter(Storage.Ref::class.java)

        val pathJson = pathAdapter.toJson(original.path)
        val json = adapter.toJson(original)
        json.toFormattedJson() shouldBe """{
            "path": $pathJson,
            "storageId": "${original.storageId.idString}",
            "storageType": "${Storage.Type.LOCAL.name}"
        }""".toFormattedJson()

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test direct serialization`() {
        val moshi = AppModule().moshi()

        val original = LocalStorageRef(
            LocalPath.build("test", "path")
        )

        val pathAdapter = moshi.adapter(APath::class.java)
        val adapter = moshi.adapter(LocalStorageRef::class.java)

        val pathJson = pathAdapter.toJson(original.path)
        val json = adapter.toJson(original)
        json.toFormattedJson() shouldBe """{
            "path": $pathJson,
            "storageId": "${original.storageId.idString}",
            "storageType": "${Storage.Type.LOCAL.name}"
        }""".toFormattedJson()

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val original = LocalStorageRef(LocalPath.build("test", "path"))
        original.storageType shouldBe Storage.Type.LOCAL
        original.storageType = Storage.Type.SAF
        original.storageType shouldBe Storage.Type.LOCAL
    }
}