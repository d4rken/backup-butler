package eu.darken.bb.common.previews.model

import android.content.pm.PackageManager
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import dagger.Lazy
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.previews.PkgUriPreviewRequest
import eu.darken.bb.common.previews.UriHelper
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton


class PkgUriIconLoader(val pkgOps: PkgOps) : ModelLoader<PkgUriPreviewRequest, AppIconData> {

    override fun buildLoadData(
        request: PkgUriPreviewRequest,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<AppIconData>? {
        return ModelLoader.LoadData(ObjectKey(request), AppDataFetcher(pkgOps, request))
    }

    override fun handles(request: PkgUriPreviewRequest): Boolean {
        return request.uri.scheme == UriHelper.APP_SCHEME
    }

    private class AppDataFetcher constructor(val pkgOps: PkgOps, val request: PkgUriPreviewRequest) :
        DataFetcher<AppIconData> {

        // TODO runBlocking is not nice, maybe try Coil?
        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in AppIconData>) = runBlocking {
            val pkgName = request.uri.host

            val applicationInfo = pkgOps.queryAppInfos(pkgName!!)
            if (applicationInfo != null) callback.onDataReady(AppIconData(applicationInfo))
            else callback.onLoadFailed(PackageManager.NameNotFoundException(pkgName))
        }

        override fun cleanup() {
            // Do nothing.
        }

        override fun cancel() {
            // Do nothing.
        }

        override fun getDataClass(): Class<AppIconData> = AppIconData::class.java

        override fun getDataSource(): DataSource = DataSource.LOCAL
    }

    @Singleton
    class Factory @Inject constructor(val pkgOpsLazy: Lazy<PkgOps>) :
        ModelLoaderFactory<PkgUriPreviewRequest, AppIconData> {

        override fun build(multiModelLoaderFactory: MultiModelLoaderFactory): ModelLoader<PkgUriPreviewRequest, AppIconData> {
            return PkgUriIconLoader(pkgOpsLazy.get())
        }

        override fun teardown() {
            // Do nothing.
        }
    }

    companion object {
        internal val TAG = logTag("Preview", "PackageNameUriLoader")
    }
}
