package eu.darken.bb.processor.core.mm

import eu.darken.bb.AppModule
import eu.darken.bb.common.files.core.RawPath
import eu.darken.bb.processor.core.mm.archive.ArchiveProps
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson
import java.util.*

class ArchivePropsTest {

    @Test
    fun `test poly serialization of archive props`() {
        val moshi = AppModule().moshi()

        val original = ArchiveProps(
                originalPath = RawPath.build("originalpath"),
                modifiedAt = Date(0),
                archiveType = "archive",
                compressionType = "compression"
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
                "archiveType":"archive",
                "compressionType":"compression",
                "dataType":"ARCHIVE"
            }""".toFormattedJson()

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test direct serialization of archive props`() {
        val moshi = AppModule().moshi()

        val original = ArchiveProps(
                originalPath = RawPath.build("originalpath"),
                modifiedAt = Date(0),
                archiveType = "archive",
                compressionType = "compression"
        )

        val adapter = moshi.adapter(ArchiveProps::class.java)

        val json = adapter.toJson(original)
        json.toFormattedJson() shouldBe """
            {
                "originalPath": {
                    "path": "originalpath",
                    "pathType":"RAW"
                },
                "modifiedAt": "1970-01-01T00:00:00.000Z",
                "archiveType": "archive",
                "compressionType": "compression",
                "dataType":"ARCHIVE"
            }""".toFormattedJson()

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `archive props should have either label or original path`() {
        shouldThrow<IllegalArgumentException> {
            ArchiveProps(
                    label = null,
                    originalPath = null,
                    modifiedAt = Date(),
                    compressionType = "compression",
                    archiveType = "archive"
            )
        }
    }
}