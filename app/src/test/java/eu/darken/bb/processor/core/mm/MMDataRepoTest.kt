package eu.darken.bb.processor.core.mm

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.processor.core.mm.MMDataRepo.Companion.CACHEDIR
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.BaseTest
import testhelper.coroutine.TestDispatcherProvider
import testhelper.coroutine.runBlockingTest2
import java.io.File

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
    fun `test release`() = runBlockingTest2 {
        val testScope = TestCoroutineScope()
        val dispatcherProvider = TestDispatcherProvider()

        val repo = MMDataRepo(
            cachePath = testDir,
            moshi = AppModule().moshi(),
            appScope = testScope,
            dispatcherProvider = dispatcherProvider
        )
        File(testDir, CACHEDIR).exists() shouldBe true

        val source1 = mockk<MMRef.RefSource>(relaxed = true)
        val req1 = MMRef.Request(
            backupId = Backup.Id(),
            source = source1
        )
        val ref1 = repo.create(req1)
        req1.source shouldBe ref1.source
        coVerify(exactly = 0) { req1.source.release() }

        val source2 = mockk<MMRef.RefSource>(relaxed = true)
        val req2 = MMRef.Request(
            backupId = Backup.Id(),
            source = source2
        )
        val ref2 = repo.create(req2)
        req2.source shouldBe ref2.source
        coVerify(exactly = 0) { req2.source.release() }

        repo.release(req1.backupId)
        coVerify { req1.source.release() }

        repo.releaseAll()
        coVerify { req2.source.release() }
    }
}