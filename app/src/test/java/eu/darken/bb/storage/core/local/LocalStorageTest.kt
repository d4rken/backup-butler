package eu.darken.bb.storage.core.local

import eu.darken.bb.AppModule
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.SimplePath
import eu.darken.bb.storage.core.Storage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocalStorageTest {
    @Test
    fun testSerialization() {
        val original = LocalStorageRef(
                SimplePath.build(APath.Type.DIRECTORY, "test", "path")
        )

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(Storage.Ref::class.java)

        val json = adapter.toJson(original)
        assertThat(json)
                .contains("\"path\":\"test/path\"")
                .contains("\"storageType\":\"${Storage.Type.LOCAL.name}\"")
                .contains("\"storageId\":\"${original.storageId.id}\"")

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(LocalStorageRef::class.java)
        assertThat(restored).isEqualTo(original)
    }
}