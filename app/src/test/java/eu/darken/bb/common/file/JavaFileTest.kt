package eu.darken.bb.common.file

import eu.darken.bb.AppModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File

class JavaFileTest {
    private val testFile = File("./testfile")

    @AfterEach
    fun cleanup() {
        testFile.delete()
    }

    @Test
    fun testSerialization() {
        testFile.tryMkFile()
        val original = JavaFile.build(file = testFile)

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(SFile::class.java)

        val json = adapter.toJson(original)
        assertThat(json)
                .contains("\"pathType\":\"JAVA\"")
                .contains("\"type\":\"FILE\"")
                .contains("\"file\":\"${testFile.canonicalPath}\"")

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(JavaFile::class.java)
        assertThat(restored).isEqualTo(original)
    }
}