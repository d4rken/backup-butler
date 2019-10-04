package eu.darken.bb.processor.core.mm

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.file.SimplePath
import eu.darken.bb.processor.core.mm.MMDataRepo.Companion.CACHEDIR
import io.kotlintest.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.BaseTest
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
    fun `test wipe`() {
        val repo = MMDataRepo(testDir)
        File(testDir, CACHEDIR).exists() shouldBe true

        val ref1 = repo.create(Backup.Id(), SimplePath.build("originalpath"))
        val ref2 = repo.create(Backup.Id(), SimplePath.build("originalpath"))
        ref1.tmpPath.mkdir()
        ref1.type shouldBe MMRef.Type.DIRECTORY

        ref2.tmpPath.createNewFile()
        ref2.type shouldBe MMRef.Type.FILE

        repo.wipe()
        ref1.type shouldBe MMRef.Type.UNUSED
        ref2.type shouldBe MMRef.Type.UNUSED
    }
}