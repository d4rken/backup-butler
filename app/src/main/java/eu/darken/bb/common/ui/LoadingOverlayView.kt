package eu.darken.bb.common.ui

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.View
import android.widget.Button
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

    @BindView(R.id.animation) lateinit var animation: LottieAnimationView
    @BindView(R.id.primary_text) lateinit var primaryText: TextView
    @BindView(R.id.cancel_button) lateinit var cancelButton: Button

    init {
        View.inflate(context, R.layout.loading_overlay_view, this)
        ButterKnife.bind(this)

        setMode(Mode.LOADING)
    }

    fun setMode(mode: Mode) {
        animation.setAnimation(mode.animationRes)
        animation.addValueCallback(KeyPath("**"), LottieProperty.COLOR_FILTER) {
            PorterDuffColorFilter(context.getColorForAttr(R.attr.colorOnBackground), PorterDuff.Mode.SRC_ATOP)
        }
        animation.repeatCount = LottieDrawable.INFINITE
        animation.playAnimation()
        primaryText.setText(mode.defaultPrimary)
    }

    fun setPrimaryText(@StringRes stringRes: Int) {
        this.setPrimaryText(context.getString(stringRes))
    }

    fun setPrimaryText(primary: String?) {
        if (primary == null) {
            primaryText.setText(R.string.progress_loading_label)
            return
        }
        primaryText.text = primary
    }

    fun updateWith(error: Throwable?) {
        if (error == null) {
            setMode(Mode.LOADING)
            return
        }
        setMode(Mode.ERROR)
        primaryText.text = error.tryLocalizedErrorMessage(context)
    }

    var isCancelable: Boolean = false
        set(value) {
            field = value
            cancelButton.setGone(!value)
        }

    fun setOnCancelListener(function: ((View) -> Unit)?) {
        cancelButton.setOnClickListener(function)
    }

    data class Mode(
            @RawRes val animationRes: Int,
            @StringRes val defaultPrimary: Int
    ) {
        companion object {
            val LOADING = Mode(R.raw.anim_loading_box, R.string.progress_loading_label)
            val ERROR = Mode(R.raw.anim_loading_box, R.string.progress_loading_label)
        }
    }

}