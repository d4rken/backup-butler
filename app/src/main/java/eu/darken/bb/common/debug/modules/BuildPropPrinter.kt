package eu.darken.bb.common.debug.modules

import android.os.Build
import android.util.Log
import androidx.core.util.Pair
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.debug.*
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.hasApiLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*

class BuildPropPrinter @AssistedInject constructor(
    @Assisted host: DebugModuleHost,
    @DebugScope private val debugScope: CoroutineScope,
) : DebugModule {

    private var previousOptions: DebugOptions = DebugOptions.default()

    init {
        host.observeOptions()
            .filter { !previousOptions.compareIgnorePath(it) && it.level <= Log.INFO }
            .onEach { previousOptions = it }
            .onEach {
                if (!hasApiLevel(26)) {
                    for (info in getSystemBuildProp()) {
                        if (info.first!!.contains("ro.product") || info.first!!.contains("ro.build") || info.first!!.contains(
                                "ro.semc"
                            )
                        ) {
                            Timber.tag(TAG).d("%s=%s", info.first, info.second)
                        }
                    }
                } else {
                    Timber.tag(TAG).d("Fingerprint: %s", Build.FINGERPRINT)
                    Timber.tag(TAG).d("ro.build.version.codename=%s", Build.VERSION.CODENAME)
                    Timber.tag(TAG).d("ro.build.version.incremental=%s", Build.VERSION.INCREMENTAL)
                    if (hasApiLevel(26)) Timber.tag(TAG)
                        .d("ro.build.version.base_os=%s", Build.VERSION.BASE_OS)
                    Timber.tag(TAG).d("ro.build.version.release=%s", Build.VERSION.RELEASE)
                    Timber.tag(TAG).d("ro.build.display.id=%s", Build.DISPLAY)
                    Timber.tag(TAG).d("ro.product.name=%s", Build.PRODUCT)
                    Timber.tag(TAG).d("ro.product.device=%s", Build.DEVICE)
                    Timber.tag(TAG).d("ro.product.board=%s", Build.BOARD)
                    Timber.tag(TAG).d("ro.product.manufacturer=%s", Build.MANUFACTURER)
                    Timber.tag(TAG).d("ro.product.brand=%s", Build.BRAND)
                    Timber.tag(TAG).d("ro.product.model=%s", Build.MODEL)
                    Timber.tag(TAG).d("ro.bootloader=%s", Build.BOOTLOADER)
                }
            }
            .launchIn(debugScope)
    }

    private fun getSystemBuildProp(): List<Pair<String, String>> {
        val result = ArrayList<Pair<String, String>>()
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(FileReader(File("/system/build.prop")))
            var line: String
            while (true) {
                line = reader.readLine() ?: break
                val cut = line.indexOf("=")
                if (cut == -1) continue
                result.add(Pair(line.substring(0, cut), line.substring(cut + 1, line.length)))
            }
            reader.close()
        } catch (e: Exception) {
            Timber.e(e)
            try {
                reader?.close()
            } catch (ignore: Exception) {
            }

        }
        return result
    }

    companion object {
        val TAG = logTag("Debug", "BuildProp")
    }

    @AssistedFactory
    interface Factory : DebugModule.Factory<BuildPropPrinter>
}