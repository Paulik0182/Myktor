package com.nayya.myktor.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.material.card.MaterialCardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.CornerFamily
import com.nayya.myktor.R

class ProfileMenuAdapter(
    private val onItemClick: (ProfileMenuType) -> Unit
) : RecyclerView.Adapter<ProfileMenuAdapter.MenuViewHolder>() {

    private var items: List<ProfileMenuType> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<ProfileMenuType>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = items[position]
        val prevItem = items.getOrNull(position - 1)
        val nextItem = items.getOrNull(position + 1)
        holder.bind(item, prevItem, nextItem)
    }

    override fun getItemCount(): Int = items.size

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val arrow: ImageView = itemView.findViewById(R.id.ivArrow)
        private val divider: View = itemView.findViewById(R.id.itemDivider)
        private val card: MaterialCardView = itemView.findViewById(R.id.cardContainer)

        fun bind(item: ProfileMenuType, prevItem: ProfileMenuType?, nextItem: ProfileMenuType?) {
            icon.setImageResource(item.iconResId)
            title.text = item.title
            itemView.setOnClickListener { onItemClick(item) }

            // Divider — только если внутри группы
            divider.visibility = if (nextItem != null && nextItem.group == item.group) View.VISIBLE else View.GONE

            // Corners
            val isFirstInGroup = prevItem == null || prevItem.group != item.group
            val isLastInGroup = nextItem == null || nextItem.group != item.group

            val topRadius = if (isFirstInGroup) 16f else 0f
            val bottomRadius = if (isLastInGroup) 16f else 0f

            card.shapeAppearanceModel = card.shapeAppearanceModel
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, topRadius)
                .setTopRightCorner(CornerFamily.ROUNDED, topRadius)
                .setBottomLeftCorner(CornerFamily.ROUNDED, bottomRadius)
                .setBottomRightCorner(CornerFamily.ROUNDED, bottomRadius)
                .build()

            // Top margin для отделения групп
            val topMargin = if (isFirstInGroup) 16.dpToPx(itemView.context) else 0
            val params = itemView.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = topMargin
            itemView.layoutParams = params
        }
    }

    fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}
