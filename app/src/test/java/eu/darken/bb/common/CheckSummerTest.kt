package eu.darken.bb.common

import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNull.nullValue
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import testhelper.BaseTest
import java.io.File

class CheckSummerTest : BaseTest() {

    @get:Rule var rule: MockitoRule = MockitoJUnit.rule()

    @Test
    fun `test String to MD5`() {
        assertThat(CheckSummer.calculate("This is a test", CheckSummer.Type.MD5), `is`("CE114E4501D2F4E2DCEA3E17B546F339"))
    }

    @Test
    fun `test File to MD5`() {
        val fileName = "FileToMD5.txt"
        val testFile = File(fileName)
        try {
            testFile.printWriter().use { out ->
                out.println("This is a test")
            }

            assertThat(CheckSummer.calculate(testFile, CheckSummer.Type.MD5), `is`("FF22941336956098AE9A564289D1BF1B"))
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun `test File to SHA1`() {
        val fileName = "FileToSHA1.txt"
        val testFile = File(fileName)
        try {
            testFile.printWriter().use { out ->
                out.println("This is a SHA1 test")
            }

            assertThat(CheckSummer.calculate(testFile, CheckSummer.Type.SHA1), `is`("C3A77E4289829C9A2688856DAD7A4D12C39D254A"))
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun `test File to SHA256`() {
        val fileName = "FileToSHA256.txt"
        val testFile = File(fileName)
        try {
            testFile.printWriter().use { out ->
                out.println("This is a SHA256 test")
            }

            assertThat(CheckSummer.calculate(testFile, CheckSummer.Type.SHA256), `is`("C2AFACFB14D64AFD3D38E40CEC5FA961788706898798BBE304AD756FB73488EF"))
        } finally {
            testFile.delete()
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `test File to BADALGO`() {
        val fileName = "FileToBADALGO.txt"
        val testFile = File(fileName)
        try {
            testFile.printWriter().use { out ->
                out.println("This is a BADALGO test")
            }

            assertThat(CheckSummer.calculate(testFile, CheckSummer.Type.BADALGO), `is`(nullValue()))
        } finally {
            testFile.delete()
        }
    }

}