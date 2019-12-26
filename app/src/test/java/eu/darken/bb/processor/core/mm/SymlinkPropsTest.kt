package eu.darken.bb.processor.core.mm

import eu.darken.bb.AppModule
import eu.darken.bb.common.files.core.Ownership
import eu.darken.bb.common.files.core.RawPath
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.processor.core.mm.generic.SymlinkProps
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import java.util.*

class SymlinkPropsTest {

    @Test
    fun `test poly serialization of symlinks`() {
        val moshi = AppModule().moshi()

        val original = SymlinkProps(
                originalPath = RawPath.build("originalpath"),
                modifiedAt = Date(0),
                ownership = Ownership(123, 456),
                symlinkTarget = RawPath.build("target")
        )

        val adapter = moshi.adapter(Props::class.java)

        val json = adapter.toJson(original)
        json shouldBe "{" +
                "\"originalPath\":{\"path\":\"originalpath\",\"pathType\":\"RAW\"}," +
                "\"modifiedAt\":\"1970-01-01T00:00:00.000Z\"," +
                "\"ownership\":{\"userId\":123,\"groupId\":456}," +
                "\"symlinkTarget\":{\"path\":\"target\",\"pathType\":\"RAW\"}," +
                "\"dataType\":\"SYMLINK\"" +
                "}"

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test direct serialization of symlinks`() {
        val moshi = AppModule().moshi()

        val original = SymlinkProps(
                originalPath = RawPath.build("originalpath"),
                modifiedAt = Date(0),
                ownership = Ownership(123, 456),
                symlinkTarget = RawPath.build("target")
        )

        val adapter = moshi.adapter(SymlinkProps::class.java)

        val json = adapter.toJson(original)
        json shouldBe "{" +
                "\"originalPath\":{\"path\":\"originalpath\",\"pathType\":\"RAW\"}," +
                "\"modifiedAt\":\"1970-01-01T00:00:00.000Z\"," +
                "\"ownership\":{\"userId\":123,\"groupId\":456}," +
                "\"symlinkTarget\":{\"path\":\"target\",\"pathType\":\"RAW\"}," +
                "\"dataType\":\"SYMLINK\"" +
                "}"

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `symlink props should have either label or original path`() {
        shouldThrow<IllegalArgumentException> {
            SymlinkProps(
                    label = null,
                    originalPath = null,
                    modifiedAt = Date(),
                    ownership = Ownership(123, 456),
                    symlinkTarget = LocalPath.build("sym/link/target")
            )
        }
    }
}