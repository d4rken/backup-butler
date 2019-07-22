package eu.darken.bb.repos.core.local

import eu.darken.bb.AppModule
import eu.darken.bb.common.file.SimpleFile
import eu.darken.bb.repos.core.BackupRepo
import eu.darken.bb.repos.core.RepoRef
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocalStorageRepoRefTest {
    @Test
    fun testSerialization() {
        val original = LocalStorageRepoRef(
                SimpleFile.build("test", "path")
        )

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(RepoRef::class.java)

        val json = adapter.toJson(original)
        assertThat(json)
                .contains("\"path\":\"test/path\"")
                .contains("\"repoType\":\"${BackupRepo.Type.LOCAL_STORAGE.name}\"")
                .contains("\"repoId\":\"${original.repoId}\"")

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(LocalStorageRepoRef::class.java)
        assertThat(restored).isEqualTo(original)
    }
}