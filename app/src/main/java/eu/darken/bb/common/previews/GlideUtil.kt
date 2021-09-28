package eu.darken.bb.common.previews

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.*
import android.os.Build
import androidx.core.graphics.BitmapCompat
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.debug.logging.logTag


object GlideUtil {
    val TAG = logTag("Preview", "GlideUtil")

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun getScaledBitmapFromDrawable(drawable: Drawable, width: Int, height: Int): Bitmap? {
        if (drawable is BitmapDrawable) {
            return Bitmap.createScaledBitmap(drawable.bitmap, width, height, true)
        } else if (drawable is StateListDrawable) {
            drawable.mutate()
            val constantState = drawable.constantState
            if (constantState is DrawableContainer.DrawableContainerState) {
                val drawables = constantState.children
                for (drwbl in drawables) {
                    if (drwbl is BitmapDrawable)
                        return Bitmap.createScaledBitmap(drwbl.bitmap, width, height, true)
                }
            }
        } else if (ApiHelper.hasLolliPop() && drawable is VectorDrawable) {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)
            return bitmap
        } else {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)
            return bitmap
        }
        return null
    }

    fun getDrawableSize(drawable: Drawable): Long {
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            return if (bitmap == null) {
                1
            } else {
                BitmapCompat.getAllocationByteCount(bitmap).toLong()
            }
        } else if (drawable is StateListDrawable) {
            return (drawable.intrinsicWidth * drawable.intrinsicHeight * 5).toLong()
        } else if (ApiHelper.hasLolliPop() && drawable is VectorDrawable) {
            return (drawable.intrinsicWidth * drawable.intrinsicHeight * 5).toLong()
        }
        return 1
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, targetWidth: Int, targetHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > targetHeight || width > targetWidth) {
            // Get ratios
            val heightRatio = Math.round(height.toFloat() / targetHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / targetWidth.toFloat())

            // Compare heigth with width so we get an image that fits the height and weidth requirements
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

}
