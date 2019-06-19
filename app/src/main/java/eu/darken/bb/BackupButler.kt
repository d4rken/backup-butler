package eu.darken.bb

import android.content.Context
import eu.darken.bb.common.dagger.AppContext
import io.reactivex.Single
import javax.inject.Inject

class BackupButler @Inject constructor(
        @AppContext val context: Context
) {

    val appInfo: Single<AppInfo> = Single.create<AppInfo> {
        val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        it.onSuccess(AppInfo(
                versionCode = pkgInfo.versionCode.toLong(),
                versionName = pkgInfo.versionName,
                buildState = if (BuildConfig.DEBUG) BuildState.DEV else BuildState.ALPHA,
                gitSha = BuildConfig.GITSHA,
                buildTime = BuildConfig.BUILD_TYPE

        ))
    }.cache()


    data class AppInfo(
            val versionCode: Long,
            val versionName: String,
            val buildState: BuildState,
            val gitSha: String,
            val buildTime: String
    )

    enum class BuildState {
        DEV, ALPHA
    }
}