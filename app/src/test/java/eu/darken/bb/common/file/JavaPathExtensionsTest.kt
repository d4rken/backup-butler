package eu.darken.bb.common.file

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class JavaPathExtensionsTest {


    @Test
    fun `test chunking`() {
        val parent = LocalPath.build("/the/parent")
        val child = LocalPath.build("/the/parent/has/a/child")

        val crumbs = parent.crumbsTo(child)

        crumbs shouldBe arrayOf("has", "a", "child")
    }

}