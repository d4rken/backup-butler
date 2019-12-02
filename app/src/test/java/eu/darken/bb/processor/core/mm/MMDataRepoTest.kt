package eu.darken.bb.processor.core.mm

import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.file.core.APathLookup
import eu.darken.bb.processor.core.mm.MMDataRepo.Companion.CACHEDIR
import io.kotlintest.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.BaseTest
import java.io.File
import java.util.*

class MMDataRepoTest : BaseTest() {

    private val testDir = File("tmptestdir")

    @BeforeEach
    fun createTmpDir() {
        testDir.mkdirs() shouldBe true
    }

    @AfterEach
    fun cleanTmpDir() {
        testDir.deleteRecursively() shouldBe true
    }

    @Test
    fun `test release`() {
        val repo = MMDataRepo(testDir, AppModule().moshi())
        File(testDir, CACHEDIR).exists() shouldBe true

        val req1 = MMRef.Request(
                backupId = Backup.Id(),
                source = spy {
                    FileRefSource(
                            File("testfile"),
                            props = MMRef.Props(
                                    name = "testname",
                                    originalPath = null,
                                    dataType = MMRef.Type.DIRECTORY,
                                    modifiedAt = Date(),
                                    createdAt = Date(),
                                    userId = 123L,
                                    groupId = 456L,
                                    permissions = APathLookup.Permissions(16888)
                            )
                    )
                }
        )
        val ref1 = repo.create(req1)
        req1.source shouldBe ref1.source
        verify(req1.source, never()).release()

        val req2 = MMRef.Request(
                backupId = Backup.Id(),
                source = spy {
                    FileRefSource(
                            File("testfile"),
                            props = MMRef.Props(
                                    name = "testname",
                                    originalPath = null,
                                    dataType = MMRef.Type.DIRECTORY,
                                    modifiedAt = Date(),
                                    createdAt = Date(),
                                    userId = 123L,
                                    groupId = 456L,
                                    permissions = APathLookup.Permissions(16888)
                            )
                    )
                }
        )
        val ref2 = repo.create(req2)
        req2.source shouldBe ref2.source
        verify(req2.source, never()).release()

        repo.release(req1.backupId)
        verify(req1.source).release()

        repo.releaseAll()
        verify(req2.source).release()
    }
}