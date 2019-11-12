package eu.darken.bb.common.previews.decoder

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
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
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.asFile
import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.local.isSymbolicLink
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
        val drawableRes: Int = when (file.pathType) {
            APath.Type.LOCAL -> decodeLocalPath(file as LocalPath)
            else -> R.drawable.ic_file_unknown_onsurface
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

    @DrawableRes
    private fun decodeLocalPath(file: LocalPath): Int {
        // TODO Support Root?
        val javaFile = file.asFile()
        return when {
            javaFile.isDirectory -> R.drawable.ic_folder_onsurface
            javaFile.isFile -> R.drawable.ic_file_onsurface
            javaFile.isSymbolicLink() -> R.drawable.ic_file_link_onsurface
            else -> R.drawable.ic_file_unknown_onsurface
        }
    }

    companion object {
        internal val TAG = App.logTag("Preview", "Decoder", "FallbackDecoder")
    }
}
