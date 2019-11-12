package eu.darken.bb.common.previews;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.request.target.ImageViewTarget;

import androidx.annotation.Nullable;

public class TintTarget extends ImageViewTarget<Drawable> {
    public TintTarget(ImageView view) {
        super(view);
    }

    @Override
    protected void setResource(@Nullable Drawable resource) {
        view.setImageDrawable(resource);
    }
}