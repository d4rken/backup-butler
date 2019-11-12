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
import eu.darken.bb.App
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.pkgs.IPCFunnel
import eu.darken.bb.common.previews.PkgUriPreviewRequest
import eu.darken.bb.common.previews.UriHelper
import javax.inject.Inject


class PkgUriIconLoader(val ipcFunnel: IPCFunnel) : ModelLoader<PkgUriPreviewRequest, AppIconData> {

    override fun buildLoadData(request: PkgUriPreviewRequest, width: Int, height: Int, options: Options): ModelLoader.LoadData<AppIconData>? {
        return ModelLoader.LoadData(ObjectKey(request), AppDataFetcher(ipcFunnel, request))
    }

    override fun handles(request: PkgUriPreviewRequest): Boolean {
        return request.uri.scheme == UriHelper.APP_SCHEME
    }

    private class AppDataFetcher constructor(val ipcFunnel: IPCFunnel, val request: PkgUriPreviewRequest) : DataFetcher<AppIconData> {

        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in AppIconData>) {
            val pkgName = request.uri.host

            val applicationInfo = ipcFunnel.submit(IPCFunnel.AppInfoQuery(pkgName!!))
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

    @PerApp
    class Factory @Inject constructor(val ipcFunnelProvider: Lazy<IPCFunnel>) : ModelLoaderFactory<PkgUriPreviewRequest, AppIconData> {

        override fun build(multiModelLoaderFactory: MultiModelLoaderFactory): ModelLoader<PkgUriPreviewRequest, AppIconData> {
            return PkgUriIconLoader(ipcFunnelProvider.get())
        }

        override fun teardown() {
            // Do nothing.
        }
    }

    companion object {
        internal val TAG = App.logTag("Preview", "PackageNameUriLoader")
    }
}
