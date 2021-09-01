package eu.darken.bb

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import eu.darken.bb.common.CheckSummer
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@SuppressLint("PackageManagerGetSignatures")
@Suppress("DEPRECATION")
@PerApp
class BackupButler @Inject constructor(
    @AppContext val context: Context,
    private val packageManager: PackageManager
) {
    private val selfHealthPrefs = context.getSharedPreferences("selfhealth", Context.MODE_PRIVATE)


    val appInfo: AppInfo by lazy {
        val pkgInfo = packageManager.getPackageInfo(context.packageName, 0)
        AppInfo(
            versionCode = pkgInfo.versionCode.toLong(),
            versionName = pkgInfo.versionName,
            buildState = when {
                BuildConfig.DEBUG -> BuildState.DEV
                BuildConfig.BETA -> BuildState.BETA
                else -> BuildState.PRODUCTION
            },
            gitSha = BuildConfig.GITSHA,
            buildTime = BuildConfig.BUILD_TYPE
        )
    }

    val checksumApkMd5: String by lazy<String> {
        val info = packageManager.getPackageInfo(context.packageName, 0)

        val lastVersion = selfHealthPrefs.getLong("apk.chksm.version", 0)
        val lastMD5 = selfHealthPrefs.getString("apk.chksm.md5", null)
        if (lastMD5 != null && info.versionCode.toLong() == lastVersion) {
            return@lazy lastMD5
        }

        val checksum = CheckSummer.calculate(File(info.applicationInfo.sourceDir), CheckSummer.Type.MD5)!!
        selfHealthPrefs.edit().putLong("apk.chksm.version", info.versionCode.toLong()).apply()
        selfHealthPrefs.edit().putString("apk.chksm.md5", checksum).apply()
        checksum
    }

    val signatures: List<Signature> by lazy<List<Signature>> {
        packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures.toList()
    }

    val updateHistory by lazy<List<Long>> {
        val moshi = Moshi.Builder().build()
        val list = Types.newParameterizedType(List::class.java, Long::class.javaObjectType)
        val listAdapter = moshi.adapter<List<Long>>(list)
        val currentVersion = appInfo.versionCode

        val versionHistory = listAdapter.fromJson(selfHealthPrefs.getString("update.history", "[]")!!)!!.toMutableList()
        if (!versionHistory.contains(currentVersion)) {
            versionHistory.add(currentVersion)
            selfHealthPrefs.edit().putString("update.history", listAdapter.toJson(versionHistory)).apply()
            Timber.tag(App.TAG).d("Updated update history: %s", versionHistory)
        }

        versionHistory.toList()
    }

    data class AppInfo(
        val versionCode: Long,
        val versionName: String,
        val buildState: BuildState,
        val gitSha: String,
        val buildTime: String
    ) {

        val fullVersionString = "$versionName ($versionCode) [$gitSha]"
    }

    enum class BuildState {
        DEV, BETA, PRODUCTION
    }
}