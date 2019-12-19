package eu.darken.bb.common.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.ClipboardHelper
import eu.darken.bb.common.files.core.RawPath
import java.io.File

class BreadCrumbBar<ItemT> @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0,
        @StyleRes defStyleRes: Int = R.style.BreadCrumbBarStyle
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    @BindView(R.id.path_container) lateinit var currentPathLayout: LinearLayout
    @BindView(R.id.path_container_scrollview) lateinit var scrollView: HorizontalScrollView

    private val crumbs = mutableListOf<ItemT>()
    var crumbListener: ((ItemT) -> Unit)? = null
    var crumbNamer: ((ItemT) -> String)? = null

    val currentCrumb: ItemT?
        get() = if (crumbs.isEmpty()) null else crumbs[crumbs.size - 1]

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.browsingbar_breadcrumbbar_view, this)
    }

    override fun onFinishInflate() {
        ButterKnife.bind(this)
        if (isInEditMode) {

            setCrumbs(listOf(*RawPath.build("/this/is/darkens/test").path.split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) as List<ItemT>)
        } else
            scrollView.isSmoothScrollingEnabled = true

        super.onFinishInflate()
    }

    fun setCrumbs(crumbs: List<ItemT>) {
        this.crumbs.clear()
        this.crumbs.addAll(crumbs)
        updateBar()
    }

    private fun updateBar() {
        currentPathLayout.removeAllViews()
        for (crumb in crumbs) {
            val item = LayoutInflater.from(context).inflate(R.layout.browsingbar_crumb_view, currentPathLayout, false)

            val name = item.findViewById<TextView>(R.id.crumb_name)
            name.text = crumbNamer?.invoke(crumb) ?: crumb.toString()

            item.setOnLongClickListener {
                ClipboardHelper(context).copyToClipboard(crumb.toString())
                Toast.makeText(context, crumb.toString(), Toast.LENGTH_SHORT).show()
                true
            }
            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f)

            if (crumbs[crumbs.size - 1] == crumb) {
                name.setTextColor(ContextCompat.getColor(context, R.color.colorSecondary))
            }

            item.layoutParams = layoutParams
            item.setOnClickListener { crumbListener?.invoke(crumb) }
            currentPathLayout.addView(item)
        }
        scrollView.requestLayout()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        scrollView.fullScroll(View.FOCUS_RIGHT)
    }

    //    public static List<SDMFile> makeCrumbs(@NonNull SDMFile bread) {
    //        List<SDMFile> crumbs = new ArrayList<>();
    //        crumbs.add(bread);
    //        while (bread.getParentFile() != null) {
    //            crumbs.add(0, bread.getParentFile());
    //            bread = bread.getParentFile();
    //            Check.notNull(bread);
    //        }
    //        return crumbs;
    //    }

}
