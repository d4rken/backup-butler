package eu.darken.bb.processor.core.mm

import com.squareup.moshi.Types
import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.file.core.RawPath
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MMRefTest {
    @Test
    fun `test serialization`() {
        val ref = MMRef(
                refId = MMRef.Id(),
                backupId = Backup.Id(),
                source = null,
                props = MMRef.Props(
                        originalPath = RawPath.build("originalpath"),
                        dataType = MMRef.Type.FILE
                )
        )

        val orig = ref.props

        val adapter = AppModule().moshi().adapter(MMRef.Props::class.java)

        val json = adapter.toJson(orig)
        json shouldBe "{\"originalPath\":{\"path\":\"originalpath\",\"pathType\":\"RAW\"},\"dataType\":\"FILE\"}"

        assertThat(adapter.fromJson(json)).isEqualTo(orig)
    }

    @Test
    fun `test props labeling`() {
        shouldThrow<IllegalArgumentException> {
            MMRef.Props(
                    name = null,
                    originalPath = null,
                    dataType = MMRef.Type.FILE
            )
        }
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