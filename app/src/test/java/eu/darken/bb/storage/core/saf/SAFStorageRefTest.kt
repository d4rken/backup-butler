package eu.darken.bb.storage.core.saf

import android.net.Uri
import eu.darken.bb.AppModule
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.saf.SAFPath
import eu.darken.bb.storage.core.Storage
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import testhelper.toFormattedJson

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class SAFStorageRefTest {

    val testUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3Asafstor")
    val safPath = SAFPath.build(testUri, "test")

    @Test
    fun `test poly serialization`() {
        val moshi = AppModule().moshi()

        val original = SAFStorageRef(safPath)

        val pathAdapter = moshi.adapter(APath::class.java)
        val adapter = moshi.adapter(Storage.Ref::class.java)

        val pathJson = pathAdapter.toJson(original.path)
        val json = adapter.toJson(original)
        json.toFormattedJson() shouldBe """{
            "path": $pathJson,
            "storageId": "${original.storageId.idString}",
            "storageType": "${Storage.Type.SAF.name}"
        }""".toFormattedJson()

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test direct serialization`() {
        val moshi = AppModule().moshi()

        val original = SAFStorageRef(safPath)

        val pathAdapter = moshi.adapter(APath::class.java)
        val adapter = moshi.adapter(SAFStorageRef::class.java)

        val pathJson = pathAdapter.toJson(original.path)
        val json = adapter.toJson(original)
        json.toFormattedJson() shouldBe """{
            "path": $pathJson,
            "storageId": "${original.storageId.idString}",
            "storageType": "${Storage.Type.SAF.name}"
        }""".toFormattedJson()

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val original = SAFStorageRef(safPath)
        original.storageType shouldBe Storage.Type.SAF
        original.storageType = Storage.Type.LOCAL
        original.storageType shouldBe Storage.Type.SAF
    }
}