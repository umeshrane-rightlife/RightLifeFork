package com.jetsynthesys.rightlife.newdashboard

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.databinding.RowJumpBackInBinding
import com.jetsynthesys.rightlife.newdashboard.model.ContentDetails
import com.jetsynthesys.rightlife.ui.Articles.ArticlesDetailActivity
import com.jetsynthesys.rightlife.ui.contentdetailvideo.ContentDetailsActivity
import com.jetsynthesys.rightlife.ui.contentdetailvideo.NewSeriesDetailsActivity

class ContentDetailsAdapter(
    private val context: Context,
    private val contentDetails: List<ContentDetails>
) : RecyclerView.Adapter<ContentDetailsAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: RowJumpBackInBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ContentDetails) {
            // Load image using Glide
            Glide.with(binding.root.context)
                .load(ApiClient.CDN_URL_QA + (item.thumbnail?.url ?: ""))
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(binding.itemImage)

            binding.itemText.text = item.contentType ?: "Untitled"
            binding.tvTitle.text = item.title
            binding.tvDurationLeft.text = item.leftDuration

            binding.imgIconview.setImageResource(
                if ("VIDEO".equals(item.contentType, ignoreCase = true))
                    R.drawable.video_jump_back_in
                else if ("AUDIO".equals(item.contentType, ignoreCase = true))
                    R.drawable.audio_jump_back_in
                else if ("TEXT".equals(item.contentType, ignoreCase = true))
                    R.drawable.text_jump_back_in
                else
                    R.drawable.series_jump_back_in
            )

            // Calculate progress
            if ("SERIES".equals(item.contentType, ignoreCase = true)) {
                val progress = item.leftDuration?.let { calculateProgress(it) } ?: 0
                binding.progressBar.progress = progress
            } else {

                val duration = item.meta?.duration ?: 0 // total duration in seconds
                val left = item.leftDurationINT ?: 0
                val progress = if (duration > 0) {
                    val completed = (duration - left).coerceAtLeast(0)
                    ((completed.toFloat() / duration) * 100).toInt().coerceIn(0, 100)
                } else 0

                binding.progressBar.progress = progress
            }


            // Click listener
            binding.root.setOnClickListener {
                if (item.contentType.equals("TEXT", ignoreCase = true)) {
                    context.startActivity(
                        Intent(
                            context,
                            ArticlesDetailActivity::class.java
                        ).apply {
                            putExtra("contentId", item.id)
                        })
                } else if (item.contentType.equals("VIDEO", ignoreCase = true) || item.contentType
                        .equals("AUDIO", ignoreCase = true)
                ) {
                    context.startActivity(
                        Intent(
                            context,
                            ContentDetailsActivity::class.java
                        ).apply {
                            putExtra("contentId", item.id)
                            putExtra("PROGRESS", item.leftDurationINT)
                        })
                } else if (item.contentType.equals("SERIES", ignoreCase = true)) {
                    context.startActivity(
                        Intent(
                            context,
                            NewSeriesDetailsActivity::class.java
                        ).apply {
                            putExtra("seriesId", item.episodeDetails?.contentId)
                            putExtra("episodeId", item.episodeDetails?.id)
                        })
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowJumpBackInBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(contentDetails[position])
    }

    override fun getItemCount(): Int = contentDetails.size

    private fun calculateProgress(value: String): Int {
        return try {
            val parts = value.split("/")
            if (parts.size == 2) {
                val completed = parts[0].trim().toFloatOrNull() ?: 0f
                val total = parts[1].trim().toFloatOrNull() ?: 0f
                if (total > 0) {
                    ((completed / total) * 100).toInt().coerceIn(0, 100)
                } else 0
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }


}
