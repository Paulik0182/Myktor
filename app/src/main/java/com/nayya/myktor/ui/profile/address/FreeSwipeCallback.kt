package com.nayya.myktor.ui.profile.address

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.nayya.myktor.R
import kotlin.math.pow

class FreeSwipeCallback(
    val context: Context,
    val onDelete: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private var swipedPosition = -1
    private var currentDx = 0f
    private var recyclerView: RecyclerView? = null

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

    // НЕ ограничиваем dX!
    override fun onChildDraw(
        c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {
        recyclerView = rv
        // Делаем плавнее
        val slowDx = dX * 0.6f
        // это эффект упругости свайпа
//        val slowDx = if (dX < 0) -(Math.abs(dX).pow(0.8f)) else dX

        currentDx = slowDx
        swipedPosition = vh.adapterPosition

        val itemView = vh.itemView

        // Фон рисуем до любого dX (свободно)
        if (slowDx < 0) {
            val paint = Paint().apply { color = Color.parseColor("#FFFFFFFF") }
            c.drawRect(
                itemView.right + slowDx, itemView.top.toFloat(),
                itemView.right.toFloat(), itemView.bottom.toFloat(),
                paint
            )
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_delete)
            val iconMargin = (itemView.height - (icon?.intrinsicHeight ?: 0)) / 2
            icon?.setBounds(
                itemView.right - iconMargin - (icon?.intrinsicWidth ?: 0),
                itemView.top + iconMargin,
                itemView.right - iconMargin,
                itemView.bottom - iconMargin
            )
            icon?.draw(c)
        }

        // Без ограничения dX!
        super.onChildDraw(c, rv, vh, slowDx, dY, actionState, isCurrentlyActive)
    }


    // НЕ удаляем из адаптера!
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // item всегда возвращается назад
        recyclerView?.post {
            recyclerView?.adapter?.notifyItemChanged(viewHolder.adapterPosition)
        }
    }

    // Слушаем отпускание пальца!
    fun attachTo(rv: RecyclerView) {
        ItemTouchHelper(this).attachToRecyclerView(rv)
        rv.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && swipedPosition != -1) {
                // если dX больше 18% ширины — показать диалог
                if (currentDx < -rv.width * 0.13f) {
                    onDelete(swipedPosition)
                }
                // item возвращается назад
                rv.post {
                    rv.adapter?.notifyItemChanged(swipedPosition)
                }
                swipedPosition = -1
                currentDx = 0f
            }
            false
        }
    }
}
