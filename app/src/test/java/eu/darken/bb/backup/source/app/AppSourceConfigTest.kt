package eu.darken.bb.backup.source.app

import eu.darken.bb.AppModule
import eu.darken.bb.backup.Source
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AppSourceConfigTest {
    @Test
    fun testSerialization() {
        val config = AppSourceConfig(listOf("test.package"))

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(Source.Config::class.java)

        val json = adapter.toJson(config)
        assertThat(json).contains("{\"sourceType\":\"APP_BACKUP\"").contains("\"packages\":[\"test.package\"]}")

        val configRestored = adapter.fromJson(json)
        assertThat(configRestored).isInstanceOf(AppSourceConfig::class.java)
    }
}