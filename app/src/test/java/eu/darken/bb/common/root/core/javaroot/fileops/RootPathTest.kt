package eu.darken.bb.common.root.core.javaroot.fileops

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.io.File

class RootPathTest {

    @Test
    fun `test constructor`() {
        val path = RootPath("/a/test/path")
        path.name shouldBe "path"
        path.path shouldBe "/a/test/path"
        path.child("child").path shouldBe "/a/test/path/child"
    }

    @Test
    fun `test builders`() {
        RootPath.build("/test/path").path shouldBe "/test/path"
        RootPath.build("/test", "path").path shouldBe "/test/path"
        RootPath.build("/test", "/path").path shouldBe "/test/path"
        RootPath.build(File("/test"), "path").path shouldBe "/test/path"
    }

}