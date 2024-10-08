package eu.darken.bb.common.files

import com.squareup.moshi.JsonDataException
import eu.darken.bb.AppModule
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.RawPath
import eu.darken.bb.common.files.core.local.File
import eu.darken.bb.common.files.core.local.LocalPath
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson

class RawPathTest {
    @Test
    fun `test polymorph serialization`() {
        val original = RawPath.build("test", "file")

        val adapter = AppModule().moshi().adapter(APath::class.java)

        val json = adapter.toJson(original)
        json.toFormattedJson() shouldBe """
            {
                "path": "test/file",
                "pathType": "RAW"
            }
        """.toFormattedJson()

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test direct serialization`() {
        val original = RawPath.build("test", "file")

        val adapter = AppModule().moshi().adapter(RawPath::class.java)

        val json = adapter.toJson(original)
        json.toFormattedJson() shouldBe """
            {
                "path": "test/file",
                "pathType": "RAW"
            }
        """.toFormattedJson()

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val file = RawPath.build("test", "file")
        file.pathType shouldBe APath.PathType.RAW
        shouldThrow<IllegalArgumentException> {
            file.pathType = APath.PathType.LOCAL
            Any()
        }
        file.pathType shouldBe APath.PathType.RAW
    }

    @Test
    fun `force typing`() {
        val original = LocalPath.build(file = File("./testfile"))

        val moshi = AppModule().moshi()

        shouldThrow<JsonDataException> {
            val json = moshi.adapter(LocalPath::class.java).toJson(original)
            moshi.adapter(RawPath::class.java).fromJson(json)
        }
    }
}