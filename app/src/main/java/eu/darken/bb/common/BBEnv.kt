package eu.darken.bb.common

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BBEnv @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val privateFilesPath: File = context.filesDir

}