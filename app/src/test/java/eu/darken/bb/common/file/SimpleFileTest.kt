package eu.darken.bb.common.file

import eu.darken.bb.AppModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SimpleFileTest {
    @Test
    fun `serialize`() {
        val original = SimplePath.build("test", "file")

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(APath::class.java)

        val json = adapter.toJson(original)
        assertThat(json)
                .contains("\"pathType\":\"SIMPLE\"")
                .contains("\"path\":\"test/file\"")

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(SimplePath::class.java)
        assertThat(restored).isEqualTo(original)
    }
}