package com.jetsynthesys.rightlife.ui.affirmation.adapter

import ai.nuralogix.anurasdk.output.e.d.v
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.affirmation.pojo.AffirmationSelectedCategoryData
import com.zhpan.indicator.IndicatorView
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle

class AffirmationCardPagerAdapter(
    private val items: ArrayList<AffirmationSelectedCategoryData>,
    private val context: Context,
    val viewPager: ViewPager
) :
    PagerAdapter() {

    private val instantiatedViews = mutableMapOf<Int, View>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(R.layout.row_affirmation_card, container, false)

        val item = items[position]

        // Set up views
        val imageAffirmationCard: ImageView = view.findViewById(R.id.imageAffirmationCard)
        val cardViewAffirmation: CardView = view.findViewById(R.id.cardViewAffirmation)
        val tvAffirmationTitle: TextView = view.findViewById(R.id.tvAffirmationTitle)
        val tvAffirmationAuthor: TextView = view.findViewById(R.id.tvAffirmationAuthor)
        val pageCount: TextView = view.findViewById(R.id.pageCount)
        val indicatorView : IndicatorView = view.findViewById(R.id.indicator_view)

        // set card bg

        when (item.categoryName?.lowercase()) {
            "self-love" -> {
                imageAffirmationCard.setImageResource(R.drawable.ic_affirmationcard_selflove)
                cardViewAffirmation.setBackgroundResource(R.drawable.affirmationcard_bg_selflove)
                tvAffirmationAuthor.setTextColor(ContextCompat.getColor(context, R.color.checklist_text_color_selflove))
            }
            "manifestation" -> {
                imageAffirmationCard.setImageResource(R.drawable.ic_affirmationcard_manifestation)
                cardViewAffirmation.setBackgroundResource(R.drawable.affirmationcard_bg_manifestation)
                tvAffirmationAuthor.setTextColor(ContextCompat.getColor(context, R.color.checklist_text_color_manifestation))
            }
            "health" -> {
                imageAffirmationCard.setImageResource(R.drawable.ic_affirmationcard_health)
                cardViewAffirmation.setBackgroundResource(R.drawable.affirmationcard_bg_health)
                tvAffirmationAuthor.setTextColor(ContextCompat.getColor(context, R.color.checklist_text_color_health))
            }
            "abundance" -> {
                imageAffirmationCard.setImageResource(R.drawable.ic_affirmationcard_abundance)
                cardViewAffirmation.setBackgroundResource(R.drawable.affirmationcard_bg_abundance)
                tvAffirmationAuthor.setTextColor(ContextCompat.getColor(context, R.color.checklist_text_color_abundance))
            }
            "gratitude" -> {
                imageAffirmationCard.setImageResource(R.drawable.ic_affirmationcard_gratitude)
                cardViewAffirmation.setBackgroundResource(R.drawable.affirmationcard_bg_gratitude)
                tvAffirmationAuthor.setTextColor(ContextCompat.getColor(context, R.color.checklist_text_color_gratitude))
            }
            "relationships" -> {
                imageAffirmationCard.setImageResource(R.drawable.ic_affirmationcard_relationship)
                cardViewAffirmation.setBackgroundResource(R.drawable.affirmationcard_bg_relationship)
                tvAffirmationAuthor.setTextColor(ContextCompat.getColor(context, R.color.checklist_text_color_relationship))
            }
            "mindfulness" -> {
                imageAffirmationCard.setImageResource(R.drawable.ic_affirmationcard_mindfulness)
                cardViewAffirmation.setBackgroundResource(R.drawable.affirmationcard_bg_mindfulness)
                tvAffirmationAuthor.setTextColor(ContextCompat.getColor(context, R.color.checklist_text_color_mindfulness))
            }
            "stress relief" -> {
                imageAffirmationCard.setImageResource(R.drawable.ic_affirmationcard_stressrelief)
                cardViewAffirmation.setBackgroundResource(R.drawable.affirmationcard_bg_stressrelief)
                tvAffirmationAuthor.setTextColor(ContextCompat.getColor(context, R.color.checklist_text_color_stressrelief))
            }
            else -> {
                imageAffirmationCard.setImageResource(R.drawable.ic_affirmationcard_gratitude)
                cardViewAffirmation.setBackgroundResource(R.drawable.affirmationcard_bg_gratitude)
                tvAffirmationAuthor.setTextColor(ContextCompat.getColor(context, R.color.checklist_text_color_gratitude))
            }
        }



        //tvAffirmationTitle.text = "\"${item.title}\""

        // As per bug https://jetsynthesys.atlassian.net/browse/RAD-7265 removed inverted comma
        tvAffirmationTitle.text = item.title

        tvAffirmationAuthor.text = item.artist


        // Load image using Glide
        //Glide.with(context).load(item).into(image)

        indicatorView.setSlideMode(IndicatorSlideMode.NORMAL);
        indicatorView.setIndicatorStyle(IndicatorStyle.ROUND_RECT);

// Set slider sizes programmatically (supported in all releases)
        indicatorView.setSliderWidth(dp(6));
        indicatorView.setSliderHeight(dp(3));
        indicatorView.setSliderGap(dp(2));



        indicatorView.setupWithViewPager(viewPager = viewPager)
        pageCount.text = "${position + 1}/${items.size}"

        container.addView(view)
        instantiatedViews[position] = view
        return view
    }

    private fun dp(v: Int): Float
    {
        return (v * context.resources.displayMetrics.density).toInt().toFloat()
    }
    override fun getCount() = items.size

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
    fun getViewAt(position: Int): View? {
        return instantiatedViews[position]
    }

}