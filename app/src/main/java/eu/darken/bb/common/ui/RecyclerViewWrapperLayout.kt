package eu.darken.bb.common.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isInvisible
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.RecyclerView
import eu.darken.bb.R
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.databinding.ViewRecyclerviewWrapperLayoutBinding
import timber.log.Timber

@Suppress("ProtectedInFinal")
class RecyclerViewWrapperLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ui = ViewRecyclerviewWrapperLayoutBinding.inflate(layoutInflator, this)

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
        override fun onChanged() = refresh()

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = refresh(itemCount)

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) = refresh(itemCount)

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = refresh(itemCount)

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = refresh(itemCount)

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = refresh(itemCount)

        override fun onStateRestorationPolicyChanged() = refresh()

        private fun refresh(itemCount: Int? = null) {
            state = state.copy(dataCount = (itemCount ?: currentAdapter?.itemCount) ?: -1)

            if (!state.isPreFirstChange && state.isLoading && state.consumeFirstLoading) {
                state = state.copy(isLoading = false, consumeFirstLoading = false)
            }

            postDelayed(100L) {
                updateUI()
            }

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
        lateinit var typedArray: TypedArray
        try {
            typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RecyclerViewWrapperLayout)

            val loadingShow = typedArray.getBoolean(R.styleable.RecyclerViewWrapperLayout_rvwLoading, false)
            val consumeFirstLoading =
                typedArray.getBoolean(R.styleable.RecyclerViewWrapperLayout_rvwLoadingUntilFirstChange, false)


            val explanationIcon = typedArray.getDrawableRes(R.styleable.RecyclerViewWrapperLayout_rvwExplanationIcon)
            if (explanationIcon != null) ui.explanationIcon.setImageResource(explanationIcon)

            val explanationText = typedArray.getStringOrRef(R.styleable.RecyclerViewWrapperLayout_rvwExplanationText)
            if (explanationText != null) ui.explanationText.text = explanationText

            val emptyIcon = typedArray.getDrawableRes(R.styleable.RecyclerViewWrapperLayout_rvwEmptyIcon)
            if (emptyIcon != null) ui.emptyIcon.setImageResource(emptyIcon)

            val emptyText = typedArray.getStringOrRef(R.styleable.RecyclerViewWrapperLayout_rvwEmptyText)
            if (emptyText != null) {
                ui.emptyText.text = emptyText
            } else {
                if ((0..5).random() == 0) {
                    ui.emptyText.setText(R.string.empty_list_easter_msg)
                } else {
                    ui.emptyText.setText(R.string.empty_list_msg)
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
        updateUI()
        super.onFinishInflate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (currentAdapter != recyclerView.adapter) {
            Timber.v("Updating tracked adapter")
            currentAdapter?.unregisterAdapterDataObserver(dataListener)
            currentAdapter = recyclerView.adapter?.also { it.registerAdapterDataObserver(dataListener) }
            dataListener.onChanged()
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    protected fun updateUI() {
        log { "updateStates(): $state" }
        ui.loadingOverlay.isInvisible = !loadingBehavior(state)
        ui.explanationContainer.isInvisible = !explanationBehavior(state)
        ui.emptyContainer.isInvisible = !emptyBehavior(state)
    }

    fun setEmptyState(@DrawableRes iconRes: Int? = null, @StringRes stringRes: Int? = null) {
        if (iconRes != null) ui.emptyIcon.setImageResource(iconRes)
        if (stringRes != null) ui.emptyText.setText(stringRes)
    }

    fun updateLoadingState(isLoading: Boolean) {
        state = state.copy(isLoading = isLoading)
        updateUI()
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