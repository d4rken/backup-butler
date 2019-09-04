package eu.darken.bb.processor.core.mm

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.file.SFile
import eu.darken.bb.common.file.SimpleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class MMRefTest {
    @Test
    fun testSerialization() {
        val ref = MMRef(
                refId = MMRef.Id(),
                backupId = Backup.Id(),
                type = MMRef.Type.DIRECTORY,
                tmpPath = File("simplefile"),
                originalPath = SimpleFile.build(SFile.Type.FILE, "originalpath")
        )

        val orig = ref.props

        val adapter = AppModule().moshi().adapter(MMRef.Props::class.java)

        val json = adapter.toJson(orig)
        assertThat(json)
                .contains("\"originalPath\":{\"pathType\":\"SIMPLE\",\"type\":\"FILE\",\"path\":\"originalpath\"")
                .contains("\"refType\":\"DIRECTORY\"")

        assertThat(adapter.fromJson(json)).isEqualTo(orig)
    }
}