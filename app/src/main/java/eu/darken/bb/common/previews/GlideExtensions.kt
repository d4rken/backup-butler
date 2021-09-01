package eu.darken.bb.common.previews

import android.widget.ImageView
import com.bumptech.glide.request.target.ViewTarget
import eu.darken.bb.common.ui.PreviewView

fun <T> GlideRequest<T>.into(previewView: PreviewView): ViewTarget<ImageView, T> {
    return listener(PlaceHolderRequestListener(previewView.image, previewView.placeHolder))
        .into(previewView.image)
}