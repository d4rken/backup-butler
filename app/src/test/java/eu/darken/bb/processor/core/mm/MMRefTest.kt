package eu.darken.bb.processor.core.mm

import com.squareup.moshi.Types
import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.file.core.Ownership
import eu.darken.bb.common.file.core.Permissions
import eu.darken.bb.common.file.core.RawPath
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import okio.Source
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class MMRefTest {
    @Test
    fun `test serialization`() {
        val ref = MMRef(
                refId = MMRef.Id(),
                backupId = Backup.Id(),
                source = object : MMRef.RefSource {
                    override val props: MMRef.Props
                        get() = MMRef.Props(
                                originalPath = RawPath.build("originalpath"),
                                dataType = MMRef.Type.FILE,
                                modifiedAt = Date(0),
                                ownership = Ownership(123, 456),
                                permissions = Permissions(16888)
                        )

                    override fun open(): Source {
                        throw UnsupportedOperationException()
                    }

                    override fun release() {

                    }

                }
        )

        val orig = ref.props

        val adapter = AppModule().moshi().adapter(MMRef.Props::class.java)

        val json = adapter.toJson(orig)
        json shouldBe "{" +
                "\"dataType\":\"FILE\"," +
                "\"originalPath\":{\"path\":\"originalpath\",\"pathType\":\"RAW\"}," +
                "\"modifiedAt\":\"1970-01-01T00:00:00.000Z\"," +
                "\"ownership\":{\"userId\":123,\"groupId\":456}," +
                "\"permissions\":{\"mode\":16888}" +
                "}"

        assertThat(adapter.fromJson(json)).isEqualTo(orig)
    }

    @Test
    fun `test props labeling`() {
        shouldThrow<IllegalArgumentException> {
            MMRef.Props(
                    name = null,
                    originalPath = null,
                    dataType = MMRef.Type.FILE,
                    modifiedAt = Date(),
                    ownership = Ownership(123, 456),
                    permissions = Permissions(16888)
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