package com.jetsynthesys.rightlife.ai_package.ui.thinkright.adapter

import android.content.Context
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

class ToolAdapter(context: Context, private val tools: List<ModuleData>, val onToolItem: (ModuleData, Int, Boolean) -> Unit
) : RecyclerView.Adapter<ToolAdapter.ToolViewHolder>() {

    val mContext = context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tool, parent, false)
        return ToolViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.tv_module_name).setText(tools.getOrNull(position)?.title)
        when (tools.getOrNull(position)?.title) {
            "Affirmation" ->{
                Glide.with(mContext)
                    .load("https://jetsynthesisqa-us-east-1.s3.amazonaws.com/media/cms/content/module/dccba8e93b41dc3f856d7c18b13d5783.png")
                    .placeholder(R.drawable.ic_plus)
                    .into(holder.itemView.findViewById<ImageView>(R.id.iv_selected))
            }
            "Your Affirmation Playlist" ->{
                Glide.with(mContext)
                    .load(R.drawable.quote_icon)
                    .placeholder(R.drawable.quote_icon)
                    .into(holder.itemView.findViewById<ImageView>(R.id.iv_selected))
            }
            "Box Breathing" ->{
                Glide.with(mContext)
                    .load("https://jetsynthesisqa-us-east-1.s3.amazonaws.com/media/cms/content/module/62e1e78cca28251bf83e46bc0018ba0b.png")
                    .placeholder(R.drawable.ic_plus)
                    .into(holder.itemView.findViewById<ImageView>(R.id.iv_selected))
            }
            "Alternate Nostril Breathing" ->{
                Glide.with(mContext)
                    .load( "https://jetsynthesisqa-us-east-1.s3.amazonaws.com/media/cms/content/module/4a37e4954896bd3a53d90424be77a724.png")
                    .placeholder(R.drawable.ic_plus)
                    .into(holder.itemView.findViewById<ImageView>(R.id.iv_selected))
            }
            "Equal Breathing" ->{
                Glide.with(mContext)
                    .load( "https://jetsynthesisqa-us-east-1.s3.amazonaws.com/media/cms/content/module/4a37e4954896bd3a53d90424be77a724.png")
                    .placeholder(R.drawable.ic_plus)
                    .into(holder.itemView.findViewById<ImageView>(R.id.iv_selected))
            }
            "4-7-8 Breathing" ->{
                Glide.with(mContext)
                    .load(  "https://jetsynthesisqa-us-east-1.s3.amazonaws.com/media/cms/content/module/c88072eb32c47a1e385fae66696dc293.png")
                    .placeholder(R.drawable.ic_plus)
                    .into(holder.itemView.findViewById<ImageView>(R.id.iv_selected))
            }
            "Free Form" -> {
                Glide.with(mContext)
                    .load(  R.drawable.ic_freeform_journal)
                    .placeholder(R.drawable.ic_plus)
                    .into(holder.itemView.findViewById<ImageView>(R.id.iv_selected))
            }

            "Bullet" -> {
                Glide.with(mContext)
                    .load(  R.drawable.ic_bullet_journal)
                    .placeholder(R.drawable.ic_plus)
                    .into(holder.itemView.findViewById<ImageView>(R.id.iv_selected))
            }

            "Gratitude" -> {
                Glide.with(mContext)
                    .load(  R.drawable.ic_gratitude_journal)
                    .placeholder(R.drawable.ic_plus)
                    .into(holder.itemView.findViewById<ImageView>(R.id.iv_selected))
            }

            "Grief" -> {
                Glide.with(mContext)
                    .load(  R.drawable.ic_grief_journal)
                    .placeholder(R.drawable.ic_plus)
                    .into(holder.itemView.findViewById<ImageView>(R.id.iv_selected))
            }
            "Custom" -> {
                Glide.with(mContext)
                    .load(  R.drawable.custom_image)
                    .placeholder(R.drawable.ic_plus)
                    .into(holder.itemView.findViewById<ImageView>(R.id.iv_selected))
            }
        }

        holder.mainLayout.setOnClickListener {
            tools.getOrNull(position)?.let { it1 ->
                onToolItem(it1, position, true) }
        }
    }

    override fun getItemCount(): Int = tools.size

    class ToolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
     //   private val moduleName: TextView = itemView.findViewById(R.id.tv_module_name)
     //   private val moduleType: TextView = itemView.findViewById(R.id.tv_module_type)
     //   private val subtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
    //    private val selectedIcon: ImageView = itemView.findViewById(R.id.iv_selected)
        val mainLayout: CardView = itemView.findViewById<CardView>(R.id.mainLayout)

        // moduleType.text = tool.moduleType
        // subtitle.text = tool.subtitle
        //selectedIcon.visibility = if (tool.isSelectedModule) View.VISIBLE else View.GONE
    }
}