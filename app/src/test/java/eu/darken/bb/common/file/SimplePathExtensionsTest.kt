package eu.darken.bb.common.file

import eu.darken.bb.common.file.core.RawPath
import eu.darken.bb.common.file.core.crumbsTo
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class SimplePathExtensionsTest {

    @Test
    fun `test chunking`() {
        val parent = RawPath.build("/the/parent/")
        val child = RawPath.build("/the/parent/has/a/child/")

        val crumbs = parent.crumbsTo(child)

        crumbs shouldBe arrayOf("has", "a", "child")
    }

}