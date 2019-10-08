package eu.darken.bb.storage.core

import eu.darken.bb.AppModule
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class SimpleStorageStrategyTest {

    @Test
    fun `test serialization`() {
        val original = SimpleStrategy()

        val expectedOutput = "{" +
                "\"type\":\"SIMPLE\"" +
                "}"

        val adapterDirect = AppModule().moshi().adapter(SimpleStrategy::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect shouldBe expectedOutput
        adapterDirect.fromJson(jsonDirect) shouldBe original

        val adapterPoly = AppModule().moshi().adapter(Storage.Strategy::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly shouldBe expectedOutput
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