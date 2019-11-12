package eu.darken.bb.common.previews.model

import android.graphics.BitmapFactory

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
import eu.darken.bb.GeneralSettings
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.previews.FilePreviewRequest
import javax.inject.Inject


class SmartFileModelLoader constructor(
        val generalSettings: GeneralSettings
) : ModelLoader<FilePreviewRequest, FileData> {

    override fun buildLoadData(preview: FilePreviewRequest, width: Int, height: Int, options: Options): ModelLoader.LoadData<FileData>? {
        return ModelLoader.LoadData(ObjectKey(preview), FileFetcher(generalSettings, preview, options))
    }

    override fun handles(file: FilePreviewRequest): Boolean = true

    private class FileFetcher(
            val generalSettings: GeneralSettings,
            val preview: FilePreviewRequest,
            val options: Options
    ) : DataFetcher<FileData> {
        private var bitMapFactoryOptions: BitmapFactory.Options? = null

        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in FileData>) {
            val file = preview.file

            val previewsEnabled = generalSettings.isPreviewEnabled

            if (!previewsEnabled) {
                callback.onDataReady(FileData(file, FileData.Type.FALLBACK))
                return
            }

            if (file.name.endsWith(".apk")) {
                callback.onDataReady(FileData(file, FileData.Type.APK))
                return
            }

            if (EXTENSIONS_IMAGE.any { file.name.endsWith(it) }) {
                callback.onDataReady(FileData(file, FileData.Type.IMAGE))
                return
            }

            if (EXTENSIONS_VIDEO.any { file.name.endsWith(it) }) {
                callback.onDataReady(FileData(file, FileData.Type.VIDEO))
                return
            }

            if (EXTENSIONS_MUSIC.any { file.name.endsWith(it) }) {
                callback.onDataReady(FileData(file, FileData.Type.MUSIC))
                return
            }

            callback.onDataReady(FileData(file, FileData.Type.FALLBACK, preview.theme))
        }

        override fun cleanup() {
            // Do nothing.
        }

        override fun cancel() {
            if (bitMapFactoryOptions != null) bitMapFactoryOptions!!.requestCancelDecode()
        }

        override fun getDataClass(): Class<FileData> {
            return FileData::class.java
        }

        override fun getDataSource(): DataSource {
            return DataSource.LOCAL
        }
    }

    @PerApp
    class Factory @Inject constructor(
            val generalSettingsLazy: Lazy<GeneralSettings>
    ) : ModelLoaderFactory<FilePreviewRequest, FileData> {

        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<FilePreviewRequest, FileData> {
            return SmartFileModelLoader(generalSettingsLazy.get())
        }

        override fun teardown() {
            // Do nothing.
        }
    }

    companion object {
        internal val TAG = App.logTag("Preview", "SmartFileModelLoader")
        internal val EXTENSIONS_IMAGE = arrayOf(".png", ".jpg", ".jpeg", ".bmp")
        internal val EXTENSIONS_MUSIC = arrayOf(".wav", ".mp3", ".wma", ".raw", ".aac", ".flac", ".m4a", ".ogg")
        internal val EXTENSIONS_VIDEO = arrayOf(".webm", ".3gp", ".avi", ".mkv", ".mpg", ".mpeg", ".3pg", ".flv", ".m4v", ".wmv", ".mp4")
    }
}
