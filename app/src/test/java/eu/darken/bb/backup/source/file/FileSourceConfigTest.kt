package eu.darken.bb.backup.source.file

import eu.darken.bb.AppModule
import eu.darken.bb.backup.Source
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FileSourceConfigTest {
    @Test
    fun testSerialization() {
        val config = FileSourceConfig(listOf("test/file"))

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(Source.Config::class.java)

        val json = adapter.toJson(config)
        assertThat(json).contains("{\"sourceType\":\"FILE\"").contains("\"paths\":[\"test/file\"]}")

        val configRestored = adapter.fromJson(json)
        assertThat(configRestored).isInstanceOf(FileSourceConfig::class.java)
    }
}