package eu.darken.bb.common.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R


class PreviewView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @BindView(R.id.preview_image) lateinit var image: ImageView
    @BindView(R.id.preview_placeholder) lateinit var placeHolder: ImageView

    init {
        View.inflate(getContext(), R.layout.view_preview, this)
        ButterKnife.bind(this)

//        lateinit var typedArray: TypedArray
//        try {
//            typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SetupBarView)
//
//            buttonPositivePrimary.text = typedArray.getStringOrRef(R.styleable.SetupBarView_sbvPositivePrimaryLabel)
//            buttonPositivePrimary.setGone(buttonPositivePrimary.text.isNullOrEmpty())
//
//            buttonPositiveSecondary.text = typedArray.getStringOrRef(R.styleable.SetupBarView_sbvPositiveSecondaryLabel)
//            buttonPositiveSecondary.setGone(buttonPositiveSecondary.text.isNullOrEmpty())
//
//            buttonNegativeSecondary.text = typedArray.getStringOrRef(R.styleable.SetupBarView_sbvNegativeSecondaryLabel)
//            buttonNegativeSecondary.setGone(buttonNegativeSecondary.text.isNullOrEmpty())
//
//            buttonNegativePrimary.text = typedArray.getStringOrRef(R.styleable.SetupBarView_sbvNegativePrimaryLabel)
//            buttonNegativePrimary.setGone(buttonNegativePrimary.text.isNullOrEmpty())
//        } finally {
//            typedArray.recycle()
//        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        setEnabledRecursion(this, enabled)
    }

    private fun setEnabledRecursion(vg: ViewGroup, enabled: Boolean) {
        for (i in 0 until vg.childCount) {
            val child = vg.getChildAt(i)
            if (child is ViewGroup) setEnabledRecursion(child, enabled)
            else child.isEnabled = enabled
        }
    }
}
