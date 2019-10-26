package eu.darken.bb.storage.core.saf

import android.net.Uri
import eu.darken.bb.AppModule
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.SAFPath
import eu.darken.bb.storage.core.Storage
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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
        json shouldBe "{" +
                "\"path\":$pathJson," +
                "\"storageId\":\"${original.storageId.idString}\"," +
                "\"storageType\":\"${Storage.Type.SAF.name}\"" +
                "}"

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
        json shouldBe "{" +
                "\"path\":$pathJson," +
                "\"storageId\":\"${original.storageId.idString}\"," +
                "\"storageType\":\"${Storage.Type.SAF.name}\"" +
                "}"

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