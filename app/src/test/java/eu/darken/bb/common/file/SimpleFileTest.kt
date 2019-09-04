package eu.darken.bb.common.file

import eu.darken.bb.AppModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SimpleFileTest {
    @Test
    fun `serialize file`() {
        val original = SimpleFile.build(SFile.Type.FILE, "test", "file")

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(SFile::class.java)

        val json = adapter.toJson(original)
        assertThat(json)
                .contains("\"pathType\":\"SIMPLE\"")
                .contains("\"type\":\"FILE\"")
                .contains("\"path\":\"test/file\"")

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(SimpleFile::class.java)
        assertThat(restored).isEqualTo(original)
    }

    @Test
    fun `serialize directory`() {
        val original = SimpleFile.build(SFile.Type.DIRECTORY, "test", "dir")

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(SFile::class.java)

        val json = adapter.toJson(original)
        assertThat(json)
                .contains("\"pathType\":\"SIMPLE\"")
                .contains("\"type\":\"DIRECTORY\"")
                .contains("\"path\":\"test/dir\"")

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(SimpleFile::class.java)
        assertThat(restored).isEqualTo(original)
    }
}