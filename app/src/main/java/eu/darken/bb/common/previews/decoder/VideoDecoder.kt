package eu.darken.bb.common.previews.decoder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever

import com.bumptech.glide.Glide
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.load.resource.bitmap.VideoBitmapDecoder
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.previews.model.FileData

import java.io.IOException


class VideoDecoder(
        private val context: Context,
        glide: Glide
) : ResourceDecoder<FileData, Bitmap> {
    private val bitmapPool: BitmapPool = glide.bitmapPool

    @Throws(IOException::class)
    override fun handles(source: FileData, options: Options): Boolean {
        return source.type == FileData.Type.VIDEO
    }

    @Throws(IOException::class)
    override fun decode(fileData: FileData, _width: Int, _height: Int, options: Options): Resource<Bitmap>? {
        var bitmap: Bitmap? = when (fileData.file.pathType) {
            APath.Type.LOCAL -> decodeLocalPath(fileData.file as LocalPath, _width, _height, options)
            else -> null // TODO
        }

        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_file_video_onsurface)
        }
        return BitmapResource(bitmap!!, bitmapPool)
    }

    private fun decodeLocalPath(path: LocalPath, _width: Int, _height: Int, options: Options): Bitmap? {
        // TODO Support Root?
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        return try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(path.file.path)

            val frameTimeMicros = options.get(VideoBitmapDecoder.TARGET_FRAME)!!
            require(!(frameTimeMicros < 0 && frameTimeMicros != VideoBitmapDecoder.DEFAULT_FRAME)) {
                "Requested frame must be non-negative, or DEFAULT_FRAME, given: $frameTimeMicros"
            }
            val frameOption = options.get(VideoBitmapDecoder.FRAME_OPTION)

            when {
                frameTimeMicros == VideoBitmapDecoder.DEFAULT_FRAME -> mediaMetadataRetriever.frameAtTime
                frameOption == null -> mediaMetadataRetriever.getFrameAtTime(frameTimeMicros)
                else -> mediaMetadataRetriever.getFrameAtTime(frameTimeMicros, frameOption)
            }
        } catch (ignore: Exception) {
            null
        } finally {
            mediaMetadataRetriever?.release()
        }
    }

    companion object {
        internal val TAG = App.logTag("Preview", "Decoder", "VideoDecoder")
    }
}
