package eu.darken.bb.storage.core.local

import eu.darken.bb.AppModule
import eu.darken.bb.common.file.JavaPath
import eu.darken.bb.common.file.asFile
import eu.darken.bb.storage.core.Storage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocalStorageRefTest {
    @Test
    fun testSerialization() {
        val original = LocalStorageRef(
                JavaPath.build("test", "path")
        )

        val moshi = AppModule().moshi()
        val adapter = moshi.adapter(Storage.Ref::class.java)

        val json = adapter.toJson(original)
        assertThat(json)
                .contains("\"${original.path.asFile()}\"")
                .contains("\"${original.path.pathType.name}\"")
                .contains("\"storageType\":\"${Storage.Type.LOCAL.name}\"")
                .contains("\"storageId\":\"${original.storageId.id}\"")

        val restored = adapter.fromJson(json)
        assertThat(restored).isInstanceOf(LocalStorageRef::class.java)
        assertThat(restored).isEqualTo(original)
    }
}