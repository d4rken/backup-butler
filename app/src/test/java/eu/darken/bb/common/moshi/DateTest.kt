package eu.darken.bb.common.moshi

import eu.darken.bb.AppModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*


class DateTest {
    @Test
    fun testSerialization() {
        val moshi = AppModule().moshi()
        val dateAdapter = moshi.adapter(Date::class.java)

        val orig = Date()

        val json = dateAdapter.toJson(orig)

        assertThat(dateAdapter.fromJson(json)).isEqualTo(orig)
    }
}