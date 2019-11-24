package eu.darken.bb.common.file

import com.squareup.moshi.JsonDataException
import eu.darken.bb.AppModule
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.RawPath
import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.local.tryMkFile
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File

class JavaPathTest {
    private val testFile = File("./testfile")

    @AfterEach
    fun cleanup() {
        testFile.delete()
    }

    @Test
    fun `test direct serialization`() {
        testFile.tryMkFile()
        val original = LocalPath.build(file = testFile)

        val adapter = AppModule().moshi().adapter(LocalPath::class.java)

        val json = adapter.toJson(original)
        assertThat(json).isEqualTo("{\"file\":\"${testFile}\",\"pathType\":\"LOCAL\"}")

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test polymorph serialization`() {
        testFile.tryMkFile()
        val original = LocalPath.build(file = testFile)

        val adapter = AppModule().moshi().adapter(APath::class.java)

        val json = adapter.toJson(original)
        assertThat(json).isEqualTo("{\"file\":\"${testFile}\",\"pathType\":\"LOCAL\"}")

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val file = LocalPath(testFile)
        file.pathType shouldBe APath.PathType.LOCAL
        shouldThrow<IllegalArgumentException> {
            file.pathType = APath.PathType.RAW
            Any()
        }
        file.pathType shouldBe APath.PathType.LOCAL
    }

    @Test
    fun `force typing`() {
        val original = RawPath.build("test", "file")

        val moshi = AppModule().moshi()

        shouldThrow<JsonDataException> {
            val json = moshi.adapter(RawPath::class.java).toJson(original)
            moshi.adapter(LocalPath::class.java).fromJson(json)
        }
    }
}