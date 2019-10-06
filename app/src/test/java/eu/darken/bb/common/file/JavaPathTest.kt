package eu.darken.bb.common.file

import eu.darken.bb.AppModule
import io.kotlintest.shouldBe
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
        val original = JavaPath.build(file = testFile)

        val adapter = AppModule().moshi().adapter(JavaPath::class.java)

        val json = adapter.toJson(original)
        assertThat(json).isEqualTo("{\"file\":\"${testFile.canonicalPath}\",\"pathType\":\"JAVA\"}")

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test polymorph serialization`() {
        testFile.tryMkFile()
        val original = JavaPath.build(file = testFile)

        val adapter = AppModule().moshi().adapter(APath::class.java)

        val json = adapter.toJson(original)
        assertThat(json).isEqualTo("{\"file\":\"${testFile.canonicalPath}\",\"pathType\":\"JAVA\"}")

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val file = JavaPath(testFile)
        file.pathType shouldBe APath.SFileType.JAVA
        file.pathType = APath.SFileType.SIMPLE
        file.pathType shouldBe APath.SFileType.JAVA
    }
}