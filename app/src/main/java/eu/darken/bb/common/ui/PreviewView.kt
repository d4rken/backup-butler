package eu.darken.bb.common.ui

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import eu.darken.bb.databinding.ViewPreviewBinding


class PreviewView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ui = ViewPreviewBinding.inflate(layoutInflator, this)
    val image = ui.previewImage
    val placeHolder = ui.previewPlaceholder

    init {
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
