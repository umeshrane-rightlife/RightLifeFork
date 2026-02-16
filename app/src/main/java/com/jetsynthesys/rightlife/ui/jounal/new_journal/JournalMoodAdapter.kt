package com.jetsynthesys.rightlife.ui.jounal.new_journal

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R

class JournalMoodAdapter(
    private val moods: List<Mood>,
    private var selectedPosition: Int = RecyclerView.NO_POSITION,
    private val onMoodSelected: (Mood) -> Unit
) : RecyclerView.Adapter<JournalMoodAdapter.MoodViewHolder>() {

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moodIcon: ImageView = itemView.findViewById(R.id.moodIcon)
        private val moodLabel: TextView = itemView.findViewById(R.id.moodLabel)
        private val iconWrapper: FrameLayout = itemView.findViewById(R.id.iconWrapper)

        fun bind(mood: Mood, isSelected: Boolean) {
            moodIcon.setImageResource(mood.iconResId)
            moodLabel.text = mood.name

            // 1. Immediate UI state updates
            iconWrapper.isSelected = isSelected
            moodLabel.setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)

            // 2. Scale Logic: If selected, zoom in; otherwise, reset to 1.0f
            val targetScale = if (isSelected) 1.25f else 1.0f

            iconWrapper.animate()
                .scaleX(targetScale)
                .scaleY(targetScale)
                .setDuration(200) // Smooth transition
                .start()

            itemView.setOnClickListener {
                if (selectedPosition == adapterPosition) return@setOnClickListener

                val previousPosition = selectedPosition
                selectedPosition = adapterPosition

                // Refresh only the items that changed for performance
                if (previousPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousPosition)
                }
                notifyItemChanged(selectedPosition)

                onMoodSelected(mood)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_journal_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moods[position], position == selectedPosition)
    }

    override fun getItemCount() = moods.size
}
