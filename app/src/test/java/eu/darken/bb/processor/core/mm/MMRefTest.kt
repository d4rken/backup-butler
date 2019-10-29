package eu.darken.bb.processor.core.mm

import com.squareup.moshi.Types
import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.file.RawPath
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
                originalPath = RawPath.build("originalpath")
        )

        val orig = ref.props

        val adapter = AppModule().moshi().adapter(MMRef.Props::class.java)

        val json = adapter.toJson(orig)
        json shouldBe "{\"originalPath\":{\"path\":\"originalpath\",\"pathType\":\"RAW\"},\"refType\":\"UNUSED\"}"

        assertThat(adapter.fromJson(json)).isEqualTo(orig)
    }

    @Test
    fun `test typing`() {
        val ref = MMRef(
                refId = MMRef.Id(),
                backupId = Backup.Id(),
                tmpPath = File("simplefile"),
                originalPath = RawPath.build("originalpath")
        )
        ref.tmpPath.mkdir()
        ref.type shouldBe MMRef.Type.DIRECTORY
        ref.tmpPath.delete()

        ref.tmpPath.createNewFile()
        ref.type shouldBe MMRef.Type.FILE
        ref.tmpPath.delete()
    }

    @Test
    fun `test serialization within map`() {
        val moshi = AppModule().moshi()

        val type = Types.newParameterizedType(Map::class.java, MMRef.Id::class.java, MMRef.Id::class.java)
        val adapter = moshi.adapter<Map<MMRef.Id, MMRef.Id>>(type)

        val idKey = MMRef.Id()
        val idValue = MMRef.Id()
        val testMap = mapOf(idKey to idValue)
        val json = adapter.toJson(testMap)

        json shouldBe "{\"${idKey.idString}\":\"${idValue.idString}\"}"
        testMap shouldBe adapter.fromJson(json)
    }
}