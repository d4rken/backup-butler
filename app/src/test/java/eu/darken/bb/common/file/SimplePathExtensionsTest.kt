package eu.darken.bb.common.file

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class SimplePathExtensionsTest {

    @Test
    fun `test chunking`() {
        val parent = SimplePath.build("/the/parent/")
        val child = SimplePath.build("/the/parent/has/a/child/")

        val crumbs = parent.crumbsTo(child)

        crumbs shouldBe arrayOf("has", "a", "child")
    }

}