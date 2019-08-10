package eu.darken.bb.common.ui

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import eu.darken.bb.R
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.tryLocalizedErrorMessage

class LoadingOverlayView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var mode: Mode = Mode.LOADING
        set(value) {
            animation.setAnimation(value.animationRes)
            animation.addValueCallback(KeyPath("**"), LottieProperty.COLOR_FILTER) {
                PorterDuffColorFilter(context.getColorForAttr(R.attr.colorOnBackground), PorterDuff.Mode.SRC_ATOP)
            }
            animation.repeatCount = LottieDrawable.INFINITE
            animation.playAnimation()
            primaryText.setText(mode.defaultPrimary)
            field = value
        }

    @BindView(R.id.animation) lateinit var animation: LottieAnimationView
    @BindView(R.id.primary_text) lateinit var primaryText: TextView

    init {
        View.inflate(context, R.layout.loading_overlay_view, this)
        ButterKnife.bind(this)

        mode = Mode.LOADING
    }


    fun setError(error: Throwable?) {
        if (error == null) {
            mode = Mode.LOADING
            return
        }
        mode = Mode.ERROR
        primaryText.text = error.tryLocalizedErrorMessage(context)
    }

    enum class Mode(
            @RawRes val animationRes: Int,
            @StringRes val defaultPrimary: Int
    ) {
        LOADING(R.raw.anim_loading_box, R.string.progress_loading_label),
        ERROR(R.raw.anim_alert_octagon, R.string.error_generic_label)
    }
}