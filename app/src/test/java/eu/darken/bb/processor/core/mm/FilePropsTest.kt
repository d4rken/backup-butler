package eu.darken.bb.processor.core.mm

import eu.darken.bb.AppModule
import eu.darken.bb.common.files.core.Ownership
import eu.darken.bb.common.files.core.Permissions
import eu.darken.bb.common.files.core.RawPath
import eu.darken.bb.processor.core.mm.file.FileProps
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import java.util.*

class FilePropsTest {

    @Test
    fun `test poly serialization of FileProps`() {
        val moshi = AppModule().moshi()

        val original = FileProps(
                originalPath = RawPath.build("originalpath"),
                modifiedAt = Date(0),
                ownership = Ownership(123, 456),
                permissions = Permissions(16888)
        )

        val adapter = moshi.adapter(Props::class.java)

        val json = adapter.toJson(original)
        json shouldBe "{" +
                "\"originalPath\":{\"path\":\"originalpath\",\"pathType\":\"RAW\"}," +
                "\"modifiedAt\":\"1970-01-01T00:00:00.000Z\"," +
                "\"ownership\":{\"userId\":123,\"groupId\":456}," +
                "\"permissions\":{\"mode\":16888}," +
                "\"dataType\":\"FILE\"" +
                "}"

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test direct serialization of FileProps`() {
        val moshi = AppModule().moshi()

        val original = FileProps(
                originalPath = RawPath.build("originalpath"),
                modifiedAt = Date(0),
                ownership = Ownership(123, 456),
                permissions = Permissions(16888)
        )

        val adapter = moshi.adapter(FileProps::class.java)

        val json = adapter.toJson(original)
        json shouldBe "{" +
                "\"originalPath\":{\"path\":\"originalpath\",\"pathType\":\"RAW\"}," +
                "\"modifiedAt\":\"1970-01-01T00:00:00.000Z\"," +
                "\"ownership\":{\"userId\":123,\"groupId\":456}," +
                "\"permissions\":{\"mode\":16888}," +
                "\"dataType\":\"FILE\"" +
                "}"

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `file props should have either label or original path`() {
        shouldThrow<IllegalArgumentException> {
            FileProps(
                    label = null,
                    originalPath = null,
                    modifiedAt = Date(),
                    ownership = Ownership(123, 456),
                    permissions = Permissions(16888)
            )
        }
    }
}