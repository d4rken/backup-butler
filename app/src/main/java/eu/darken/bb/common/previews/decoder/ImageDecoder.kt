package eu.darken.bb.common.previews.decoder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import com.bumptech.glide.Glide
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.request.target.Target
import eu.darken.bb.App
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.previews.GlideUtil
import eu.darken.bb.common.previews.model.FileData

import java.io.IOException

class ImageDecoder(
    val context: Context,
    glide: Glide
) : ResourceDecoder<FileData, Bitmap> {
    private val bitmapPool: BitmapPool = glide.bitmapPool

    @Throws(IOException::class)
    override fun handles(source: FileData, options: Options): Boolean {
        return source.type == FileData.Type.IMAGE
    }

    @Throws(IOException::class)
    override fun decode(fileData: FileData, _width: Int, _height: Int, options: Options): Resource<Bitmap>? {
        val bitmap: Bitmap? = when (fileData.file.pathType) {
            APath.PathType.LOCAL -> decodeLocalPath(fileData.file as LocalPath, _width, _height, options)
            else -> null // TODO
        }

        return if (bitmap == null) null else BitmapResource(bitmap, bitmapPool)
    }

    private fun decodeLocalPath(item: LocalPath, _width: Int, _height: Int, options: Options): Bitmap {
        // TODO Support Root?
        val path = item.file.path
        val factoryOptions = BitmapFactory.Options()
        factoryOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, factoryOptions)

        val targetWidth = if (_width == Target.SIZE_ORIGINAL) factoryOptions.outWidth else _width
        val targetHeight = if (_height == Target.SIZE_ORIGINAL) factoryOptions.outHeight else _height
        // Calculate inSampleSize
        factoryOptions.inSampleSize = GlideUtil.calculateInSampleSize(factoryOptions, targetWidth, targetHeight)

        // Decode bitmap with inSampleSize set
        factoryOptions.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, factoryOptions)
    }

    companion object {
        internal val TAG = App.logTag("Preview", "Decoder", "ImageDecoder")
    }
}
