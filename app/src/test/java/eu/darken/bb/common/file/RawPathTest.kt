package eu.darken.bb.common.file

import com.squareup.moshi.JsonDataException
import eu.darken.bb.AppModule
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.RawPath
import eu.darken.bb.common.file.core.local.File
import eu.darken.bb.common.file.core.local.LocalPath
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RawPathTest {
    @Test
    fun `test polymorph serialization`() {
        val original = RawPath.build("test", "file")

        val adapter = AppModule().moshi().adapter(APath::class.java)

        val json = adapter.toJson(original)
        assertThat(json).isEqualTo("{\"path\":\"test/file\",\"pathType\":\"RAW\"}")

        assertThat(adapter.fromJson(json)).isEqualTo(original)
    }

    @Test
    fun `test direct serialization`() {
        val original = RawPath.build("test", "file")

        val adapter = AppModule().moshi().adapter(RawPath::class.java)

        val json = adapter.toJson(original)
        assertThat(json).isEqualTo("{\"path\":\"test/file\",\"pathType\":\"RAW\"}")

        assertThat(adapter.fromJson(json)).isEqualTo(original)
    }

    @Test
    fun `test fixed type`() {
        val file = RawPath.build("test", "file")
        file.pathType shouldBe APath.Type.RAW
        shouldThrow<IllegalArgumentException> {
            file.pathType = APath.Type.LOCAL
            Any()
        }
        file.pathType shouldBe APath.Type.RAW
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