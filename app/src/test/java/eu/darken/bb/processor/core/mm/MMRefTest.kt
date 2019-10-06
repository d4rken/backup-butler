package eu.darken.bb.processor.core.mm

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.file.SimplePath
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
                originalPath = SimplePath.build("originalpath")
        )

        val orig = ref.props

        val adapter = AppModule().moshi().adapter(MMRef.Props::class.java)

        val json = adapter.toJson(orig)
        json shouldBe "{\"originalPath\":{\"path\":\"originalpath\",\"pathType\":\"SIMPLE\"},\"refType\":\"UNUSED\"}"

        assertThat(adapter.fromJson(json)).isEqualTo(orig)
    }

    @Test
    fun `test typing`() {
        val ref = MMRef(
                refId = MMRef.Id(),
                backupId = Backup.Id(),
                tmpPath = File("simplefile"),
                originalPath = SimplePath.build("originalpath")
        )
        ref.tmpPath.mkdir()
        ref.type shouldBe MMRef.Type.DIRECTORY
        ref.tmpPath.delete()

        ref.tmpPath.createNewFile()
        ref.type shouldBe MMRef.Type.FILE
        ref.tmpPath.delete()
    }
}