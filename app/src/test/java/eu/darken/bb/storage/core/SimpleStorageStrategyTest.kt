package eu.darken.bb.storage.core

import eu.darken.bb.AppModule
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson

class SimpleStorageStrategyTest {

    @Test
    fun `test serialization`() {
        val original = SimpleStrategy()

        val expectedOutput = """{
            "type":"SIMPLE"
        }""".toFormattedJson()

        val adapterDirect = AppModule().moshi().adapter(SimpleStrategy::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect.toFormattedJson() shouldBe expectedOutput
        adapterDirect.fromJson(jsonDirect) shouldBe original

        val adapterPoly = AppModule().moshi().adapter(Storage.Strategy::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly.toFormattedJson() shouldBe expectedOutput
        adapterPoly.fromJson(jsonPoly) shouldBe original
    }

    @Test
    fun `test version fixed type`() {
        val original = SimpleStrategy()
        original.type shouldBe Storage.Strategy.Type.SIMPLE
//        original.type = Storage.Strategy.Type.SIMPLE
//        original.type shouldBe Storage.Strategy.Type.SIMPLE
    }

}