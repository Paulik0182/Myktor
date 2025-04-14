package com.nayya.myktor.ui.product.productview

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nayya.myktor.R
import com.nayya.myktor.data.BASE_URL

class ImagePagerAdapter(
    private val imageUrls: List<String>
) : RecyclerView.Adapter<ImagePagerAdapter.ViewHolder>() {

    inner class ViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        return ViewHolder(imageView)
    }

    override fun getItemCount(): Int = imageUrls.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.imageView.context)
            .asBitmap()
            .load(BASE_URL + imageUrls[position])
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.imageView)

//        Glide.with(holder.imageView.context)
//            .asBitmap()
//            .load(if (imageUrls[position].isNotBlank()) BASE_URL + imageUrls[position] else null)
//            .placeholder(R.drawable.ic_placeholder)
//            .error(R.drawable.ic_placeholder)
//            .into(holder.imageView)

//            .load(imageUrls.getOrNull(position)?.let { BASE_URL + it })
//            .placeholder(R.drawable.ic_placeholder)
//            .fallback(R.drawable.ic_placeholder) // если URL == null
//            .error(R.drawable.ic_placeholder)    // если загрузка не удалась
    }
}
