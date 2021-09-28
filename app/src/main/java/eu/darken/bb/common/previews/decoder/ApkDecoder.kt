package eu.darken.bb.common.previews.decoder

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.request.target.Target
import dagger.Lazy
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.GatewaySwitch
import eu.darken.bb.common.files.core.local.LocalPathLookup
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.previews.GlideUtil
import eu.darken.bb.common.previews.model.FileData
import timber.log.Timber


class ApkDecoder constructor(
    val context: Context,
    glide: Glide,
    val pkgOpsLazy: Lazy<PkgOps>,
    val gatewaySwitch: GatewaySwitch
) : ResourceDecoder<FileData, Bitmap> {
    private val bitmapPool: BitmapPool = glide.bitmapPool

    override fun handles(source: FileData, options: Options): Boolean {
        return source.type == FileData.Type.APK
    }

    override fun decode(fileData: FileData, _width: Int, _height: Int, options: Options): Resource<Bitmap>? {
        val bitmap: Bitmap? = when (fileData.file.pathType) {
            APath.PathType.LOCAL -> decodeLocalPath(fileData.file as LocalPathLookup, _width, _height, options)
            APath.PathType.SAF -> null // TODO
            else -> null
        }

        return if (bitmap == null) null else BitmapResource(bitmap, bitmapPool)
    }

    private fun decodeLocalPath(filePath: LocalPathLookup, _width: Int, _height: Int, options: Options): Bitmap? {
        // TODO Support Root?
        val packageInfo = pkgOpsLazy.get().viewArchive(filePath.path, PackageManager.GET_ACTIVITIES)
        if (packageInfo == null) return null

        val appInfo = packageInfo.applicationInfo
        if (appInfo == null) return null

        // http://stackoverflow.com/questions/5674683/how-to-show-icon-of-apk-in-my-file-manager
        appInfo.sourceDir = filePath.path
        appInfo.publicSourceDir = filePath.path
        var icon: Drawable? = null
        try {
            // FIXME pass through IPCFunnel?
            icon = appInfo.loadIcon(context.packageManager)
        } catch (e: OutOfMemoryError) {
            Timber.tag(TAG).w(e)
        }
        if (icon == null) return null

        val targetWidth = if (_width == Target.SIZE_ORIGINAL) icon.intrinsicWidth else _width
        val targetHeight = if (_height == Target.SIZE_ORIGINAL) icon.intrinsicHeight else _height
        return GlideUtil.getScaledBitmapFromDrawable(icon, targetWidth, targetHeight)
    }

    companion object {
        internal val TAG = logTag("Preview", "Decoder", "ApkDecoder")
    }
}
