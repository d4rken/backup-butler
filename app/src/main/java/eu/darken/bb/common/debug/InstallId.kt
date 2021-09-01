package eu.darken.bb.common.debug

import eu.darken.bb.common.BBEnv
import eu.darken.bb.common.dagger.PerApp
import java.io.File
import java.util.*
import javax.inject.Inject

@PerApp
class InstallId @Inject constructor(
    bbEnv: BBEnv
) {
    private val installIdFile = File(bbEnv.privateFilesPath, "installid")

    val installId: UUID by lazy {
        try {
            UUID.fromString(installIdFile.readText())
        } catch (e: Exception) {
            val newId = UUID.randomUUID()
            installIdFile.writeText(newId.toString())
            newId
        }
    }
}