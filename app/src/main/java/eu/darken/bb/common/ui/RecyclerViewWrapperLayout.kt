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

    protected val recyclerView: RecyclerView by lazy<RecyclerView> {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is RecyclerView) return@lazy child
        }
        throw IllegalArgumentException("No RecyclerView found")
    }

    protected var shouldDisplayExplanation = true

    private val dataListener = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            recyclerView.setInvisible(currentAdapter?.itemCount == 0)

            if (shouldDisplayExplanation && currentAdapter?.itemCount != 0) {
                shouldDisplayExplanation = false
            }

            emptyContainer.setInvisible(shouldDisplayExplanation || currentAdapter?.itemCount != 0)
            explanationContainer.setGone(!shouldDisplayExplanation || currentAdapter?.itemCount != 0)
        }
    }

    internal var currentAdapter: RecyclerView.Adapter<*>? = null

    init {
        View.inflate(getContext(), R.layout.view_recyclerview_wrapper_layout, this)
        ButterKnife.bind(this)

        lateinit var typedArray: TypedArray
        try {
            typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RecyclerViewWrapperLayout)


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

            val explanationIcon = typedArray.getDrawableRes(R.styleable.RecyclerViewWrapperLayout_rvwExplanationIcon)
            if (explanationIcon != null) explanationIconView.setImageResource(explanationIcon)

            val explanationText = typedArray.getStringOrRef(R.styleable.RecyclerViewWrapperLayout_rvwExplanationText)
            if (explanationText != null) explanationTextView.text = explanationText
            explanationContainer.setGone(explanationText == null)


            emptyContainer.setInvisible(explanationText != null)
            shouldDisplayExplanation = explanationText != null

        } finally {
            typedArray.recycle()
        }
    }

//    override fun onFinishInflate() {
//        recyclerView.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
//            override fun onChildViewDetachedFromWindow(view: View) {}
//
//            override fun onChildViewAttachedToWindow(view: View) {
//                checkAdapter()
//            }
//
//        })
//        super.onFinishInflate()
//    }

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

    fun setEmptyInfos(@DrawableRes iconRes: Int? = null, @StringRes stringRes: Int? = null) {
        if (iconRes != null) emptyIconView.setImageResource(iconRes)
        if (stringRes != null) emptyTextView.setText(stringRes)
    }
}