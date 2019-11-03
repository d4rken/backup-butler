package eu.darken.bb.common.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults

@Suppress("ProtectedInFinal")
class EmptyRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @BindView(R.id.recyclerview) protected lateinit var recyclerView: RecyclerView
    @BindView(R.id.empty_container) protected lateinit var emptyContainer: ViewGroup
    @BindView(R.id.empty_icon) protected lateinit var emptyIconView: ImageView
    @BindView(R.id.empty_text) protected lateinit var emptyTextView: TextView
    @BindView(R.id.explanation_container) protected lateinit var explanationContainer: ViewGroup
    @BindView(R.id.explanation_icon) protected lateinit var explanationIconView: ImageView
    @BindView(R.id.explanation_text) protected lateinit var explanationTextView: TextView

    internal var shouldDisplayExplanation = true

    private val dataListener = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            recyclerView.setInvisible(recyclerView.adapter?.itemCount == 0)

            if (shouldDisplayExplanation && recyclerView.adapter?.itemCount != 0) {
                shouldDisplayExplanation = false
            }

            emptyContainer.setInvisible(shouldDisplayExplanation || recyclerView.adapter?.itemCount != 0)
            explanationContainer.setGone(!shouldDisplayExplanation || recyclerView.adapter?.itemCount != 0)
        }
    }

    init {
        View.inflate(getContext(), R.layout.view_recyclerview_empty, this)
        ButterKnife.bind(this)

        lateinit var typedArray: TypedArray
        try {
            typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.EmptyRecyclerView)


            val emptyIcon = typedArray.getDrawableRes(R.styleable.EmptyRecyclerView_ervEmptyIcon)
            if (emptyIcon != null) emptyIconView.setImageResource(emptyIcon)

            val emptyText = typedArray.getStringOrRef(R.styleable.EmptyRecyclerView_ervEmptyText)
            if (emptyText != null) {
                emptyTextView.text = emptyText
            } else {
                if ((0..5).random() == 0) {
                    emptyTextView.setText(R.string.empty_list_msg_easter)
                } else {
                    emptyTextView.setText(R.string.empty_list_msg)
                }
            }


            val explanationIcon = typedArray.getDrawableRes(R.styleable.EmptyRecyclerView_ervExplanationIcon)
            if (explanationIcon != null) explanationIconView.setImageResource(explanationIcon)

            val explanationText = typedArray.getStringOrRef(R.styleable.EmptyRecyclerView_ervExplanationText)
            if (explanationText != null) explanationTextView.text = explanationText
            explanationContainer.setGone(explanationText == null)


            emptyContainer.setInvisible(explanationText != null)
            shouldDisplayExplanation = explanationText != null

        } finally {
            typedArray.recycle()
        }
    }

    fun setEmptyInfo(@DrawableRes iconRes: Int = R.drawable.ic_emoji_neutral, @StringRes textRes: Int = R.string.msg_empty_list) {
        emptyIconView.setImageResource(iconRes)
        emptyTextView.setText(textRes)
    }

    fun setExplanationInfo(@DrawableRes iconRes: Int? = null, @StringRes textRes: Int? = null) {
        if (iconRes != null) explanationIconView.setImageResource(iconRes)
        if (textRes != null) explanationTextView.setText(textRes)
        explanationContainer.setGone(textRes == null)
    }

    var adapter: ModularAdapter<*>?
        get() = recyclerView.adapter as ModularAdapter<*>
        set(value) {
            recyclerView.adapter?.unregisterAdapterDataObserver(dataListener)
            value?.registerAdapterDataObserver(dataListener)
            recyclerView.adapter = value
        }

    fun setupDefaults(adapter: ModularAdapter<*>, dividers: Boolean = true) {
        recyclerView.adapter?.unregisterAdapterDataObserver(dataListener)
        adapter.registerAdapterDataObserver(dataListener)
        recyclerView.setupDefaults(adapter, dividers)
    }
}