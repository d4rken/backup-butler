package eu.darken.bb.common.file

import android.net.Uri
import eu.darken.bb.common.file.core.saf.SAFPath
import eu.darken.bb.common.file.core.saf.crumbsTo
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SAFPathExtensionsTest {

    private val testUri1: Uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3Asafstor")
    private val testUri2: Uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3Asafstor")

    @Test
    fun `test crumbsTo`() {
        val parent = SAFPath.build(testUri1, "the", "parent")
        val child = SAFPath.build(testUri1, "the", "parent", "has", "a", "child")

        val crumbs = parent.crumbsTo(child)

        crumbs shouldBe arrayOf("has", "a", "child")
    }

    @Test
    fun `test crumbsTo with empty parent`() {
        val parent = SAFPath.build(testUri1)
        val child = SAFPath.build(testUri1, "has", "a", "child")

        val crumbs = parent.crumbsTo(child)

        crumbs shouldBe arrayOf("has", "a", "child")
    }

    @Test
    fun `test crumbsTo with equal arguments`() {
        val parent = SAFPath.build(testUri1, "the", "parent")
        val child = SAFPath.build(testUri1, "the", "parent")

        val crumbs = parent.crumbsTo(child)

        crumbs shouldBe arrayOf()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test crumbsTo needs same root`() {
        val parent = SAFPath.build(testUri1, "/the/parent/")
        val child = SAFPath.build(testUri2, "/the/parent/has/a/child/")

        parent.crumbsTo(child)
    }

}