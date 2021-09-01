package eu.darken.bb.common.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R


open class PreferenceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    @BindView(R.id.icon) protected lateinit var iconView: ImageView
    @BindView(R.id.title) protected lateinit var titleView: TextView
    @BindView(R.id.description) protected lateinit var descriptionView: TextView
    @BindView(R.id.extra) protected lateinit var extraContainerView: ViewGroup

    init {
        View.inflate(getContext(), R.layout.view_preference, this)
        ButterKnife.bind(this)

        lateinit var typedArray: TypedArray
        try {
            typedArray = getContext().theme.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
            background = typedArray.getDrawable(0)
        } finally {
            typedArray.recycle()
        }

        try {
            typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PreferenceView)
            val iconRes = typedArray.getResourceId(R.styleable.PreferenceView_pvIcon, 0)
            if (iconRes != 0) iconView.setImageResource(iconRes)
            else iconView.visibility = View.GONE

            val titleRes = typedArray.getResourceId(R.styleable.PreferenceView_pvTitle, 0)
            if (titleRes != 0) {
                titleView.setText(titleRes)
            } else {
                titleView.text = typedArray.getNonResourceString(R.styleable.PreferenceView_pvTitle)
            }

            if (typedArray.hasValue(R.styleable.PreferenceView_pvDescription)) {
                val descId = typedArray.getResourceId(R.styleable.PreferenceView_pvDescription, 0)
                if (descId != 0) {
                    descriptionView.setText(descId)
                } else {
                    descriptionView.text = typedArray.getNonResourceString(R.styleable.PreferenceView_pvDescription)
                }
            }
            descriptionView.setGone(descriptionView.text.isNullOrEmpty())
        } finally {
            typedArray.recycle()
        }
    }

    fun addExtra(view: View?) {
        extraContainerView.addView(view)
        extraContainerView.visibility = if (view != null) View.VISIBLE else View.GONE
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        setEnabledRecursion(this, enabled)
    }

    private fun setEnabledRecursion(vg: ViewGroup, enabled: Boolean) {
        for (i in 0 until vg.childCount) {
            val child = vg.getChildAt(i)
            if (child is ViewGroup)
                setEnabledRecursion(child, enabled)
            else
                child.isEnabled = enabled
        }
    }

    fun setIcon(@DrawableRes iconRes: Int) {
        iconView.setImageResource(iconRes)
    }

    var description: String
        get() = descriptionView.text.toString()
        set(value) {
            descriptionView.text = value
            descriptionView.setGone(value.isEmpty())
        }
}
