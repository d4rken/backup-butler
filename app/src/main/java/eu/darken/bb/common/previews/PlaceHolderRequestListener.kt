package eu.darken.bb.common.previews

import android.graphics.drawable.Animatable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import eu.darken.bb.R


class PlaceHolderRequestListener<T> @JvmOverloads constructor(
        private val imageHolder: ImageView,
        private val placeHolder: View? = null
) : RequestListener<T> {

    init {
        if (this.imageHolder.drawable is Animatable) {
            (this.imageHolder.drawable as Animatable).stop()
        }
        this.imageHolder.setImageBitmap(null)
        this.imageHolder.visibility = View.INVISIBLE
        if (this.placeHolder != null) this.placeHolder.visibility = View.VISIBLE
    }

    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<T>, isFirstResource: Boolean): Boolean {
        this.imageHolder.setImageResource(R.drawable.ic_error_onsurface)
        this.imageHolder.visibility = View.VISIBLE
        if (placeHolder != null) placeHolder.visibility = View.GONE
        return true
    }

    override fun onResourceReady(resource: T, model: Any, target: Target<T>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
        this.imageHolder.visibility = View.VISIBLE
        if (placeHolder != null) placeHolder.visibility = View.GONE
        if (resource is Animatable) {
            (resource as Animatable).start()
        }
        return false
    }
}
