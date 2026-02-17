package com.jetsynthesys.rightlife.ui.mindaudit

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.databinding.RowHelplineBinding

class HelpLineAdapter(
    private val context: Context,
    private val items: List<Organization>,
    private val isForState: Boolean = false
) : RecyclerView.Adapter<HelpLineAdapter.CountryViewHolder>() {

    class CountryViewHolder(val binding: RowHelplineBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val binding =
            RowHelplineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CountryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvTiming.text = item.hours
            tvOrganizationName.text = item.name
            if (!item.website.isNullOrEmpty())
                tvOrganizationName.paintFlags =
                    tvOrganizationName.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            tvOrganizationName.setOnClickListener {
                item.website?.let {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(it)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }
            }

            val phoneNumbers = item.hotline?.split(",")
            if (phoneNumbers.isNullOrEmpty()) {
                rlPhoneNumber1.visibility = View.GONE
                rlPhoneNumber2.visibility = View.GONE
            } else if (phoneNumbers.size == 1) {
                tvPhoneNumber1.text = item.hotline
                rlPhoneNumber2.visibility = View.GONE
            } else {
                tvPhoneNumber1.text = phoneNumbers[0]
                tvPhoneNumber2.text = phoneNumbers[1]
            }

            if (isForState) {
                val colorHex = if (position % 2 == 0) "#FFA8A5" else "#FEB4B2"
                val color = Color.parseColor(colorHex)

// Use backgroundTintList to preserve your drawable shape/corners
                rlHelpLine.backgroundTintList = ColorStateList.valueOf(color)
            } else {
                val color = Color.parseColor("#FDD3D2")
                rlHelpLine.backgroundTintList = ColorStateList.valueOf(color)
            }
            rlPhoneNumber1.setOnClickListener {
                if (tvPhoneNumber1.text.isNullOrEmpty()) return@setOnClickListener
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${tvPhoneNumber1.text}")
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = items.size
}
