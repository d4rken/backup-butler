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

    protected val recyclerView: RecyclerView by lazy<RecyclerView> {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is RecyclerView) return@lazy child
        }
        throw IllegalArgumentException("No RecyclerView found")
    }

    protected var displayExplanation = true

    protected var currentAdapter: RecyclerView.Adapter<*>? = null
    protected var loadingAutoHide: Boolean = true
    protected var isFirstDataAvailable: Boolean = false
    protected var loadingShow: Boolean = false

    private val dataListener = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            recyclerView.setInvisible(currentAdapter?.itemCount == 0)

            if (displayExplanation && currentAdapter?.itemCount != 0) {
                displayExplanation = false
            }

            emptyContainer.setInvisible(displayExplanation || currentAdapter?.itemCount != 0 || (!isFirstDataAvailable && loadingShow))
            explanationContainer.setInvisible(!displayExplanation || currentAdapter?.itemCount != 0 || (!isFirstDataAvailable && loadingShow))

            if (!isFirstDataAvailable) {
                isFirstDataAvailable = true
                if (loadingAutoHide) loadingOverlayView.setInvisible(true)
            }
        }
    }

    init {
        View.inflate(getContext(), R.layout.view_recyclerview_wrapper_layout, this)
        ButterKnife.bind(this)

        lateinit var typedArray: TypedArray
        try {
            typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RecyclerViewWrapperLayout)

            loadingAutoHide = typedArray.getBoolean(R.styleable.RecyclerViewWrapperLayout_rvwLoadingAutoHide, true)
            loadingShow = typedArray.getBoolean(R.styleable.RecyclerViewWrapperLayout_rvwLoadingShowByDefault, false)
            loadingOverlayView.setInvisible(!loadingShow)

            val explanationIcon = typedArray.getDrawableRes(R.styleable.RecyclerViewWrapperLayout_rvwExplanationIcon)
            if (explanationIcon != null) explanationIconView.setImageResource(explanationIcon)

            val explanationText = typedArray.getStringOrRef(R.styleable.RecyclerViewWrapperLayout_rvwExplanationText)
            if (explanationText != null) explanationTextView.text = explanationText
            explanationContainer.setInvisible(explanationText == null || loadingShow)
            displayExplanation = explanationText != null

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
            emptyContainer.setInvisible(explanationText != null || loadingShow)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        checkAdapter()
        super.onLayout(changed, left, top, right, bottom)
    }

    protected fun checkAdapter() {
        if (currentAdapter != recyclerView.adapter) {
            Timber.v("Updating tracked adapter")
            currentAdapter?.unregisterAdapterDataObserver(dataListener)
            currentAdapter = recyclerView.adapter
            currentAdapter?.registerAdapterDataObserver(dataListener)
            dataListener.onChanged()
        }
    }

    fun setEmptyState(@DrawableRes iconRes: Int? = null, @StringRes stringRes: Int? = null) {
        if (iconRes != null) emptyIconView.setImageResource(iconRes)
        if (stringRes != null) emptyTextView.setText(stringRes)
    }

    fun setLoadingState(isLoading: Boolean) {
        loadingShow = isLoading
        loadingOverlayView.setInvisible(!isLoading)
    }
//
//    data class State(
//        val firstDataAvailable: Boolean = false,
//        val dataCount: Int = 0
//    )
//
//    interface Behavior {
//        fun onAdapterChanged(State: State)
//    }
//
//    var loadingOverlayBehavior: (State) -> Boolean = {
//
//    }


}