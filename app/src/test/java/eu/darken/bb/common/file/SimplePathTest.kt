package eu.darken.bb.common.file

import eu.darken.bb.AppModule
import io.kotlintest.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SimplePathTest {
    @Test
    fun `test polymorph serialization`() {
        val original = SimplePath.build("test", "file")

        val adapter = AppModule().moshi().adapter(APath::class.java)

        val json = adapter.toJson(original)
        assertThat(json).isEqualTo("{\"path\":\"test/file\",\"pathType\":\"SIMPLE\"}")

        assertThat(adapter.fromJson(json)).isEqualTo(original)
    }

    @Test
    fun `test direct serialization`() {
        val original = SimplePath.build("test", "file")

        val adapter = AppModule().moshi().adapter(SimplePath::class.java)

        val json = adapter.toJson(original)
        assertThat(json).isEqualTo("{\"path\":\"test/file\",\"pathType\":\"SIMPLE\"}")

        assertThat(adapter.fromJson(json)).isEqualTo(original)
    }

    @Test
    fun `test fixed type`() {
        val file = SimplePath.build("test", "file")
        file.pathType shouldBe APath.SFileType.SIMPLE
        file.pathType = APath.SFileType.JAVA
        file.pathType shouldBe APath.SFileType.SIMPLE
    }
}