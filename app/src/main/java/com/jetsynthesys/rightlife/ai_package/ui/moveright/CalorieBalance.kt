package com.jetsynthesys.rightlife.ai_package.ui.moveright


import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.response.CalorieAnalysisResponse
import com.jetsynthesys.rightlife.ai_package.model.response.CalorieData
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment.RestorativeSleepFragment
import com.jetsynthesys.rightlife.databinding.FragmentCalorieBalanceBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.max
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

class CalorieBalance : BaseFragment<FragmentCalorieBalanceBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCalorieBalanceBinding
        get() = FragmentCalorieBalanceBinding::inflate

    private lateinit var barChart: BarChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var backwardImage: ImageView
    private lateinit var forwardImage: ImageView
    private lateinit var dottedLineView : DottedLineView
    private var selectedWeekDate: String = ""
    private var selectedMonthDate: String = ""
    private var selectedHalfYearlyDate: String = ""
    private lateinit var selectedDate: TextView
    private lateinit var selectedItemDate: TextView
    private lateinit var dottedLine: View
    private lateinit var selectHeartRateLayout: CardView
    private lateinit var selectedCalorieTv: TextView
    private lateinit var averageBurnCalorie: TextView
    private lateinit var averageHeading: TextView
    private lateinit var percentageTv: TextView
    private lateinit var percentageIc: ImageView
    private lateinit var back_button_calorie_balance: ImageView
    private lateinit var layoutLineChart: FrameLayout
    private lateinit var stripsContainer: FrameLayout
    private lateinit var heartRateDescriptionHeading : TextView
    private lateinit var heartRateDescription : TextView
    private lateinit var lineChart: LineChart
    private var calorieBalanceGoal : String = ""
    private var calorieBalanceBurnTarget : Double = 0.0
    private var loadingOverlay : FrameLayout? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundResource(R.drawable.gradient_color_background_workout)
        barChart = view.findViewById(R.id.heartRateChart)
        dottedLineView = view.findViewById(R.id.dottedLineView)
        radioGroup = view.findViewById(R.id.tabGroup)
        dottedLine = view.findViewById(R.id.dottedLineView1)
        backwardImage = view.findViewById(R.id.backwardImage)
        forwardImage = view.findViewById(R.id.forwardImage)
        selectedDate = view.findViewById(R.id.selectedDate)
        selectedItemDate = view.findViewById(R.id.selectedItemDate)
        selectHeartRateLayout = view.findViewById(R.id.selectHeartRateLayout)
        selectedCalorieTv = view.findViewById(R.id.selectedCalorieTv)
        percentageTv = view.findViewById(R.id.percentageTv)
        averageBurnCalorie = view.findViewById(R.id.averageBurnCalorie)
        averageHeading = view.findViewById(R.id.averageHeading)
        percentageIc = view.findViewById(R.id.percentageIc)
        layoutLineChart = view.findViewById(R.id.lyt_line_chart)
        stripsContainer = view.findViewById(R.id.stripsContainer)
        lineChart = view.findViewById(R.id.heartLineChart)
        heartRateDescriptionHeading = view.findViewById(R.id.heartRateDescriptionHeading)
        heartRateDescription = view.findViewById(R.id.heartRateDescription)
        back_button_calorie_balance = view.findViewById(R.id.back_button_calorie_balance)

        back_button_calorie_balance.setOnClickListener {
            val fragment = HomeBottomTabFragment()
            val args = Bundle().apply {
                putString("ModuleName", "MoveRight")
            }
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                addToBackStack(null)
                commit()
            }
        }

        radioGroup.check(R.id.rbWeek)
        fetchActiveCalories("last_weekly")
        setupLineChart()
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until group.childCount) {
                val radioButton = group.getChildAt(i) as RadioButton
                if (radioButton.id == checkedId) {
                    radioButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                } else {
                    radioButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                }
            }

            when (checkedId) {
                R.id.rbWeek -> fetchActiveCalories("last_weekly")
                R.id.rbMonth -> fetchActiveCalories("last_monthly")
                R.id.rbSixMonths -> fetchActiveCalories("last_six_months")
            }
        }

        backwardImage.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            var selectedTab: String = "Week"
            if (selectedId != -1) {
                val selectedRadioButton = view.findViewById<RadioButton>(selectedId)
                selectedTab = selectedRadioButton.text.toString()
            }

            if (selectedTab.contentEquals("Week")) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                val dateString = selectedWeekDate
                val date = dateFormat.parse(dateString)
                calendar.time = date!!
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                calendar.set(year, month, day)
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val dateStr = dateFormat.format(calendar.time)
                selectedWeekDate = dateStr
                fetchActiveCalories("last_weekly")
            } else if (selectedTab.contentEquals("Month")) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                val dateString = selectedMonthDate
                val date = dateFormat.parse(dateString)
                calendar.time = date!!
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                calendar.set(year, month, day)
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val dateStr = dateFormat.format(calendar.time)
                // val firstDateOfMonth = getFirstDateOfMonth(dateStr, 1)
                selectedMonthDate = dateStr
                fetchActiveCalories("last_monthly")
            } else {
                selectedHalfYearlyDate = ""
                fetchActiveCalories("last_six_months")
            }
        }

        forwardImage.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            var selectedTab: String = "Week"
            if (selectedId != -1) {
                val selectedRadioButton = view.findViewById<RadioButton>(selectedId)
                selectedTab = selectedRadioButton.text.toString()
            }
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val currentDate: String = currentDateTime.format(formatter)

            if (selectedTab.contentEquals("Week")) {
                if (!selectedWeekDate.contentEquals(currentDate)) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val dateString = selectedWeekDate
                    val date = dateFormat.parse(dateString)
                    calendar.time = date!!
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    calendar.set(year, month, day)
                    calendar.add(Calendar.DAY_OF_YEAR, +7)
                    val dateStr = dateFormat.format(calendar.time)
                    selectedWeekDate = dateStr
                    fetchActiveCalories("last_weekly")
                } else {
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                }
            } else if (selectedTab.contentEquals("Month")) {
                if (!selectedMonthDate.contentEquals(currentDate)) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val dateString = selectedMonthDate
                    val date = dateFormat.parse(dateString)
                    calendar.time = date!!
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    calendar.set(year, month, day)
                    calendar.add(Calendar.DAY_OF_YEAR, +30)
                    val dateStr = dateFormat.format(calendar.time)
                    //  val firstDateOfMonth = getFirstDateOfMonth(dateStr, 1)
                    selectedMonthDate = dateStr
                    fetchActiveCalories("last_monthly")
                } else {
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (!selectedHalfYearlyDate.contentEquals(currentDate)){
                    selectedHalfYearlyDate = ""
                    fetchActiveCalories("last_six_months")
                } else {
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = HomeBottomTabFragment()
            val args = Bundle().apply {
                putString("ModuleName", "MoveRight")
            }
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                addToBackStack(null)
                commit()
            }
        }
    }


    /** Update BarChart with new data */
    private fun updateChart(entries: List<BarEntry>, labels: List<String>, labelsDate: List<String>) {
        val dataSet = BarDataSet(entries, "")
        val colors: ArrayList<Int> = ArrayList()
        entries.forEach { item ->
            if (item.y.toDouble() > 0) {
                colors.add(ContextCompat.getColor(requireContext(), R.color.red))
            } else {
                colors.add(ContextCompat.getColor(requireContext(), R.color.color_green))
            }
        }
        dataSet.colors = colors
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        dataSet.valueTextSize = 12f
        if (entries.size > 7) {
            dataSet.setDrawValues(false)
        } else {
            dataSet.setDrawValues(true)
        }

        dataSet.highLightAlpha = 0 // removes color overlay
        dataSet.barShadowColor = Color.TRANSPARENT
        dataSet.highLightColor = Color.TRANSPARENT
        val typeface = ResourcesCompat.getFont(requireContext(), R.font.dmsans_bold)
        dataSet.valueTypeface = typeface

        val barData = BarData(dataSet)
        barData.barWidth = 0.4f
        barChart.data = barData
        barChart.setFitBars(true)
        // Enable highlighting for vertical line
        barChart.setScaleEnabled(false)
        barChart.isDoubleTapToZoomEnabled = false
        barChart.isHighlightPerTapEnabled = true
        barChart.isHighlightPerDragEnabled = false

        // Dynamically set Y-axis range to handle positive and negative values
        val leftYAxis: YAxis = barChart.axisLeft
        leftYAxis.textSize = 12f
        leftYAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        leftYAxis.setDrawGridLines(true)
        // Calculate min and max values from entries
        val minValue = entries.minOfOrNull { it.y } ?: 0f
        val maxValue = entries.maxOfOrNull { it.y } ?: 0f
        // Set axis range with some padding
        leftYAxis.axisMinimum = if (minValue < 0) minValue * 1.2f else 0f
        leftYAxis.axisMaximum = if (maxValue > 0) maxValue * 1.2f else 1000f
        leftYAxis.setDrawZeroLine(true) // Show zero line for clarity
        leftYAxis.zeroLineColor = Color.BLACK
        leftYAxis.zeroLineWidth = 1f
        // Custom formatter for Y-axis labels (e.g., 2000 -> 2k, -1000 -> -1k)
        leftYAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return when {
                    value == 0f -> "0"
                    else -> {
                        val kiloValue = (value / 1000f).toInt()
                        if (kiloValue != 0) "${kiloValue}k" else ""
                    }
                }
            }
        }

        // X-axis with multiline labels
        val combinedLabels: List<String> = if (entries.size == 30) {
            labels
        } else {
            labels.take(entries.size).zip(labelsDate.take(entries.size)) { label, date ->
                val cleanedDate = date.substringBefore(",")
                "$label\n$cleanedDate"
            }
        }

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(combinedLabels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 10f
        xAxis.granularity = 1f
        xAxis.labelCount = entries.size
        xAxis.setDrawLabels(true)
        xAxis.labelRotationAngle = 0f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        xAxis.yOffset = 15f // Adjust if needed, but keeping as is

        if (entries.size == 30) {
            xAxis.axisMinimum = -0.5f
            xAxis.axisMaximum = 29.5f
            xAxis.setCenterAxisLabels(false)
        } else {
            xAxis.axisMinimum = -0.5f
            xAxis.axisMaximum = entries.size - 0.5f
            xAxis.setCenterAxisLabels(false)
        }

        // Custom XAxisRenderer for multiline labels
        val customRenderer = RestorativeSleepFragment.MultilineXAxisRenderer(
            barChart.viewPortHandler,
            barChart.xAxis,
            barChart.getTransformer(YAxis.AxisDependency.LEFT)
        )
        (barChart as BarLineChartBase<*>).setXAxisRenderer(customRenderer)

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        //barChart.isHighlightPerTapEnabled = false

        barChart.setExtraOffsets(0f, 0f, 0f, 40f) // Increased bottom offset to prevent cutting
        val legend = barChart.legend
        legend.isEnabled = false
        legend.form = Legend.LegendForm.SQUARE  // Keep color box
        legend.textColor = Color.TRANSPARENT    // Hide text
        legend.textSize = 0f
        legend.setDrawInside(false)
        // Add custom view for dotted line
        val dottedLineView = View(requireContext()).apply {
            setBackgroundColor(Color.TRANSPARENT)
            visibility = View.GONE
        }
        // Add dotted line view as overlay (you might need to add this to parent layout)
        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                selectHeartRateLayout.visibility = View.VISIBLE
                dottedLineView.visibility = View.VISIBLE
                if (e != null && h != null) {
                    val x = e.x.toInt()
                    val y = e.y
                    Log.d("ChartClick", "Clicked X: $x, Y: $y")
                    if (x >= 0 && x < labelsDate.size) { // Boundary check
                        selectedItemDate.text = labelsDate[x]
                        selectedCalorieTv.text = y.toInt().toString()
                        val pos = FloatArray(2)
                        pos[0] = h.x
                        pos[1] = 0f

                        // Convert chart value to pixel position
                        barChart.getTransformer(YAxis.AxisDependency.LEFT).pointValuesToPixel(pos)

                        // Ensure chart is drawn before setting position
                        barChart.post {
                            dottedLine.visibility = View.VISIBLE

                            // Use translationX to move the line smoothly
                            dottedLine.translationX = pos[0]
                           // selectHeartRateLayout.translationX = pos[0] - 450
                        }
                        val barX = h.xPx
                        val chartWidth = barChart.width.toFloat()
                        val cardWidth = selectHeartRateLayout.width.toFloat()
                        val targetX = barX - (cardWidth / 2f)

                        val margin = 10 * Resources.getSystem().displayMetrics.density
                        val clampedX = max(margin, min(targetX, chartWidth - cardWidth - margin))

                        selectHeartRateLayout.x = clampedX

                    } else {
                        Log.e("ChartClick", "Index $x out of bounds for labelsDate size ${labelsDate.size}")
                        selectHeartRateLayout.visibility = View.GONE
                    }
                }
            }
            override fun onNothingSelected() {
                Log.d("ChartClick", "Nothing selected")
                selectHeartRateLayout.visibility = View.GONE
                removeVerticalDottedLine()
                dottedLine.visibility = View.GONE
            }
        })
        barChart.animateY(1000)
        barChart.invalidate()
    }

    private fun removeVerticalDottedLine() {
        val chartParent = barChart.parent as? ViewGroup
        val overlay = chartParent?.findViewWithTag<View>("dotted_line_overlay")
        overlay?.let { chartParent.removeView(it) }
    }
    /** Sample Data for Week */
    private fun getWeekData(): List<BarEntry> {
        return listOf(
            BarEntry(0f, 200f), BarEntry(1f, 350f), BarEntry(2f, 270f),
            BarEntry(3f, 400f), BarEntry(4f, 320f), BarEntry(5f, 500f), BarEntry(6f, 450f)
        )
    }

    private fun getWeekLabels(): List<String> {
        return listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    private fun getWeekLabelsDate(): List<String> {
        return listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    /** Fetch and update chart with API data */
    private fun fetchActiveCalories(period: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        showLoader(requireView())
                    }
                }
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                var selectedDate: String

                if (period.contentEquals("last_weekly")) {
                    if (selectedWeekDate.contentEquals("")) {
                        selectedDate = currentDateTime.format(formatter)
                        selectedWeekDate = selectedDate
                    } else {
                        selectedDate = selectedWeekDate
                    }
                    setSelectedDate(selectedWeekDate)
                } else if (period.contentEquals("last_monthly")) {
                    if (selectedMonthDate.contentEquals("")) {
                        selectedDate = currentDateTime.format(formatter)

                        selectedMonthDate = selectedDate
                    } else {
                        selectedDate = selectedMonthDate
                    }
                    setSelectedDateMonth(selectedMonthDate, "Month")
                } else {
                    if (selectedHalfYearlyDate.contentEquals("")) {
                        selectedDate = currentDateTime.format(formatter)
                        selectedHalfYearlyDate = selectedDate
                    } else {
                        selectedDate = selectedHalfYearlyDate
                    }
                    setSelectedDateMonth(selectedHalfYearlyDate, "Year")
                }

                val response = ApiClient.apiServiceFastApi.getCalorieAnalysis(
                    userId = userId, period = period, date = selectedDate
                )
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val calorieAnalysisResponse = response.body()
                    if (calorieAnalysisResponse?.status_code == 200) {
                        calorieAnalysisResponse.let { data ->
                            val (entries, labels, labelsDate) = when (period) {
                                "last_weekly" -> processWeeklyData(data, selectedDate)
                                "last_monthly" -> processMonthlyData(data, selectedDate)
                                "last_six_months" -> processSixMonthsData(data, selectedDate)
                                else -> Triple(getWeekData(), getWeekLabels(), getWeekLabelsDate())
                            }
                            withContext(Dispatchers.Main) {
                                if (period == "last_six_months") {
                                    barChart.visibility = View.GONE
                                    layoutLineChart.visibility = View.VISIBLE
                                    lineChartForSixMonths(data.data.calorie_data)
                                } else {
                                    barChart.visibility = View.VISIBLE
                                    layoutLineChart.visibility = View.GONE
                                    selectHeartRateLayout.visibility = View.GONE
                                    dottedLine.visibility = View.GONE
                                    updateChart(entries, labels, labelsDate)
                                }
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error: ${response.code()} - ${response.message()}", Toast.LENGTH_SHORT).show()
                        if (isAdded  && view != null){
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }
        }
    }

    /** Process API data for last_weekly (7 days) */
    private fun processWeeklyData(calorieAnalysisResponse: CalorieAnalysisResponse, currentDate: String): Triple<List<BarEntry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dateString = currentDate
        val date = dateFormat.parse(dateString)
        calendar.time = date!!
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(year, month, day)
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val calorieMap = mutableMapOf<String, Float>()
        val labels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        // Initialize 7 days with 0 calories
        repeat(7) {
            val dateStr = dateFormat.format(calendar.time)
            calorieMap[dateStr] = 0f
            val dateLabel = (convertDate(dateStr) + "," + year)
            val dayString = dayFormat.format(calendar.time)
            labels.add(dayString)
            labelsDate.add(dateLabel)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        calorieBalanceGoal = calorieAnalysisResponse.data.goal
        calorieBalanceBurnTarget = calorieAnalysisResponse.data.tdee

        // Aggregate calories by day
        calorieAnalysisResponse.data.calorie_data.forEach { calorie ->
            val dayKey = calorie.date
            calorieMap[dayKey] = calorieMap[dayKey]?.plus(calorie.calorie_balance) ?: calorie.calorie_balance
        }

        setLastAverageValue(calorieAnalysisResponse, "% Past Week")
        val entries = calorieMap.entries.mapIndexed { index, entry -> BarEntry(index.toFloat(), entry.value) }
        return Triple(entries, labels, labelsDate)
    }

    /** Process API data for last_monthly (5 weeks) */
    private fun processMonthlyData(calorieAnalysisResponse: CalorieAnalysisResponse, currentDate: String): Triple<List<BarEntry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dateString = currentDate
        val date = dateFormat.parse(dateString)
        calendar.time = date!!
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(year, month, day)
        calendar.add(Calendar.DAY_OF_YEAR, -29)
        val calorieMap = mutableMapOf<String, Float>()
        val weeklyLabels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()
        val dateList = mutableListOf<String>()

        val days = getDaysInMonth(month + 1, year)
        repeat(30) {
            val dateStr = dateFormat.format(calendar.time)
            calorieMap[dateStr] = 0f
            dateList.add(dateStr)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        val labelsWithEmpty = generateLabeled30DayListWithEmpty(dateList[0])
        val labels = formatDateList(dateList)
        weeklyLabels.addAll(labelsWithEmpty)
        labelsDate.addAll(labels)

        // Aggregate calories by day
        calorieAnalysisResponse.data.calorie_data.forEach { calorie ->
            val dayKey = calorie.date
            calorieMap[dayKey] = calorieMap[dayKey]?.plus(calorie.calorie_balance) ?: calorie.calorie_balance
        }

        setLastAverageValue(calorieAnalysisResponse, "% Past Month")
        val entries = calorieMap.entries.mapIndexed { index, entry -> BarEntry(index.toFloat(), entry.value) }
        return Triple(entries, weeklyLabels, labelsDate)
    }

    private fun formatDateList(dates: List<String>): List<String> {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM, yyyy", Locale.getDefault())
        return dates.mapNotNull { dateStr ->
            try {
                val parsedDate = inputFormat.parse(dateStr)
                parsedDate?.let { outputFormat.format(it) }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun generateLabeled30DayListWithEmpty(startDateStr: String): List<String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        val startDate = dateFormat.parse(startDateStr)!!
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        val endDate = Calendar.getInstance().apply {
            time = startDate
            add(Calendar.DAY_OF_MONTH, 29) // total 30 days
        }.time

        val fullList = MutableList(30) { "" } // default 30 items with empty strings
        var labelIndex = 0
        var startIndex = 0

        while (calendar.time <= endDate && startIndex < 30) {
            val weekStart = calendar.time
            calendar.add(Calendar.DAY_OF_MONTH, 6)
            val weekEnd = if (calendar.time.after(endDate)) endDate else calendar.time
            val startDay = dayFormat.format(weekStart)
            val endDay = dayFormat.format(weekEnd)
            val startMonth = monthFormat.format(weekStart)
            val endMonth = monthFormat.format(weekEnd)
            val newLine = "\n"
            val label = if (startMonth == endMonth) {
                "$startDay–$endDay$newLine$startMonth"
            } else {
                "$startDay$startMonth–$endDay$newLine$endMonth"
            }
            fullList[startIndex] = label // set label at start of week
            // Move to next start index
            startIndex += 7
            calendar.add(Calendar.DAY_OF_MONTH, 1) // move past last week end
        }
        return fullList
    }

    /** Process API data for last_six_months (6 months) */
    private fun processSixMonthsData(calorieAnalysisResponse: CalorieAnalysisResponse, currentDate: String): Triple<List<BarEntry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dateString = currentDate
        val date = dateFormat.parse(dateString)
        calendar.time = date!!
        calendar.add(Calendar.MONTH, -5) // Start 6 months back

        val calorieMap = mutableMapOf<Int, Float>()
        val labels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()

        // Initialize 6 months
        repeat(6) { month ->
            calorieMap[month] = 0f
            labels.add(monthFormat.format(calendar.time))
            labelsDate.add(convertMonth(dateFormat.format(calendar.time)) + "," + calendar.get(Calendar.YEAR))
            calendar.add(Calendar.MONTH, 1)
        }

        // Aggregate calories by month
        calorieAnalysisResponse.data.calorie_data.forEach { calorie ->
            val calorieDate = dateFormat.parse(calorie.date)
            if (calorieDate != null) {
                calendar.time = calorieDate
                val calorieYearMonth = yearMonthFormat.format(calorieDate)
                calendar.time = dateFormat.parse(currentDate)
                calendar.add(Calendar.MONTH, -5) // Reset to start of 6-month period
                for (monthIndex in 0..5) {
                    val periodYearMonth = yearMonthFormat.format(calendar.time)
                    if (calorieYearMonth == periodYearMonth) {
                        calorieMap[monthIndex] = calorieMap[monthIndex]?.plus(calorie.calorie_balance) ?: calorie.calorie_balance
                    }
                    calendar.add(Calendar.MONTH, 1)
                }
            }
        }

        setLastAverageValue(calorieAnalysisResponse, "% Past 6 Months")
        val entries = calorieMap.entries.mapIndexed { index, entry -> BarEntry(index.toFloat(), entry.value) }
        return Triple(entries, labels, labelsDate)
    }

    private fun setSelectedDate(selectedWeekDate: String) {
        requireActivity().runOnUiThread {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val dateString = selectedWeekDate
            val date = dateFormat.parse(dateString)
            calendar.time = date!!
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, day)
            calendar.add(Calendar.DAY_OF_YEAR, -6)
            val dateStr = dateFormat.format(calendar.time)
            val dateView: String = convertDate(dateStr) + "-" + convertDate(selectedWeekDate) + "," + year
            selectedDate.text = dateView
            selectedDate.gravity = Gravity.CENTER
        }
    }

    private fun setSelectedDateMonth(selectedMonthDate: String, dateViewType: String) {
        activity?.runOnUiThread {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val dateString = selectedMonthDate
            val date = dateFormat.parse(dateString)
            calendar.time = date!!
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, day)
            calendar.add(Calendar.DAY_OF_YEAR, -29)
            val dateStr = dateFormat.format(calendar.time)
            if (dateViewType.contentEquals("Month")){
                val dateView : String = convertDate(dateStr.toString()) + "-" + convertDate(selectedMonthDate)+","+ year.toString()
                selectedDate.text = dateView
                selectedDate.gravity = Gravity.CENTER
            }else{
                selectedDate.text = year.toString()
                selectedDate.gravity = Gravity.CENTER
            }
        }
    }

    private fun setLastAverageValue(calorieAnalysisResponse: CalorieAnalysisResponse, type: String) {
        activity?.runOnUiThread {
            heartRateDescriptionHeading.text = calorieAnalysisResponse.data.messages.heading
            heartRateDescription.text = calorieAnalysisResponse.data.messages.message
            val averageCalories = calorieAnalysisResponse.data.calorie_data.map { it.calorie_balance }.average().toFloat()
            averageBurnCalorie.text = averageCalories.toInt().toString()
            val previousAverage = 0f // Replace with actual logic if API provides previous period data
            val percentage = if (previousAverage != 0f) ((averageCalories - previousAverage) / previousAverage * 100).toInt() else 0
            if (percentage > 0) {
                percentageTv.text = "+$percentage$type"
                percentageIc.setImageResource(R.drawable.ic_up)
            } else if (percentage < 0) {
                percentageTv.text = "$percentage$type"
                percentageIc.setImageResource(R.drawable.ic_down)
            } else {
                percentageTv.text = "0$type"
                percentageIc.setImageResource(R.drawable.ic_up)
            }
        }
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    private fun convertDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        return try {
            val date = inputFormat.parse(inputDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    private fun convertMonth(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM", Locale.getDefault())
        return try {
            val date = inputFormat.parse(inputDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    private fun getDaysInMonth(month: Int, year: Int): Int {
        val yearMonth = YearMonth.of(year, month)
        return yearMonth.lengthOfMonth()
    }

    private fun getFirstDateOfMonth(inputDate: String, value: Int): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val parsedDate = LocalDate.parse(inputDate, formatter)
        val firstDayOfMonth = parsedDate.withDayOfMonth(value)
        return firstDayOfMonth.format(formatter)
    }

    private fun setupLineChart() {
        lineChart.apply {
            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
                textColor = Color.BLACK
                textSize = 10f
                setLabelCount(6, true)
                setDrawAxisLine(true)
            }
            axisRight.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.BLACK
                textSize = 10f
                setDrawGridLines(true)
                setDrawLabels(true)
                granularity = 1f
                setAvoidFirstLastClipping(true)
            }
            description.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            setDrawBorders(false)
            setNoDataText("Loading data...")
        }
    }

    private fun lineChartForSixMonths(calorieData: List<CalorieData>) {
        val entries = calorieData.mapIndexed { index, calorie ->
            Entry(index.toFloat(), calorie.calorie_balance)
        }.ifEmpty { listOf(Entry(0f, 0f), Entry(1f, 0f)) }

        val dataSet = LineDataSet(entries, "Calorie Balance").apply {
            color = Color.rgb(255, 102, 128)
            lineWidth = 1f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.LINEAR
            fillAlpha = 20
            fillColor = Color.rgb(255, 102, 128)
            setDrawFilled(true)
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        val labels = calorieData.map { convertMonth(it.date) }.ifEmpty { listOf("No Data", "No Data") }
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        lineChart.xAxis.apply {
            labelCount = labels.size.coerceAtLeast(2)
        }

        lineChart.axisLeft.apply {
            axisMaximum = (calorieData.maxOfOrNull { it.calorie_balance }?.times(1.2f) ?: 1000f)
            valueFormatter = object : IndexAxisValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        lineChart.animateX(1000)
        lineChart.animateY(1000)
        lineChart.invalidate()

        lineChart.post {
            val transformer = lineChart.getTransformer(YAxis.AxisDependency.LEFT)
            val hMargin = 40f
            val vMargin = 20f
            stripsContainer.removeAllViews()
            calorieData.groupBy { convertMonth(it.date) }.forEach { (month, data) ->
                val startIndex = calorieData.indexOfFirst { convertMonth(it.date) == month }
                val endIndex = calorieData.indexOfLast { convertMonth(it.date) == month }
                if (startIndex >= 0 && endIndex >= 0) {
                    val xStartFraction = startIndex.toFloat() / calorieData.size
                    val xEndFraction = endIndex.toFloat() / calorieData.size
                    val avgCalories = data.map { it.calorie_balance }.average().toFloat()
                    val yFraction = avgCalories / (calorieData.maxOfOrNull { it.calorie_balance }?.toFloat() ?: 1f)
                    val pixelStart = transformer.getPixelForValues(
                        (xStartFraction * (lineChart.width - 2 * hMargin)) + hMargin,
                        ((1 - yFraction) * (lineChart.height - 2 * vMargin) + vMargin).toFloat()
                    )
                    val pixelEnd = transformer.getPixelForValues(
                        (xEndFraction * (lineChart.width - 2 * hMargin)) + hMargin,
                        ((1 - yFraction) * (lineChart.height - 2 * vMargin) + vMargin).toFloat()
                    )

                    // Draw capsule
                    val capsuleView = View(requireContext()).apply {
                        val width = (pixelEnd.x - pixelStart.x).toInt().coerceAtLeast(20)
                        layoutParams = FrameLayout.LayoutParams(width, 8)
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 4f
                            setColor(Color.RED)
                        }
                        x = pixelStart.x.toFloat()
                        y = (pixelStart.y - 3).toFloat()
                    }
                    stripsContainer.addView(capsuleView)

                    // Average label
                    val avgText = TextView(requireContext()).apply {
                        text = "${avgCalories.toInt()}"
                        setTextColor(Color.BLACK)
                        textSize = 12f
                        setPadding(6, 4, 6, 4)
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        x = (((pixelStart.x + pixelEnd.x) / 2 - 20).toFloat())
                        y = (pixelStart.y - 20).toFloat()
                    }
                    stripsContainer.addView(avgText)
                }
            }
        }
    }


}

class DottedLineView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        color = android.graphics.Color.GRAY
        strokeWidth = 4f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f) // Dotted effect
        isAntiAlias = true
    }

    var startX = 0f
    var startY = 0f
    var endX = 0f
    var endY = 0f

    fun setLineCoordinates(sx: Float, sy: Float, ex: Float, ey: Float) {
        startX = sx
        startY = sy
        endX = ex
        endY = ey
        invalidate() // Trigger a redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawLine(startX, startY, endX, endY, paint)
    }
}