package eu.darken.bb.common.previews.model

import android.content.Context

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.previews.AppPreviewRequest
import javax.inject.Inject


class AppInfoIconLoader : ModelLoader<AppPreviewRequest, AppIconData> {

    override fun buildLoadData(
        app: AppPreviewRequest,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<AppIconData>? {
        return ModelLoader.LoadData(ObjectKey(app), AppDataFetcher(app))
    }

    override fun handles(pkg: AppPreviewRequest): Boolean = true

    private class AppDataFetcher constructor(val iconRequest: AppPreviewRequest) : DataFetcher<AppIconData> {

        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in AppIconData>) {
            val applicationInfo = iconRequest.applicationInfo
            if (applicationInfo == null) callback.onLoadFailed(Exception("ApplicationInfo unavailable"))
            else callback.onDataReady(AppIconData(iconRequest.applicationInfo!!, iconRequest.theme))
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
    class Factory @Inject constructor(@AppContext val context: Context) :
        ModelLoaderFactory<AppPreviewRequest, AppIconData> {

        override fun build(multiModelLoaderFactory: MultiModelLoaderFactory): ModelLoader<AppPreviewRequest, AppIconData> {
            return AppInfoIconLoader()
        }

        override fun teardown() {
            // Do nothing.
        }
    }

    companion object {
        internal val TAG = App.logTag("Preview", "PkgIconLoader")
    }
}
