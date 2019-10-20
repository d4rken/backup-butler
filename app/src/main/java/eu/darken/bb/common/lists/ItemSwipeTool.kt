package eu.darken.bb.common.lists

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import eu.darken.bb.R
import eu.darken.bb.common.UnitConverter
import kotlin.math.absoluteValue

@Suppress("UnnecessaryVariable")
class ItemSwipeTool(
        vararg directions: Direction
) {
    enum class Direction(val directionValue: Int) {
        LEFT(ItemTouchHelper.LEFT),
        RIGHT(ItemTouchHelper.RIGHT),
        START(ItemTouchHelper.START),
        END(ItemTouchHelper.END),
    }

    private val touchCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            directions.fold(initial = 0, operation = { acc: Int, dir: Direction -> acc.or(dir.directionValue) })
    ) {
        private val bg = ColorDrawable(Color.RED)
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            swipeCallback?.invoke(viewHolder, Direction.values().single { it.directionValue == direction })
        }

        override fun onChildDraw(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

            val iv = viewHolder.itemView

            when {
                // Swipe RIGHT
                dX > 0 -> bg.setBounds(iv.left, iv.top, iv.left + dX.toInt(), iv.bottom)
                // Swipe LEFT
                dX < 0 -> bg.setBounds(iv.right + dX.toInt(), iv.top, iv.right, iv.bottom)
                else -> bg.setBounds(0, 0, 0, 0)
            }
            bg.draw(canvas)

            val defaultPadding = UnitConverter.dpToPx(recyclerView.context, 16f)
            val leftIcon: Drawable? = AppCompatResources.getDrawable(recyclerView.context, R.drawable.ic_delete)
            val rightIcon: Drawable? = AppCompatResources.getDrawable(recyclerView.context, R.drawable.ic_delete)
            when {
                dX > 0 -> { // Swipe RIGHT
                    if (leftIcon != null) {
                        val iconTop = iv.height / 2 - leftIcon.intrinsicHeight / 2
                        val iconStart = defaultPadding
                        val iconEnd = iconStart + leftIcon.intrinsicWidth
                        val iconBottom = iconTop + leftIcon.intrinsicHeight
                        if (dX > iconEnd + defaultPadding) {
                            leftIcon.bounds = Rect(iconStart, iconTop, iconEnd, iconBottom)
                        }
                    }
                    rightIcon?.setBounds(0, 0, 0, 0)
                }
                dX < 0 -> { // Swipe LEFT
                    if (rightIcon != null) {
                        val iconTop = iv.height / 2 - rightIcon.intrinsicHeight / 2
                        val iconStart = iv.width - defaultPadding - rightIcon.intrinsicWidth
                        val iconEnd = iconStart + rightIcon.intrinsicWidth
                        val iconBottom = iconTop + rightIcon.intrinsicHeight
                        if (iv.width - dX.absoluteValue < iconStart - defaultPadding) {
                            rightIcon.bounds = Rect(iconStart, iconTop, iconEnd, iconBottom)
                        }
                    }
                    leftIcon?.setBounds(0, 0, 0, 0)
                }
                else -> { // NONE
                    leftIcon?.setBounds(0, 0, 0, 0)
                    rightIcon?.setBounds(0, 0, 0, 0)
                }
            }
            leftIcon?.draw(canvas)
            rightIcon?.draw(canvas)
        }
    }
    private val touchHelper = ItemTouchHelper(touchCallback)

    var swipeCallback: ((RecyclerView.ViewHolder, Direction) -> Unit)? = null
    @DrawableRes var iconLeftRes: Int? = null
    @ColorRes var colorLeftRes: Int? = null

    @DrawableRes var iconRightRes: Int? = null
    @ColorRes var colorRightRes: Int? = null

    fun attach(recyclerView: RecyclerView) {
        touchHelper.attachToRecyclerView(recyclerView)
    }

}