package eu.darken.bb.processor.core.mm

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.file.AFile
import eu.darken.bb.common.file.SimpleFile
import io.kotlintest.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class MMRefTest {
    @Test
    fun `test serialization`() {
        val ref = MMRef(
                refId = MMRef.Id(),
                backupId = Backup.Id(),
                tmpPath = File("simplefile"),
                originalPath = SimpleFile.build(AFile.Type.FILE, "originalpath")
        )

        val orig = ref.props

        val adapter = AppModule().moshi().adapter(MMRef.Props::class.java)

        val json = adapter.toJson(orig)
        assertThat(json)
                .contains("\"originalPath\":{\"pathType\":\"SIMPLE\",\"type\":\"FILE\",\"path\":\"originalpath\"")
                .contains("\"refType\":\"NONE\"")

        assertThat(adapter.fromJson(json)).isEqualTo(orig)
    }

    @Test
    fun `test typing`() {
        val ref = MMRef(
                refId = MMRef.Id(),
                backupId = Backup.Id(),
                tmpPath = File("simplefile"),
                originalPath = SimpleFile.build(AFile.Type.FILE, "originalpath")
        )
        ref.tmpPath.mkdir()
        ref.type shouldBe MMRef.Type.DIRECTORY
        ref.tmpPath.delete()

        ref.tmpPath.createNewFile()
        ref.type shouldBe MMRef.Type.FILE
        ref.tmpPath.delete()
    }
}