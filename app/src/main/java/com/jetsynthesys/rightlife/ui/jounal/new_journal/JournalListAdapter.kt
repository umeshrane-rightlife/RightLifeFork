package com.jetsynthesys.rightlife.ui.jounal.new_journal

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.RowJournalListBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class JournalListAdapter(
    private val items: MutableList<JournalEntry>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<JournalListAdapter.EntryViewHolder>() {

    inner class EntryViewHolder(val binding: RowJournalListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = RowJournalListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val entry = items[position]
        val ctx = holder.itemView.context
        with(holder.binding) {
            tvTitle.text = entry.answer
            tvDate.text = entry.createdAt?.let { formatDate(it) }
            textTime.text = entry.createdAt?.let { formatTime(it) }


            chip2.text = entry.title

        /*    if (entry.tags.isNullOrEmpty()){
                chip3.visibility = View.GONE
                chipMore.visibility = View.GONE
            }else {
                chip3.text = entry.tags?.get(0)
                chipMore.text = "${entry.tags?.size?.minus(1)}+"
            }*/
            if (entry.tags.isNullOrEmpty()){
                chip3.visibility = View.GONE
                chipMore.visibility = View.GONE
            }else {
                chip3.visibility = View.VISIBLE
                if (entry.tags?.size == 1)
                    chipMore.visibility = View.GONE
                else
                    chipMore.visibility = View.VISIBLE
                chip3.text = entry.tags?.get(0)
                chipMore.text = "+${entry.tags?.size?.minus(1)}"
            }

            if (entry.emotion.isNullOrEmpty())
                llEmotion.visibility = View.GONE
            else {
                llEmotion.visibility = View.VISIBLE
                chipEmotion.text = entry.emotion
                var colorStateList: ColorStateList? = null
                when (entry.emotion) {
                    "Happy" -> {
                        imageEmotion.setImageResource(R.drawable.ic_happy)
                        colorStateList =
                            ContextCompat.getColorStateList(ctx, R.color.happy_bg_color)
                    }

                    "Relaxed" -> {
                        imageEmotion.setImageResource(R.drawable.ic_relaxed)
                        colorStateList =
                            ContextCompat.getColorStateList(ctx, R.color.relaxed_bg_color)
                    }
                    "Unsure" -> {
                        imageEmotion.setImageResource(R.drawable.ic_unsure)
                        colorStateList =
                            ContextCompat.getColorStateList(ctx, R.color.unsure_bg_color)
                    }
                    "Stressed" -> {
                        imageEmotion.setImageResource(R.drawable.ic_stressed)
                        colorStateList =
                            ContextCompat.getColorStateList(ctx, R.color.stressed_bg_color)
                    }
                    "Sad" -> {
                        imageEmotion.setImageResource(R.drawable.ic_sad)
                        colorStateList =
                            ContextCompat.getColorStateList(ctx, R.color.sad_bg_color)
                    }
                    else -> {
                        colorStateList =
                            ContextCompat.getColorStateList(ctx, R.color.no_emotion_bg_color)
                    }
                }
                llJournalList.backgroundTintList = colorStateList
            }

            menuIcon.setOnClickListener {
                onItemClickListener.onMenuClick(entry, menuIcon)
            }

        }

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(entry)
        }
    }

    interface OnItemClickListener {
        fun onMenuClick(journalEntry: JournalEntry, view: View)
        fun onItemClick(journalEntry: JournalEntry)
    }

    override fun getItemCount(): Int = items.size

    fun deleteItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun formatDate(iso: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val outputFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
            val date = inputFormat.parse(iso)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun formatTime(iso: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(iso)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timeFormat.timeZone = TimeZone.getDefault()
            return date?.let { timeFormat.format(it) } ?: ""

        } catch (e: Exception) {
            ""
        }
    }

}
