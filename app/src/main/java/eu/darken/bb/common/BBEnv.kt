package eu.darken.bb.common

import android.content.Context
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import java.io.File
import javax.inject.Inject

@PerApp
class BBEnv @Inject constructor(
        @AppContext private val context: Context
) {

    val privateFilesPath: File = context.filesDir

}