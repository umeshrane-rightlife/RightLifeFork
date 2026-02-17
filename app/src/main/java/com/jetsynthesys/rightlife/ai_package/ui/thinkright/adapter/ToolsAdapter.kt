package com.jetsynthesys.rightlife.ai_package.ui.thinkright.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.ModuleData
import com.jetsynthesys.rightlife.ai_package.model.ToolGridData
import com.jetsynthesys.rightlife.ai_package.model.ToolsData

class ToolsAdapter( context: Context, private var dataLists: ArrayList<ToolGridData>?,
                   val onToolItem: (ToolGridData, Int, Boolean) -> Unit)
    : RecyclerView.Adapter<ToolsAdapter.ViewHolder>() {

        val mContext = context
    val imageList = arrayListOf(
        R.drawable.breathwork_icon,    // Replace with actual drawable names
        R.drawable.quote_grey_icon,
        R.drawable.journaling_ink_icon,
        R.drawable.meditate_icon
    )

    val colorList = arrayListOf(
        R.color.blue_bar,
        R.color.thinkright_text_color,
        R.color.ql_affirmation_color,
        R.color.meditate_text_color
    )
    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tools_card_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToolsAdapter.ViewHolder, position: Int) {
        Glide.with(mContext)
            .load( imageList.get(position))
            .placeholder(R.drawable.ic_plus)
            .into(holder.itemView.findViewById<ImageView>(R.id.iconImageView))
        if(dataLists?.getOrNull(position)?.moduleName == "Affirmation"){
            holder.itemView.findViewById<TextView>(R.id.titleTextView).setText("Affirmations")
        }else{
            holder.itemView.findViewById<TextView>(R.id.titleTextView).setText(dataLists?.getOrNull(position)?.moduleName)
        }
       // dataLists?.getOrNull(position)?.image.let { holder.itemView.findViewById<ImageView>(R.id.iconImageView).setImageResource(it) }

        holder.itemView.findViewById<TextView>(R.id.subtitleTextView).setText(dataLists?.getOrNull(position)?.subtitle)
        // colorList.getOrNull(position)?.let { holder.itemView.findViewById<TextView>(R.id.titleTextView).setTextColor(it) }

        when (position % 3) {
            0 -> holder.itemView.findViewById<TextView>(R.id.titleTextView).setTextColor(mContext.resources.getColor(R.color.color_blue))
            2 -> holder.itemView.findViewById<TextView>(R.id.titleTextView).setTextColor(mContext.resources.getColor(R.color.ql_journaling_color))
            1 -> holder.itemView.findViewById<TextView>(R.id.titleTextView).setTextColor(mContext.resources.getColor(R.color.ql_affirmation_color))
        }
        if (position == 3){
            holder.itemView.findViewById<CardView>(R.id.mainLayout).visibility = View.GONE
        }else{
            holder.itemView.findViewById<CardView>(R.id.mainLayout).visibility = View.VISIBLE
        }


        holder.itemView.findViewById<CardView>(R.id.mainLayout).setOnClickListener {
            dataLists?.getOrNull(position)?.let { it1 -> onToolItem(it1, position, true) }
        }
    }

    override fun getItemCount(): Int {
        return dataLists?.size ?: 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}