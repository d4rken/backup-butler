package eu.darken.bb.processor.core.mm

import eu.darken.bb.AppModule
import eu.darken.bb.common.files.core.Ownership
import eu.darken.bb.common.files.core.Permissions
import eu.darken.bb.common.files.core.RawPath
import eu.darken.bb.processor.core.mm.generic.DirectoryProps
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson
import java.util.*

class DirectoryPropsTest {

    @Test
    fun `test poly serialization of directory props`() {
        val moshi = AppModule().moshi()

        val original = DirectoryProps(
            originalPath = RawPath.build("originalpath"),
            modifiedAt = Date(0),
            ownership = Ownership(123, 456),
            permissions = Permissions(16888)
        )

        val adapter = moshi.adapter(Props::class.java)

        val json = adapter.toJson(original)
        json.toFormattedJson() shouldBe """
            {
                "originalPath": {
                    "path":"originalpath",
                    "pathType":"RAW"
                },
                "modifiedAt": "1970-01-01T00:00:00.000Z",
                "ownership": {
                    "userId": 123,
                    "groupId":456
                },
                "permissions": {
                    "mode":16888
                },
                "dataType":"DIRECTORY"
            }""".toFormattedJson()

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test direct serialization of file props`() {
        val moshi = AppModule().moshi()

        val original = DirectoryProps(
            originalPath = RawPath.build("originalpath"),
            modifiedAt = Date(0),
            ownership = Ownership(123, 456),
            permissions = Permissions(16888)
        )

        val adapter = moshi.adapter(DirectoryProps::class.java)

        val json = adapter.toJson(original)
        json.toFormattedJson() shouldBe """{
                "originalPath": {
                    "path": "originalpath",
                    "pathType":"RAW"
                },
                "modifiedAt": "1970-01-01T00:00:00.000Z",
                "ownership": {
                    "userId": 123,
                    "groupId": 456
                },
                "permissions": {
                    "mode": 16888
                },
                "dataType":"DIRECTORY"
            }""".toFormattedJson()

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `directory props should have either label or original path`() {
        shouldThrow<IllegalArgumentException> {
            DirectoryProps(
                label = null,
                originalPath = null,
                modifiedAt = Date(),
                ownership = Ownership(123, 456),
                permissions = Permissions(16888)
            )
        }
    }

}