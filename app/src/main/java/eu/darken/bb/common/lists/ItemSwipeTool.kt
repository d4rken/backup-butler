package eu.darken.bb.common.lists

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import eu.darken.bb.App
import eu.darken.bb.common.UnitConverter
import timber.log.Timber
import kotlin.math.absoluteValue

@Suppress("UnnecessaryVariable")
class ItemSwipeTool(vararg actions: SwipeAction) {

    init {
        require(actions.isNotEmpty()) {
            "SwipeTool without actions doesn't make sense."
        }
        require(actions.map { it.direction }.toSet().size == actions.size) {
            "Duplicate direction actions are not allowed."
        }
    }

    private val touchCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            actions.map { it.direction }.fold(initial = 0, operation = { acc: Int, dir: SwipeAction.Direction -> acc.or(dir.value) })
    ) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, directionValue: Int) {
            val action = actions.single { it.direction.value == directionValue }
            Timber.tag(TAG).d("onSwiped(): %s", action)
            action.callback(viewHolder, action.direction)
        }

        override fun onChildDraw(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

            val iv = viewHolder.itemView

            val direction: SwipeAction.Direction? = when {
                dX > 0 -> SwipeAction.Direction.RIGHT
                dX < 0 -> SwipeAction.Direction.LEFT
                else -> null
            }

            val background = actions.find { it.direction == direction }?.background
            when (direction) {
                SwipeAction.Direction.RIGHT -> {
                    background?.setBounds(iv.left, iv.top, iv.left + dX.toInt(), iv.bottom)

                }
                SwipeAction.Direction.LEFT -> {
                    background?.setBounds(iv.right + dX.toInt(), iv.top, iv.right, iv.bottom)
                }
                else -> background?.setBounds(0, 0, 0, 0)
            }
            background?.draw(canvas)

            val defaultPadding = UnitConverter.dpToPx(recyclerView.context, 16f)

            when (direction) {
                SwipeAction.Direction.RIGHT -> {
                    val iconSwipeRight = actions.find { it.direction == direction }?.icon
                    if (iconSwipeRight != null) {
                        val iconTop = iv.top + iv.height / 2 - iconSwipeRight.intrinsicHeight / 2
                        val iconStart = defaultPadding
                        val iconEnd = iconStart + iconSwipeRight.intrinsicWidth
                        val iconBottom = iconTop + iconSwipeRight.intrinsicHeight
                        if (dX > iconEnd + defaultPadding) {
                            iconSwipeRight.bounds = Rect(iconStart, iconTop, iconEnd, iconBottom)
                        } else {
                            iconSwipeRight.bounds = Rect(0, 0, 0, 0)
                        }
                    }
                    actions.filter { it.icon != iconSwipeRight }.forEach {
                        it.icon?.setBounds(0, 0, 0, 0)
                    }
                }
                SwipeAction.Direction.LEFT -> {
                    val iconSwipeLeft = actions.find { it.direction == direction }?.icon
                    if (iconSwipeLeft != null) {
                        val iconTop = iv.top + iv.height / 2 - iconSwipeLeft.intrinsicHeight / 2
                        val iconStart = iv.width - defaultPadding - iconSwipeLeft.intrinsicWidth
                        val iconEnd = iconStart + iconSwipeLeft.intrinsicWidth
                        val iconBottom = iconTop + iconSwipeLeft.intrinsicHeight
                        if (iv.width - dX.absoluteValue < iconStart - defaultPadding) {
                            iconSwipeLeft.bounds = Rect(iconStart, iconTop, iconEnd, iconBottom)
                        } else {
                            iconSwipeLeft.bounds = Rect(0, 0, 0, 0)
                        }
                    }
                    actions.filter { it.icon != iconSwipeLeft }.forEach {
                        it.icon?.setBounds(0, 0, 0, 0)
                    }
                }
                else -> { // NONE
                    actions.forEach {
                        it.icon?.setBounds(0, 0, 0, 0)
                    }
                }
            }
            actions.forEach {
                it.icon?.draw(canvas)
            }
        }
    }
    private val touchHelper by lazy { ItemTouchHelper(touchCallback) }

    fun attach(recyclerView: RecyclerView) {
        touchHelper.attachToRecyclerView(recyclerView)
    }

    data class SwipeAction(
            val direction: Direction,
            val callback: (RecyclerView.ViewHolder, Direction) -> Unit,
            val icon: Drawable?,
            val background: Drawable?
    ) {
        enum class Direction(val value: Int) {
            LEFT(ItemTouchHelper.LEFT),
            RIGHT(ItemTouchHelper.RIGHT),
            START(ItemTouchHelper.START),
            END(ItemTouchHelper.END),
        }
    }

    companion object {
        internal val TAG = App.logTag("ItemSwipeTool")
    }

}