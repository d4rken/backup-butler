package eu.darken.bb.common.files.core.saf

import android.net.Uri
import com.squareup.moshi.JsonDataException
import eu.darken.bb.AppModule
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.RawPath
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SAFPathTest {

    val testUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3Asafstor")

    @Test
    fun `test direct serialization`() {
        val original = SAFPath.build(testUri, "seg1", "seg2", "seg3")

        val adapter = AppModule().moshi().adapter(SAFPath::class.java)

        val json = adapter.toJson(original)
        assertThat(json).isEqualTo("{\"treeRoot\":\"$testUri\",\"crumbs\":[\"seg1\",\"seg2\",\"seg3\"],\"pathType\":\"SAF\"}")

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test polymorph serialization`() {
        val original = SAFPath.build(testUri, "seg3", "seg2", "seg1")

        val adapter = AppModule().moshi().adapter(APath::class.java)

        val json = adapter.toJson(original)
        assertThat(json).isEqualTo("{\"treeRoot\":\"$testUri\",\"crumbs\":[\"seg3\",\"seg2\",\"seg1\"],\"pathType\":\"SAF\"}")

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val file = SAFPath.build(testUri, "seg1", "seg2")
        file.pathType shouldBe APath.PathType.SAF
        shouldThrow<java.lang.IllegalArgumentException> {
            file.pathType = APath.PathType.LOCAL
            Any()
        }
        file.pathType shouldBe APath.PathType.SAF
    }

    @Test
    fun `test must be tree uri`() {
        shouldThrow<IllegalArgumentException> {
            SAFPath.build(Uri.parse("abc"))
        }
    }

    @Test
    fun `force typing`() {
        val original = RawPath.build("test", "file")

        val moshi = AppModule().moshi()

        shouldThrow<JsonDataException> {
            val json = moshi.adapter(RawPath::class.java).toJson(original)
            moshi.adapter(SAFPath::class.java).fromJson(json)
        }
    }
}