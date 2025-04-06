package com.nayya.myktor.ui.product.editproduct

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nayya.myktor.R

class LinksAdapter(
    private val onLinkChanged: (Int, String) -> Unit,
    private val onAddClicked: () -> Unit,
    private val onRemoveClicked: (Int) -> Unit
) : ListAdapter<String, LinksAdapter.LinkViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_link, parent, false)
        return LinkViewHolder(view)
    }

    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class LinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val editText: EditText = itemView.findViewById(R.id.etLink)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)
        private val btnAdd: ImageButton = itemView.findViewById(R.id.btnAdd)

        fun bind(link: String, position: Int) {
            editText.setText(link)

            editText.doAfterTextChanged {
                onLinkChanged(position, it.toString())
            }

            btnRemove.setOnClickListener {
                onRemoveClicked(position)
            }

            // Показываем кнопку "добавить" только для последнего элемента
            btnAdd.visibility = if (position == currentList.lastIndex && currentList.size < 4) View.VISIBLE else View.GONE
            btnAdd.setOnClickListener {
                onAddClicked()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem === newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}
