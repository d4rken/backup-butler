package eu.darken.bb.common.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper

import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import eu.darken.bb.R


class SwitchPreferenceView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PreferenceView(context, attrs, defStyleAttr) {
    private lateinit var toggle: SwitchCompat

    var isChecked: Boolean
        get() = toggle.isChecked
        set(checked) {
            toggle.isChecked = checked
        }

    override fun onFinishInflate() {
        toggle = SwitchMaterial(ContextThemeWrapper(context, R.style.Preference_SwitchPreferenceCompat_Material))
        toggle.isClickable = false
        toggle.isFocusable = false
        isClickable = true
        addExtra(toggle)
//        setOnClickListener { performClick() }
        super.onFinishInflate()
    }

    fun setSwitchListener(listener: (SwitchPreferenceView, Boolean) -> Unit) {
        setOnClickListener {
            toggle.isChecked = !toggle.isChecked
            listener.invoke(this@SwitchPreferenceView, isChecked)
        }
    }
}
