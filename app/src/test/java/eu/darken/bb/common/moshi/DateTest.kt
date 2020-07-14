package eu.darken.bb.common.moshi

import eu.darken.bb.AppModule
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.*


class DateTest {
    @Test
    fun testSerialization() {
        val moshi = AppModule().moshi()
        val dateAdapter = moshi.adapter(Date::class.java)

        val orig = Date()

        val json = dateAdapter.toJson(orig)

        dateAdapter.fromJson(json) shouldBe orig
    }
}