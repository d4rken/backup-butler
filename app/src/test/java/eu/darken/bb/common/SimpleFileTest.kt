package eu.darken.bb.common

import eu.darken.bb.AppModule
import eu.darken.bb.common.file.SFile
import eu.darken.bb.common.file.SimpleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SimpleFileTest {
    @Test
    fun testSerialization() {
        val original = SimpleFile.build("test", "path")

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(SFile::class.java)

        val json = adapter.toJson(original)
        assertThat(json)
                .contains("\"sfileType\":\"SIMPLE\"")
                .contains("\"path\":\"test/path\"")

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(SimpleFile::class.java)
        assertThat(restored).isEqualTo(original)
    }
}