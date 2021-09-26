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
import timber.log.Timber

@Suppress("ProtectedInFinal")
class RecyclerViewWrapperLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @BindView(R.id.empty_container) protected lateinit var emptyContainer: ViewGroup
    @BindView(R.id.empty_icon) protected lateinit var emptyIconView: ImageView
    @BindView(R.id.empty_text) protected lateinit var emptyTextView: TextView
    @BindView(R.id.explanation_container) protected lateinit var explanationContainer: ViewGroup
    @BindView(R.id.explanation_icon) protected lateinit var explanationIconView: ImageView
    @BindView(R.id.explanation_text) protected lateinit var explanationTextView: TextView
    @BindView(R.id.loading_overlay) protected lateinit var loadingOverlayView: LoadingOverlayView

    protected val recyclerView: RecyclerView by lazy {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is RecyclerView) return@lazy child
        }
        throw IllegalArgumentException("No RecyclerView found")
    }

    protected var currentAdapter: RecyclerView.Adapter<*>? = null
    protected lateinit var state: State
    private val dataListener = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            state = state.copy(dataCount = currentAdapter?.itemCount ?: -1)

            if (!state.isPreFirstChange && state.isLoading && state.consumeFirstLoading) {
                state = state.copy(isLoading = false, consumeFirstLoading = false)
            }

            updateStates()

            if (state.isPreFirstChange) {
                state = state.copy(isPreFirstChange = false)
            }
            if (state.isPreFirstData && state.dataCount > 0) {
                state = state.copy(isPreFirstData = false)
            }
        }
    }

    var loadingBehavior: (State) -> Boolean = {
        it.isLoading
    }

    var explanationBehavior: (State) -> Boolean = {
        it.hasExplanation && it.isPreFirstData
                && !it.isLoading
                && it.dataCount == 0
    }

    var emptyBehavior: (State) -> Boolean = {
        (!it.hasExplanation || !it.isPreFirstData)
                && !it.isLoading
                && it.dataCount == 0
    }

    init {
        View.inflate(getContext(), R.layout.view_recyclerview_wrapper_layout, this)
        ButterKnife.bind(this)

        lateinit var typedArray: TypedArray
        try {
            typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RecyclerViewWrapperLayout)

            val loadingShow = typedArray.getBoolean(R.styleable.RecyclerViewWrapperLayout_rvwLoading, false)
            val consumeFirstLoading =
                typedArray.getBoolean(R.styleable.RecyclerViewWrapperLayout_rvwLoadingUntilFirstChange, false)

            val explanationIcon = typedArray.getDrawableRes(R.styleable.RecyclerViewWrapperLayout_rvwExplanationIcon)
            if (explanationIcon != null) explanationIconView.setImageResource(explanationIcon)

            val explanationText = typedArray.getStringOrRef(R.styleable.RecyclerViewWrapperLayout_rvwExplanationText)
            if (explanationText != null) explanationTextView.text = explanationText

            val emptyIcon = typedArray.getDrawableRes(R.styleable.RecyclerViewWrapperLayout_rvwEmptyIcon)
            if (emptyIcon != null) emptyIconView.setImageResource(emptyIcon)

            val emptyText = typedArray.getStringOrRef(R.styleable.RecyclerViewWrapperLayout_rvwEmptyText)
            if (emptyText != null) {
                emptyTextView.text = emptyText
            } else {
                if ((0..5).random() == 0) {
                    emptyTextView.setText(R.string.empty_list_easter_msg)
                } else {
                    emptyTextView.setText(R.string.empty_list_msg)
                }
            }

            state = State(
                hasExplanation = explanationText != null,
                isLoading = loadingShow,
                consumeFirstLoading = consumeFirstLoading
            )
        } finally {
            typedArray.recycle()
        }
    }

    override fun onFinishInflate() {
        updateStates()
        super.onFinishInflate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (currentAdapter != recyclerView.adapter) {
            Timber.v("Updating tracked adapter")
            currentAdapter?.unregisterAdapterDataObserver(dataListener)
            currentAdapter = recyclerView.adapter
            currentAdapter?.registerAdapterDataObserver(dataListener)
            dataListener.onChanged()
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    protected fun updateStates() {
        loadingOverlayView.setInvisible(!loadingBehavior(state))
        explanationContainer.setInvisible(!explanationBehavior(state))
        emptyContainer.setInvisible(!emptyBehavior(state))
    }

    fun setEmptyState(@DrawableRes iconRes: Int? = null, @StringRes stringRes: Int? = null) {
        if (iconRes != null) emptyIconView.setImageResource(iconRes)
        if (stringRes != null) emptyTextView.setText(stringRes)
    }

    fun updateLoadingState(isLoading: Boolean) {
        state = state.copy(isLoading = isLoading)
        updateStates()
    }

    data class State(
        val isPreFirstData: Boolean = true,
        val isPreFirstChange: Boolean = true,
        val isLoading: Boolean,
        val consumeFirstLoading: Boolean,
        val hasExplanation: Boolean,
        val dataCount: Int = 0
    )

}