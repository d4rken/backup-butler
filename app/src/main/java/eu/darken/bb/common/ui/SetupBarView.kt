package eu.darken.bb.common.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import eu.darken.bb.R
import eu.darken.bb.databinding.ViewSetupbarBinding


class SetupBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var ui = ViewSetupbarBinding.inflate(layoutInflator, this, true)
    val buttonPositivePrimary = ui.actionPositivePrimary
    val buttonPositiveSecondary = ui.actionPositiveSecondary
    val buttonNegativePrimary = ui.actionNegativePrimary
    val buttonNegativeSecondary = ui.actionNegativeSecondary

    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))

        lateinit var typedArray: TypedArray
        try {
            typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SetupBarView)
            ui.apply {
                actionPositivePrimary.text =
                    typedArray.getStringOrRef(R.styleable.SetupBarView_sbvPositivePrimaryLabel)
                actionPositivePrimary.setGone(actionPositivePrimary.text.isNullOrEmpty())

                actionPositiveSecondary.text =
                    typedArray.getStringOrRef(R.styleable.SetupBarView_sbvPositiveSecondaryLabel)
                actionPositiveSecondary.setGone(actionPositiveSecondary.text.isNullOrEmpty())

                actionNegativeSecondary.text =
                    typedArray.getStringOrRef(R.styleable.SetupBarView_sbvNegativeSecondaryLabel)
                actionNegativeSecondary.setGone(actionNegativeSecondary.text.isNullOrEmpty())

                actionNegativePrimary.text =
                    typedArray.getStringOrRef(R.styleable.SetupBarView_sbvNegativePrimaryLabel)
                actionNegativePrimary.setGone(actionNegativePrimary.text.isNullOrEmpty())
            }

        } finally {
            typedArray.recycle()
        }
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
