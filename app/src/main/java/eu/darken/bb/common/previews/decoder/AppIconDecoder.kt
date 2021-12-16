package eu.darken.bb.common.previews.decoder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.load.resource.drawable.DrawableDecoderCompat
import com.bumptech.glide.request.target.Target
import dagger.Lazy
import eu.darken.bb.R
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.previews.model.AppIconData
import kotlinx.coroutines.runBlocking
import timber.log.Timber


class AppIconDecoder(
    val context: Context,
    glide: Glide,
    val pkgOpsLazy: Lazy<PkgOps>
) : ResourceDecoder<AppIconData, Bitmap> {
    private val bitmapPool: BitmapPool = glide.bitmapPool

    override fun handles(source: AppIconData, options: Options): Boolean = true

    // TODO runBlocking is not nice, maybe check Coil?
    override fun decode(
        source: AppIconData,
        _width: Int,
        _height: Int,
        options: Options
    ): Resource<Bitmap>? = runBlocking {
        var icon: Drawable? = try {
            pkgOpsLazy.get().getIcon(source.applicationInfo)
        } catch (e: Exception) {
            // TODO, what kind of exception could we get here, do we need to catch?
            Timber.tag(TAG).w(e)
            null
        }

        if (icon == null) {
            icon = DrawableDecoderCompat.getDrawable(context, R.drawable.ic_default_appicon_onsurface, source.theme)
        }

        var bitmap: Bitmap? = null
        if (icon != null) {
            val targetWidth = if (_width == Target.SIZE_ORIGINAL) icon.intrinsicWidth else _width
            val targetHeight = if (_height == Target.SIZE_ORIGINAL) icon.intrinsicHeight else _height
            bitmap = icon.toBitmap(targetWidth, targetHeight)
        }

        bitmap?.let { BitmapResource(it, bitmapPool) }
    }

    companion object {
        internal val TAG = logTag("Preview", "Decoder", "AppDecoder")
    }
}
