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
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.common.previews.model.FileData
import java.io.IOException


class MusicDecoder(
    private val context: Context,
    glide: Glide
) : ResourceDecoder<FileData, Bitmap> {
    private val mBitmapPool: BitmapPool = glide.bitmapPool

    @Throws(IOException::class)
    override fun handles(source: FileData, options: Options): Boolean {
        return source.type == FileData.Type.MUSIC
    }

    @Throws(IOException::class)
    override fun decode(fileData: FileData, _width: Int, _height: Int, options: Options): Resource<Bitmap>? {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_file_music_onsurface)
        return BitmapResource(bitmap, mBitmapPool)
    }

    companion object {
        internal val TAG = App.logTag("Preview", "Decoder", "MusicDecoder")
    }
}
