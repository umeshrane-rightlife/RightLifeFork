package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.R.color.transparent
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.jetsynthesys.rightlife.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class YourCaloricSummaryInfoBottomSheet : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_meal_summary_info_bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dialog = BottomSheetDialog(requireContext(), R.style.LoggedBottomSheetDialogTheme)
        dialog.setContentView(R.layout.fragment_frequently_logged)
        dialog.window?.setBackgroundDrawableResource(transparent)
        val title = view.findViewById<TextView>(R.id.title)
        val close = view.findViewById<ImageView>(R.id.close)
        val summary = view.findViewById<TextView>(R.id.tvMealSummary)
        val image = view.findViewById<ImageView>(R.id.image)

        image.setImageResource(R.drawable.calorie_balance)
        title.text = "Your Caloric Summary"

        close.setOnClickListener {
            dismiss()
        }
        val htmlContent = """
        <h2>What is Your Caloric Balance?</h2>
        <p><b>Your body is constantly using energy</b>—whether you're working out, walking, or even resting. Your <b>Caloric Summary</b> breaks this process down into three key numbers:</p>

<ul>
  <li><b>Calories In</b> → The energy from the food and drinks you consume.</li>
  <li><b>Calories Out</b> → The energy your body burns throughout the day.</li>
  <li><b>Caloric Balance</b> → The difference between the two, which tells you if you're in a deficit (burning more) or surplus (consuming more).</li>
</ul>

<h3>Calories In: What You Eat and Drink</h3>
<p>Everything you eat and drink contains calories, which provide the energy your body needs to function.</p>
<p><b>Where do Calories In come from?</b></p>
<ul>
  <li><b>Meals & Snacks</b> → Every bite of food contributes to your calorie intake.</li>
  <li><b>Drinks</b> → Some drinks (like sodas, juices, and alcohol) have calories, while others (like water, black coffee, and tea) don’t.</li>
  <li><b>Supplements & Shakes</b> → Protein shakes, meal replacements, and supplements also add to your intake.</li>
</ul>

<p>Keeping an accurate log of what you consume helps ensure your Calories In count reflects your actual intake. While calorie tracking is important, it’s not just about how many calories you consume, but <b>where those calories come from</b>.</p>
<p>That’s where logging your food on <b>RightLife</b> comes in handy. We use gold-standard nutritional databases, AI-powered meal logging, and precision metabolic tracking to ensure your calorie intake and nutritional source align with your weight goal.</p>

<h3>Calories OUT: The Energy You Burn</h3>
<p>Your body constantly burns calories—even when you're not moving! The <b>Calories Out</b> number includes three main types of calorie burn:</p>
<ul>
  <li><b>Basal Metabolic Rate (BMR)</b> → The calories your body burns just to stay alive (breathing, digesting, thinking, maintaining body temperature, etc.). This makes up about 60-70% of your total daily energy burn.</li>
  <li><b>Thermic Effect of Food (TEF)</b> → The energy required to digest, absorb, and process the food you eat. Generally, this accounts for about 10% of your total calories burned daily (higher for protein-rich foods, lower for fats).</li>
  <li><b>Active Burn (Activity Calories)</b> → This includes everything you do beyond basic survival:
    <ul>
      <li><b>Workouts</b> → Running, weightlifting, cycling, yoga, etc.</li>
      <li><b>Daily Movement</b> → Walking, climbing stairs, carrying groceries.</li>
      <li><b>Non-Exercise Activity (NEAT)</b> → Fidgeting, standing, cleaning.</li>
    </ul>
  </li>
</ul>

<h3>What Do the Numbers Mean?</h3>
<ul>
  <li><b>Deficit</b> → You're burning more than you're consuming. This is ideal for weight loss.</li>
  <li><b>Surplus</b> → You're consuming more than you're burning. Useful for muscle gain or maintaining high energy levels.</li>
</ul>

<h3>How to Use the Caloric Summary in RightLife</h3>
<p>RightLife simplifies calorie tracking by doing the heavy lifting for you. Unlike other apps that rely on static numbers, we dynamically adjust your caloric needs based on real-time data. We use a custom metabolic model based on the <b>Cunningham 1991 equation</b>, also known as the <b>Katch-McArdle equation</b>.</p>

<h3>What RightLife Does for You</h3>
<ul>
  <li><b>Adapts to Changes in Your BMR</b> → Your BMR isn’t fixed. As you gain or lose weight, RightLife recalculates your BMR to reflect your body’s evolving energy needs.</li>
  <li><b>Updates Your Activity Factor</b> → We continuously adjust your daily calorie burn based on logged activity and overall movement—ensuring an accurate picture of energy expenditure.</li>
  <li><b>Tracks Your Calories In & Out</b> → RightLife logs your food intake and estimates your calorie burn, including resting metabolism, digestion, and activity.</li>
</ul>

<h3>What You Need to Do</h3>
<ul>
  <li> <b>Log Your Meals</b> → Enter your food intake to keep track of Calories In. RightLife does the math for you.</li>
  <li> <b>Record Your Activity or Sync Your Wearable</b> → Workouts, steps, and movement all count. RightLife updates Calories Out to reflect your energy burn.</li>
  <li> <b>Check Your Balance</b> → Know if you’re in a deficit, surplus, or maintenance. We’ll make the adjustments for you.</li>
</ul>

<p><b>By regularly checking your Caloric Summary</b>, you can make better decisions about your nutrition, workouts, and daily habits—helping you achieve your fitness and health goals more effectively! </p>
    """.trimIndent()
        summary.text = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY)
    }

    companion object {
        const val TAG = "LoggedBottomSheet"
        @JvmStatic
        fun newInstance() = YourCaloricSummaryInfoBottomSheet().apply {
            arguments = Bundle().apply {
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
    }
}

