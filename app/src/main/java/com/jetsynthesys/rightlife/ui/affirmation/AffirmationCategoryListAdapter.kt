package com.jetsynthesys.rightlife.ui.affirmation

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.ui.affirmation.pojo.AffirmationCategoryData
import com.jetsynthesys.rightlife.ui.utility.svgloader.GlideApp

class AffirmationCategoryListAdapter(
    private val context: Context,
    private val categoryList: List<AffirmationCategoryData>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AffirmationCategoryListAdapter.CategoryViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.row_affirmation_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.tvCategoryRow.text = category.title
        /*GlideApp.with(context)
            .load(ApiClient.CDN_URL_QA + category.image)
            .placeholder(R.drawable.rl_placeholder)
            .error(R.drawable.rl_placeholder)
            .into(holder.imageCategoryRow)*/

        Glide.with(context)
            .`as`(PictureDrawable::class.java)
            .load(ApiClient.CDN_URL_QA + category.image)
            .placeholder(R.drawable.rl_placeholder)
            .error(R.drawable.rl_placeholder)
            .into(holder.imageCategoryRow)

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(category)
        }
    }

    fun interface OnItemClickListener {
        fun onItemClick(category: AffirmationCategoryData)
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryRow: TextView = itemView.findViewById(R.id.tvCategoryRow)
        val imageCategoryRow: ImageView = itemView.findViewById(R.id.imageCategoryRow)
    }
}