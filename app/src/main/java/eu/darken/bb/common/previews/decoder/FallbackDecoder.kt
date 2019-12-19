package eu.darken.bb.common.previews.decoder

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.load.resource.drawable.DrawableDecoderCompat
import com.bumptech.glide.request.target.Target
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.previews.model.FileData
import java.io.IOException


class FallbackDecoder(
        val context: Context,
        glide: Glide
) : ResourceDecoder<FileData, Bitmap> {
    private val bitmapPool: BitmapPool = glide.bitmapPool

    @Throws(IOException::class)
    override fun handles(file: FileData, options: Options): Boolean {
        return file.type == FileData.Type.FALLBACK
    }

    @Throws(IOException::class)
    override fun decode(source: FileData, _width: Int, _height: Int, options: Options): Resource<Bitmap>? {
        val file = source.file
        val drawableRes: Int = when (file.fileType) {
            APath.FileType.DIRECTORY -> R.drawable.ic_folder_onsurface
            APath.FileType.SYMBOLIC_LINK -> R.drawable.ic_file_link_onsurface
            APath.FileType.FILE -> R.drawable.ic_file_onsurface
        }

        // FIXME theme could leak?
        val drawable = DrawableDecoderCompat.getDrawable(context, drawableRes, source.theme)

        val bitmap = drawable?.let {
            val targetWidth = if (_width == Target.SIZE_ORIGINAL) drawable.intrinsicWidth else _width
            val targetHeight = if (_height == Target.SIZE_ORIGINAL) drawable.intrinsicHeight else _height
            drawable.toBitmap(targetWidth, targetHeight)
        }

        return if (bitmap != null) BitmapResource(bitmap, bitmapPool) else null
    }

    companion object {
        internal val TAG = App.logTag("Preview", "Decoder", "FallbackDecoder")
    }
}
