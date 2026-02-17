package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Guideline
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.*
import com.jetsynthesys.rightlife.ai_package.ui.adapter.CarouselAdapter
import com.jetsynthesys.rightlife.ai_package.ui.adapter.GridAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.jetsynthesys.rightlife.ai_package.ui.moveright.graphs.LineGrapghViewSteps
import com.jetsynthesys.rightlife.ai_package.ui.moveright.graphs.LineGraphView
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.adapter.RecommendedAdapterSleep
import com.jetsynthesys.rightlife.ai_package.ui.steps.SetYourStepGoalFragment
import com.jetsynthesys.rightlife.ai_package.utils.AppPreference
import com.jetsynthesys.rightlife.databinding.FragmentLandingBinding
import com.jetsynthesys.rightlife.databinding.FragmentSleepRightLandingBinding
import com.jetsynthesys.rightlife.ui.aireport.AIReportWebViewActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.reflect.KClass

class MoveRightLandingFragment : BaseFragment<FragmentLandingBinding>() {

    private lateinit var carouselViewPager: ViewPager2
    private lateinit var dotsLayout: LinearLayout
    private lateinit var nodataWorkout: ConstraintLayout
    private lateinit var horizontalStepsSection: ConstraintLayout
    private lateinit var stepsBottomSection: ConstraintLayout
    private lateinit var dataFilledworkout: ConstraintLayout
    private lateinit var calorie_no_data_filled_layout: ConstraintLayout
    private lateinit var calorie_layout_data_filled: ConstraintLayout
    private lateinit var dots: Array<ImageView?>
    private var totalCaloriesBurnedRecord: List<TotalCaloriesBurnedRecord>? = null
    private var activeCalorieBurnedRecord: List<ActiveCaloriesBurnedRecord>? = null
    private var stepsRecord: List<StepsRecord>? = null
    private var heartRateRecord: List<HeartRateRecord>? = null
    private var heartRateVariability: List<HeartRateVariabilityRmssdRecord>? = null
    private var restingHeartRecord: List<RestingHeartRateRecord>? = null
    private var basalMetabolicRateRecord: List<BasalMetabolicRateRecord>? = null
    private var bloodPressureRecord: List<BloodPressureRecord>? = null
    private var sleepSessionRecord: List<SleepSessionRecord>? = null
    private var exerciseSessionRecord: List<ExerciseSessionRecord>? = null
    private var weightRecord: List<WeightRecord>? = null
    private var distanceRecord: List<DistanceRecord>? = null
    private var bodyFatRecord: List<BodyFatRecord>? = null
    private var oxygenSaturationRecord: List<OxygenSaturationRecord>? = null
    private var respiratoryRateRecord: List<RespiratoryRateRecord>? = null
    private lateinit var healthConnectClient: HealthConnectClient
    private lateinit var tvBurnValue: TextView
    private lateinit var text_activity: TextView
    private lateinit var weightLossZoneText: TextView
    private lateinit var lightZoneBelow: TextView
    private lateinit var lightZoneHighl: TextView
    private lateinit var fatLossHighl: TextView
    private lateinit var cardioHighl: TextView
    private lateinit var heartRateZoneNoDataTv : TextView
    private lateinit var text_no_data_activity_factor: TextView
    private lateinit var peakHighl: TextView
    private lateinit var line_graph: LineGraphView
    private lateinit var calorieBalanceIcon: ImageView
    private lateinit var calorie_balance_icon_no_data: ImageView
    private lateinit var step_forward_icon: ImageView
    private lateinit var sync_with_icon: ImageView
    private lateinit var moveRightImageBack: ImageView
    private lateinit var stepLineGraphView: LineGrapghViewSteps
    private lateinit var todayStepsTv: TextView
    private lateinit var today_steps_count: TextView
    private lateinit var averageStepsTv: TextView
    private lateinit var stepHeading : TextView
    private lateinit var yesterday_steps_count: TextView
    private lateinit var steps_no_data_text: TextView
    private lateinit var stes_no_data_text_description: TextView
    private lateinit var syncWithHealthConnectButton: ConstraintLayout
    private lateinit var goalStepsTv: TextView
    private lateinit var calorieCountText: TextView
    private lateinit var totalIntakeCalorieText: TextView
    private lateinit var calorieBalanceDescription: TextView
    private lateinit var calorieBalanceMessageTitle : TextView
    private lateinit var appPreference: AppPreference
    private lateinit var transparentOverlay : View
    private lateinit var circleIndicator : View
    private lateinit var workoutImageIcon : ImageView
    private lateinit var progressBarCalorieBalance : ProgressBar
    private lateinit var belowTransparent : ImageView
    private lateinit var progressBarLayout : ConstraintLayout
    private lateinit var stepWithDataCardLayout : ConstraintLayout
    private lateinit var stepNoDataLayout : ConstraintLayout
    private lateinit var verticalLineStartLightBpmTv : TextView
    private lateinit var verticalLineFatLossBpmTv : TextView
    private lateinit var verticalLineCardioBpmTv : TextView
    private lateinit var verticalLinePeakBpmTv : TextView
    private lateinit var verticalLinePeakEndBpmTv : TextView
    private lateinit var caloricInfo : ImageView
    private lateinit var viewWorkoutHistory : LinearLayoutCompat
    private lateinit var yourMovementSummary : ImageView
    private lateinit var overlayGuideline : Guideline
    private lateinit var yourVitals : ImageView
    private lateinit var yourHeartRateZone : ImageView
    private lateinit var recyclerView : RecyclerView
    private lateinit var adapter : GridAdapter
    private var totalIntakeCaloriesSum: Int = 0
    private var loadingOverlay : FrameLayout? = null
    private var isRepeat : Boolean = false
    private lateinit var swipeRefreshLayout : SwipeRefreshLayout
    private lateinit var nestedScrollView : NestedScrollView
    private lateinit var rightLifeReportCard : FrameLayout
    private lateinit var compactSyncIndicator : FrameLayout
    private lateinit var compactHeartIcon : ImageView
    private lateinit var compactRotatingArc : ProgressBar
    private var fullHeartAnimator: ObjectAnimator? = null
    var compactHeartAnimator: ObjectAnimator? = null
    private lateinit var recomendationRecyclerView: RecyclerView
    private lateinit var thinkRecomendedResponse : ThinkRecomendedResponse
    private lateinit var recomendationAdapter: RecommendedAdapterSleep
    private var isSyncData : Boolean = false

    private val allReadPermissions = setOf(
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(RespiratoryRateRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
    )

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLandingBinding
        get() = FragmentLandingBinding::inflate

    private var _binding: FragmentLandingBinding? = null
    private val binding get() = _binding!!

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        context?.let {
            appPreference = AppPreference(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSeatName = arguments?.getString("BottomSeatName").toString()
        carouselViewPager = view.findViewById(R.id.carouselViewPager)
        calorie_no_data_filled_layout = view.findViewById(R.id.calorie_no_data_filled_layout)
        calorie_layout_data_filled = view.findViewById(R.id.calorie_layout_data_filled)
        text_no_data_activity_factor = view.findViewById(R.id.text_no_data_activity_factor)
        line_graph = view.findViewById(R.id.line_graph)
        weightLossZoneText = view.findViewById(R.id.weightLossZoneText)
        lightZoneBelow = view.findViewById(R.id.lightZoneBelow)
        lightZoneHighl = view.findViewById(R.id.lightZoneHigh)
        fatLossHighl = view.findViewById(R.id.fatLossHigh)
        sync_with_icon = view.findViewById(R.id.sync_with_icon)
        cardioHighl = view.findViewById(R.id.cardioHigh)
        peakHighl = view.findViewById(R.id.peakHigh)
        text_activity = view.findViewById(R.id.text_activity)
        nodataWorkout = view.findViewById(R.id.no_data_workout_landing)
        horizontalStepsSection = view.findViewById(R.id.horizontalStepsSection)
        stepsBottomSection = view.findViewById(R.id.stepsBottomSection)
        dataFilledworkout = view.findViewById(R.id.data_filled_workout)
        step_forward_icon = view.findViewById(R.id.step_forward_icon)
        totalIntakeCalorieText = view.findViewById(R.id.textView1)
        calorieCountText = view.findViewById(R.id.calorie_count)
        recomendationRecyclerView = view.findViewById(R.id.recommendationRecyclerView)
        steps_no_data_text = view.findViewById(R.id.steps_no_data_text)
        stes_no_data_text_description = view.findViewById(R.id.stes_no_data_text_description)
        syncWithHealthConnectButton = view.findViewById(R.id.syncWithHealthConnectButton)
        calorieBalanceIcon = view.findViewById(R.id.calorie_balance_icon)
        calorie_balance_icon_no_data = view.findViewById(R.id.calorie_balance_icon_no_data)
        dotsLayout = view.findViewById(R.id.dotsLayout)
        moveRightImageBack = view.findViewById(R.id.moveright_image_back)
        calorieBalanceDescription = view.findViewById(R.id.calorieBalanceMessage)
        calorieBalanceMessageTitle = view.findViewById(R.id.calorieBalanceMessageTitle)
        tvBurnValue = view.findViewById(R.id.textViewBurnValue)
        stepLineGraphView = view.findViewById(R.id.line_graph_steps)
        todayStepsTv = view.findViewById(R.id.todayStepsTv)
        today_steps_count = view.findViewById(R.id.today_steps_count)
        stepHeading = view.findViewById(R.id.step_text_heading)
        averageStepsTv = view.findViewById(R.id.averageStepsTv)
        yesterday_steps_count = view.findViewById(R.id.yesterday_steps_count)
        goalStepsTv = view.findViewById(R.id.goal_tex)
        progressBarCalorieBalance = view.findViewById(R.id.progressBar)
        circleIndicator = view.findViewById(R.id.circleIndicator)
        transparentOverlay = view.findViewById(R.id.transparentOverlay)
        belowTransparent = view.findViewById(R.id.imageViewBelowOverlay)
        progressBarLayout = view.findViewById(R.id.progressBarLayout)
        workoutImageIcon = view.findViewById(R.id.workout_forward_icon)
        stepNoDataLayout = view.findViewById(R.id.stepNoDataLayout)
        stepWithDataCardLayout = view.findViewById(R.id.stepWithDataCardLayout)
        verticalLineStartLightBpmTv = view.findViewById(R.id.vertical_line_start_light_bgmtext)
        verticalLineFatLossBpmTv = view.findViewById(R.id.vertical_line_fatloss_button_bgmtext)
        verticalLineCardioBpmTv = view.findViewById(R.id.vertical_line_cardio_button_bgmtext)
        verticalLinePeakBpmTv = view.findViewById(R.id.vertical_line_peak_button_bgmtext)
        verticalLinePeakEndBpmTv = view.findViewById(R.id.vertical_line_peak_button_end_bgmtext)
        heartRateZoneNoDataTv = view.findViewById(R.id.heartRateZoneNoDataTv)
        caloricInfo = view.findViewById(R.id.caloricInfo)
        yourMovementSummary = view.findViewById(R.id.yourMovementSummary)
        yourVitals = view.findViewById(R.id.yourVitals)
        yourHeartRateZone = view.findViewById(R.id.yourHeartRateZone)
        viewWorkoutHistory = view.findViewById(R.id.viewWorkoutHistory)
        rightLifeReportCard = view.findViewById(R.id.rightLifeReportCard)
        overlayGuideline = view.findViewById(R.id.overlayGuideline)
        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val dottedLine = view.findViewById<View>(R.id.horizontal_dotted_green)

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        nestedScrollView = view.findViewById(R.id.nestedScrollView)
        compactSyncIndicator = view.findViewById(R.id.compactSyncIndicator)
        compactHeartIcon = view.findViewById(R.id.compactHeartIcon)
        compactRotatingArc = view.findViewById(R.id.compactRotatingArc)

        showCompactSyncView()

        swipeRefreshLayout.setOnRefreshListener {
            // Call your API or refresh function
            fetchDataFromApi()
        }

// Optional: Scroll to top to enable swipe-to-refresh again (when loading more at bottom)
        nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            swipeRefreshLayout.isEnabled = scrollY == 0
        }

        val layoutParams = dottedLine.layoutParams as ConstraintLayout.LayoutParams

        dottedLine.layoutParams = layoutParams
        val dottedRed = view.findViewById<View>(R.id.horizontal_dotted_red)
        val redLayoutParams = dottedRed.layoutParams as ConstraintLayout.LayoutParams
        redLayoutParams.width = if (screenWidthDp < 600) {
            resources.getDimensionPixelSize(R.dimen.dotted_line_width_small) // 50dp
        } else {
            resources.getDimensionPixelSize(R.dimen.dotted_line_width_large) // 56dp
        }
        dottedRed.layoutParams = redLayoutParams

        setupRecyclerView(view)
        fetchThinkRecomendedData()

        moveRightImageBack.setOnClickListener {
            activity?.finish()
        }
        step_forward_icon.setOnClickListener {
            navigateToFragment(StepFragment(), "StepTakenFragment")
        }
        averageStepsTv.setOnClickListener {
            navigateToFragment(SetYourStepGoalFragment(), "StepTakenFragment")
        }
        val activityFactorImageIcon = view.findViewById<ImageView>(R.id.activity_forward_icon)
        val logMealButton = view.findViewById<ConstraintLayout>(R.id.log_meal_button)
        val layoutAddWorkout = view.findViewById<ConstraintLayout>(R.id.lyt_snap_meal)
        val lytNoDataAddWorkoutBtn = view.findViewById<ConstraintLayout>(R.id.lytNoDataAddWorkoutBtn)
        val logMealNoDataBtn = view.findViewById<ConstraintLayout>(R.id.logMealNoDataBtn)

        calorieBalanceIcon.setOnClickListener {
            context?.let { it1 ->
                AnalyticsLogger.logEvent(
                    it1, AnalyticsEvent.MR_Report_PageOpen
                )
            }
            context?.let { it1 ->
                AnalyticsLogger.logEvent(
                    it1, AnalyticsEvent.MR_CalBalanceCard_Arrow_Tap
                )
            }
            val fragment = CalorieBalance()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment, fragment, "CalorieBalance")
                .addToBackStack(null)
                .commit()
        }
        calorie_balance_icon_no_data.setOnClickListener {
            context?.let { it1 ->
                AnalyticsLogger.logEvent(
                    it1, AnalyticsEvent.MR_Report_PageOpen
                )
            }
            context?.let { it1 ->
                AnalyticsLogger.logEvent(
                    it1, AnalyticsEvent.MR_CalBalanceCard_Arrow_Tap
                )
            }
            val fragment = CalorieBalance()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment, fragment, "CalorieBalance")
                .addToBackStack(null)
                .commit()
        }

        lytNoDataAddWorkoutBtn.setOnClickListener{
            navigateToFragment(YourActivityFragment(), "YourActivityFragment")
        }

        viewWorkoutHistory.setOnClickListener{
            navigateToFragment(YourActivityFragment(), "YourActivityFragment")
        }

        layoutAddWorkout.setOnClickListener {
            navigateToFragment(YourActivityFragment(), "YourActivityFragment")
        }
        activityFactorImageIcon.setOnClickListener {
            navigateToFragment(ActivityFactorFragment(), "ActivityFactorFragment")
        }
        logMealNoDataBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = YourMealLogsFragment()
                val args = Bundle()
                args.putString("ModuleName", "MoveRightLanding")
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }
        logMealButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = YourMealLogsFragment()
                val args = Bundle()
                args.putString("ModuleName", "MoveRightLanding")
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }

        caloricInfo.setOnClickListener {
            val yourCaloricSummaryInfoBottomSheet = YourCaloricSummaryInfoBottomSheet()
            yourCaloricSummaryInfoBottomSheet.isCancelable = true
            parentFragment.let { yourCaloricSummaryInfoBottomSheet.show(childFragmentManager, "YourCaloricSummaryInfoBottomSheet") }
        }

        yourMovementSummary.setOnClickListener {
            val yourMovementSummaryInfoBottomSheet = YourMovementSummaryInfoBottomSheet()
            yourMovementSummaryInfoBottomSheet.isCancelable = true
            parentFragment.let { yourMovementSummaryInfoBottomSheet.show(childFragmentManager, "YourMovementSummaryInfoBottomSheet") }
        }

        yourVitals.setOnClickListener {
            val yourVitalsInfoBottomSheet = YourVitalsInfoBottomSheet()
            yourVitalsInfoBottomSheet.isCancelable = true
            parentFragment.let { yourVitalsInfoBottomSheet.show(childFragmentManager, "YourVitalsInfoBottomSheet") }
        }

        yourHeartRateZone.setOnClickListener {
            val yourHeartRateZonesInfoBottomSheet = YourHeartRateZonesInfoBottomSheet()
            yourHeartRateZonesInfoBottomSheet.isCancelable = true
            parentFragment.let { yourHeartRateZonesInfoBottomSheet.show(childFragmentManager, "YourHeartRateZonesInfoBottomSheet") }
        }

        progressBarCalorieBalance.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                progressBarCalorieBalance.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val progressBarWidth = progressBarCalorieBalance.width.toFloat()
                val overlayPositionPercentage = 0.6f
                val rawProgress = progressBarCalorieBalance.progress
                val max = progressBarCalorieBalance.max
               // val rawProgress = it.data.calorieBalance.calorieIntake.toInt()
                progressBarCalorieBalance.progress = rawProgress.coerceIn(0, max)
                val progressPercentage = when {
                    max <= 0 -> 0f
                    rawProgress > max -> 0.96f
                    rawProgress == max -> 0.96f
                    rawProgress < 0 -> 0f
                    else -> rawProgress.toFloat() / max.toFloat()
                }
                val constraintSet = ConstraintSet()
                constraintSet.clone(progressBarLayout)
                constraintSet.setGuidelinePercent(R.id.circleIndicatorGuideline, progressPercentage)
                constraintSet.setGuidelinePercent(R.id.overlayGuideline, overlayPositionPercentage)
                constraintSet.applyTo(progressBarLayout)
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })

        syncWithHealthConnectButton.setOnClickListener {
            context?.let {
                val availabilityStatus = HealthConnectClient.getSdkStatus(it)
                if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
                    healthConnectClient = HealthConnectClient.getOrCreate(it)
                    lifecycleScope.launch {
                        showCompactSyncView()
                        requestPermissionsAndReadAllData()
                    }
                } else {
                    Toast.makeText(it, "Please install or update health connect from the Play Store.", Toast.LENGTH_LONG).show()
                    onSyncComplete()
                }
            }
        }

        rightLifeReportCard.setOnClickListener {
            var dynamicReportId = ""
            dynamicReportId = SharedPreferenceManager.getInstance(requireActivity()).userId
            if (dynamicReportId.isEmpty()) {
                // Some error handling if the ID is not available
            }else{
                val intent = Intent(requireActivity(), AIReportWebViewActivity::class.java).apply {
                    putExtra(AIReportWebViewActivity.EXTRA_REPORT_ID, dynamicReportId)
                }
                startActivity(intent)
            }
        }
    }

    private fun setupRecyclerView(view: View) {
         recyclerView = view.findViewById(R.id.recyclerView)
         adapter = GridAdapter(emptyList()) { itemName ->
            when (itemName) {
                "RHR" -> navigateToFragment(RestingHeartRateFragment(), "RestingHeartRateFragment")
                "Avg HR" -> navigateToFragment(AverageHeartRateFragment(), "AverageHeartRateFragment")
                "HRV" -> navigateToFragment(HeartRateVariabilityFragment(), "HeartRateVariabilityFragment")
                "Burn" -> navigateToFragment(BurnFragment(), "BurnFragment")
            }
        }
        context?.let {
            recyclerView.layoutManager = GridLayoutManager(it, 2)
            recyclerView.adapter = adapter
        }
       // if (!isRepeat){
            fetchMoveLanding(recyclerView, adapter)
       // }
    }

    private fun fetchMoveLanding(recyclerView: RecyclerView, adapter: GridAdapter) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (isAdded  && view != null){
                   // requireActivity().runOnUiThread {
                        showLoader(requireView())
                  //  }
                }
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val selectedDate = currentDateTime.format(formatter)
                val response = ApiClient.apiServiceFastApi.getMoveLanding(
                    userId = userId,
                    date = selectedDate
                )
                if (response.isSuccessful) {
                    val fitnessData = response.body()
                    fitnessData?.let {
                        val rhrData = padData(it.data.restingHeartRate.last7Days.map { day -> day.bpm }, 7)
                        val avgHrData = padData(it.data.averageHeartRate.last7Days.map { day -> day.heartRate }, 7)
                        val hrvData = padData(it.data.heartRateVariability.last7Days.map { day -> day.hrv }, 7)
                        val burnData = padData(it.data.caloriesBurned.last7Days.map { day -> day.caloriesBurned }, 7)
                        val activityFactorData = padData(it.data.activityFactor.last7Days.map { day -> day.activityFactor }, 7)
                        val todayStepCount = it.data.steps.todayTotal
                        val averageStepCount = it.data.steps.averageSteps
                        val goalStepCount = it.data.steps.goalSteps
                        val comparisonMessage = it.data.steps.comparisonMessage
                        val todayStepsData = it.data.steps.todayCumulativeSteps?.takeIf { it.isNotEmpty() }?.let { data ->
                            data.map { it.cumulativeSteps.toFloat() }.toFloatArray()
                        } ?: FloatArray(24) { 0f }.also {
                            //errorMessages.add("Today Steps")
                        }
                        val averageStepsData = it.data.steps.averageCumulativeSteps?.takeIf { it.isNotEmpty() }?.let { data ->
                            data.map { it.cumulativeSteps.toFloat() }.toFloatArray() } ?: FloatArray(24) { 0f }.also {
                          //  errorMessages.add("Average Steps")
                        }
                        val goalStepsData = it.data.steps.goalSteps.let { goal ->
                            FloatArray(24) { goal.toFloat() }
                        } ?: FloatArray(24) { 0f }.also {
                            //errorMessages.add("Goal Steps")
                        }
                        val items = listOf(
                            GridItem(
                                name = "RHR",
                                imageRes = R.drawable.rhr_icon,
                                additionalInfo = it.data.restingHeartRate.unit,
                                fourthParameter = it.data.restingHeartRate.today.toInt().toString(),
                                dataPoints = rhrData
                            ),
                            GridItem(
                                name = "Avg HR",
                                imageRes = R.drawable.rhr_icon,
                                additionalInfo = it.data.averageHeartRate.unit,
                                fourthParameter = it.data.averageHeartRate.today.toInt().toString(),
                                dataPoints = avgHrData
                            ),
                            GridItem(
                                name = "HRV",
                                imageRes = R.drawable.hrv_icon,
                                additionalInfo = it.data.heartRateVariability.unit,
                                fourthParameter = it.data.heartRateVariability.today.toInt().toString(),
                                dataPoints = hrvData
                            ),
                            GridItem(
                                name = "Burn",
                                imageRes = R.drawable.burn_icon,
                                additionalInfo = it.data.caloriesBurned.unit,
                                fourthParameter = it.data.caloriesBurned.today.toInt().toString(),
                                dataPoints = burnData
                            )
                        )
                        // Heart rate zone checks and UI updates
                        withContext(Dispatchers.Main) {
                            text_activity.text = it.data.activityFactor.today.toString() ?: "0"
                            val allInvalid = (/*it.data.calorieBalance.calorieBurnTarget == null || it.data.calorieBalance.calorieBurnTarget == 0f) &&
                                    (it.data.calorieBalance.difference == null || it.data.calorieBalance.difference == 0f) &&*/
                                    it.data.calorieBalance.calorieIntake == null || it.data.calorieBalance.calorieIntake == 0.0)
                            // Always set layout to VISIBLE
                            calorie_no_data_filled_layout.visibility = View.GONE
                            calorie_layout_data_filled.visibility = View.VISIBLE

                            if (allInvalid) {
                                // No data state
                                calorie_no_data_filled_layout.visibility = View.GONE
                                calorie_layout_data_filled.visibility = View.VISIBLE
                                val calorieIntake = it.data.calorieBalance.calorieIntake
                                val calorieRange = it.data.calorieBalance.calorieRange // or an array [start, end]
                                val colorRes = if (calorieIntake in calorieRange[0]..calorieRange[1]) {
                                    R.color.color_eat_right   // ✅ green
                                } else {
                                    R.color.red              // ❌ red
                                }
                                context?.let {
                                    calorieCountText.setTextColor(ContextCompat.getColor(it, colorRes))
                                }
//                                val color = when (it.data.calorieBalance.goal_text) {
//                                    "weight_loss" -> {
//                                        if (it.data.calorieBalance.calorieIntake < it.data.calorieBalance.calorieBurnTarget) R.color.color_eat_right else R.color.red
//                                    }
//                                    "weight_gain" -> {
//                                        if (it.data.calorieBalance.calorieIntake < it.data.calorieBalance.calorieBurnTarget) R.color.red else R.color.color_eat_right
//                                    }
//                                    else -> {
//                                        R.color.color_eat_right
//                                    }
//                                }
//                                calorieCountText.setTextColor(ContextCompat.getColor(requireContext(), color))
                                calorie_no_data_filled_layout.visibility = View.GONE
                                calorie_layout_data_filled.visibility = View.VISIBLE
                                tvBurnValue.text = if (it.data.calorieBalance.calorieBurnTarget == null || it.data.calorieBalance.calorieBurnTarget == 0.0) "0" else it.data.calorieBalance.calorieBurnTarget.toInt().toString()
                                val intake = it.data.calorieBalance.calorieIntake ?: 0.0
                                val burnTarget = it.data.calorieBalance.calorieBurnTarget ?: 0.0
                                val difference = (intake - burnTarget).toInt()

                                calorieCountText.text = if (difference >= 0) {
                                    difference.toString() // Positive value without sign
                                } else {
                                    difference.toString() // Negative value with minus sign (automatic)
                                }
                                calorieCountText.text = difference.toString()
                                totalIntakeCalorieText.text = if (it.data.calorieBalance.calorieIntake == null || it.data.calorieBalance.calorieIntake == 0.0) "0" else it.data.calorieBalance.calorieIntake.toInt().toString()
                                calorieBalanceMessageTitle.text = it.data.calorieBalance.heading
                                calorieBalanceDescription.text = it.data.calorieBalance.message
                                progressBarCalorieBalance.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                                    override fun onGlobalLayout() {
                                        progressBarCalorieBalance.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                        val progressBarWidth = progressBarCalorieBalance.width.toFloat()
                                        val burnedTarget = it.data.calorieBalance.calorieBurnTarget ?: 0.0
                                        var rangeEnd : Double = 0.0
                                        val rangeStart = it.data.calorieBalance.calorieRange.getOrNull(0) ?: 0.0
                                        if (it.data.calorieBalance.calorieRange.size > 1){
                                            rangeEnd = it.data.calorieBalance.calorieRange.getOrNull(1) ?: 0.0
                                        }
                                        val totalCalorie = it.data.calorieBalance.calorieBurnTarget.toInt() * 2
                                        val percentage = (( it.data.calorieBalance.calorieBurnTarget - it.data.calorieBalance.calorieRange.get(0)) / (it.data.calorieBalance.calorieRange.get(1) - it.data.calorieBalance.calorieRange.get(0))).toFloat()
                                        //  val percentage = (it.data.calorieBalance.calorieRange.get(0) / it.data.calorieBalance.calorieBurnTarget) * 100
                                        val value = (percentage / 10)
                                        //val overlayPositionPercentage : Float = String.format("%.1f", value).toFloat()
                                        val overlayPositionPercentage = if (value.isFinite() && !value.isNaN()) value else 0f
                                        progressBarCalorieBalance.max = totalCalorie
                                        val max = progressBarCalorieBalance.max
                                        val rawProgress = it.data.calorieBalance.calorieIntake.toInt()
                                        progressBarCalorieBalance.progress = rawProgress.coerceIn(0, max)
                                        val progressPercentage = when {
                                            max <= 0 -> 0f
                                            rawProgress > max -> 0.96f
                                            rawProgress == max -> 0.96f
                                            rawProgress < 0 -> 0f
                                            else -> rawProgress.toFloat() / max.toFloat()
                                        }

                                        val constraintSet = ConstraintSet()
                                        constraintSet.clone(progressBarLayout)
                                        constraintSet.setGuidelinePercent(R.id.circleIndicatorGuideline, progressPercentage)
                                        constraintSet.setGuidelinePercent(R.id.overlayGuideline, overlayPositionPercentage)
                                        constraintSet.applyTo(progressBarLayout)
                                        // D) Text Zone Label
                                        val zoneText = when {
                                            burnedTarget < rangeStart -> "Weight Gain Zone"
                                            burnedTarget.toInt() == rangeStart.toInt() -> "Weight Maintain Zone"
                                            else -> "Weight Loss Zone"
                                        }
                                        weightLossZoneText.text = it.data.calorieBalance.goal_text
                                        progressBarCalorieBalance.post {
                                            val max = progressBarCalorieBalance.max.toFloat()
                                            if (max <= 0f) return@post
                                            val barWidth = progressBarCalorieBalance.width -
                                                    progressBarCalorieBalance.paddingStart -
                                                    progressBarCalorieBalance.paddingEnd
                                            // Fractions (relative positions inside progress bar)
                                            val startFrac = rangeStart / max
                                            val endFrac = rangeEnd / max
                                            // X coordinates
                                            val startX = (barWidth * startFrac).toInt() + progressBarCalorieBalance.paddingStart
                                            val endX = (barWidth * endFrac).toInt() + progressBarCalorieBalance.paddingStart
                                            // Overlay width
                                            val context = context ?: return@post
                                            val minWidthPx = TypedValue.applyDimension(
                                                TypedValue.COMPLEX_UNIT_DIP,
                                                5f,
                                                context.resources.displayMetrics
                                            ).toInt()
                                            val overlayWidth = (endX - startX).coerceAtLeast(minWidthPx)
                                            // Apply layout params
                                            val lp = transparentOverlay.layoutParams as ConstraintLayout.LayoutParams
                                            lp.width = overlayWidth
                                            lp.marginStart = startX
                                            transparentOverlay.layoutParams = lp
                                            transparentOverlay.visibility = View.VISIBLE
                                        }
                                    }
                                })
                            } else {
                                // Data state
                                val calorieIntake = it.data.calorieBalance.calorieIntake
                                val calorieRange = it.data.calorieBalance.calorieRange // or an array [start, end]
                                val colorRes = if (calorieIntake in calorieRange[0]..calorieRange[1]) {
                                    R.color.color_eat_right   // ✅ green
                                } else {
                                    R.color.red              // ❌ red
                                }
                                context?.let {
                                    calorieCountText.setTextColor(ContextCompat.getColor(it, colorRes))
                                }
//                                val color = when (it.data.calorieBalance.goal_text) {
//                                    "weight_loss" -> {
//                                        if (it.data.calorieBalance.calorieIntake < it.data.calorieBalance.calorieBurnTarget) R.color.color_eat_right else R.color.red
//                                    }
//                                    "weight_gain" -> {
//                                        if (it.data.calorieBalance.calorieIntake < it.data.calorieBalance.calorieBurnTarget) R.color.red else R.color.color_eat_right
//                                    }
//                                    else -> {
//                                        R.color.color_eat_right
//                                    }
//                                }
//                                calorieCountText.setTextColor(ContextCompat.getColor(requireContext(), color))
                                calorie_no_data_filled_layout.visibility = View.GONE
                                calorie_layout_data_filled.visibility = View.VISIBLE
                                tvBurnValue.text = if (it.data.calorieBalance.calorieBurnTarget == null || it.data.calorieBalance.calorieBurnTarget == 0.0) "0" else it.data.calorieBalance.calorieBurnTarget.toInt().toString()
                                val intake = it.data.calorieBalance.calorieIntake ?: 0.0
                                val burnTarget = it.data.calorieBalance.calorieBurnTarget ?: 0.0
                                val difference = (intake - burnTarget).toInt()

                                calorieCountText.text = if (difference >= 0) {
                                    difference.toString() // Positive value without sign
                                } else {
                                    difference.toString() // Negative value with minus sign (automatic)
                                }
                                calorieCountText.text = difference.toString()
                                totalIntakeCalorieText.text = if (it.data.calorieBalance.calorieIntake == null || it.data.calorieBalance.calorieIntake == 0.0) "0" else it.data.calorieBalance.calorieIntake.toInt().toString()
                                calorieBalanceMessageTitle.text = it.data.calorieBalance.heading
                                calorieBalanceDescription.text = it.data.calorieBalance.message
                                progressBarCalorieBalance.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                                    override fun onGlobalLayout() {
                                        progressBarCalorieBalance.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                        val progressBarWidth = progressBarCalorieBalance.width.toFloat()
                                        val burnedTarget = it.data.calorieBalance.calorieBurnTarget ?: 0.0
                                        var rangeEnd : Double = 0.0
                                        val rangeStart = it.data.calorieBalance.calorieRange.getOrNull(0) ?: 0.0
                                        if (it.data.calorieBalance.calorieRange.size > 1){
                                            rangeEnd = it.data.calorieBalance.calorieRange.getOrNull(1) ?: 0.0
                                        }
                                        val totalCalorie = it.data.calorieBalance.calorieBurnTarget.toInt() * 2
                                        //val percentage = (( it.data.calorieBalance.calorieBurnTarget - it.data.calorieBalance.calorieRange.get(0)) / (it.data.calorieBalance.calorieRange.get(1) - it.data.calorieBalance.calorieRange.get(0))).toFloat()
                                      //  val percentage = (it.data.calorieBalance.calorieRange.get(0) / it.data.calorieBalance.calorieBurnTarget) * 100
                                        val denominator = (rangeEnd - rangeStart)
                                        val percentage = if (denominator != 0.0) {
                                            ((burnedTarget - rangeStart) / denominator).toFloat()
                                        } else 0f

                                        val value = (percentage / 10)
                                       // val overlayPositionPercentage : Float = String.format("%.1f", value).toFloat()
                                        val cleanValue = if (value.isFinite() && !value.isNaN()) {
                                            value
                                        } else {
                                            context?.let { it1 ->
                                                AnalyticsLogger.logEvent(
                                                    it1, AnalyticsEvent.MR_Number_Format_Exception,
                                                    mapOf(
                                                        AnalyticsParam.MR_Crash_Msg to 0f,
                                                        AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                                                    )
                                                )
                                            }
                                            0f
                                        }
                                        val overlayPositionPercentage = cleanValue
                                        progressBarCalorieBalance.max = totalCalorie
                                        val max = progressBarCalorieBalance.max
                                        progressBarCalorieBalance.progress = it.data.calorieBalance.calorieIntake.toInt()
                                        val rawProgress = it.data.calorieBalance.calorieIntake.toInt()
                                        progressBarCalorieBalance.progress = rawProgress.coerceIn(0, max)
                                        val progressPercentage = when {
                                            max <= 0 -> 0f
                                            rawProgress > max -> 0.96f
                                            rawProgress == max -> 0.96f
                                            rawProgress < 0 -> 0f
                                            else -> rawProgress.toFloat() / max.toFloat()
                                        }
                                        val constraintSet = ConstraintSet()
                                        constraintSet.clone(progressBarLayout)
                                        constraintSet.setGuidelinePercent(R.id.circleIndicatorGuideline, progressPercentage)
                                        constraintSet.setGuidelinePercent(R.id.overlayGuideline, overlayPositionPercentage)
                                        constraintSet.applyTo(progressBarLayout)
                                        // D) Text Zone Label
                                        val zoneText = when {
                                            burnedTarget < rangeStart -> "Weight Gain Zone"
                                            burnedTarget.toInt() == rangeStart.toInt() -> "Weight Maintain Zone"
                                            else -> "Weight Loss Zone"
                                        }
                                        weightLossZoneText.text = it.data.calorieBalance.goal_text
                                        progressBarCalorieBalance.post {
                                            val max = progressBarCalorieBalance.max.toFloat()
                                            if (max <= 0f) return@post
                                            val barWidth = progressBarCalorieBalance.width -
                                                    progressBarCalorieBalance.paddingStart -
                                                    progressBarCalorieBalance.paddingEnd
                                            // Fractions (relative positions inside progress bar)
                                            val startFrac = rangeStart / max
                                            val endFrac = rangeEnd / max
                                            // X coordinates
                                            val startX = (barWidth * startFrac).toInt() + progressBarCalorieBalance.paddingStart
                                            val endX = (barWidth * endFrac).toInt() + progressBarCalorieBalance.paddingStart
                                            // Overlay width
                                            val ctx = context ?: return@post  // stop if fragment not attached
                                            val minWidthPx = TypedValue.applyDimension(
                                                TypedValue.COMPLEX_UNIT_DIP,
                                                5f,
                                                ctx.resources.displayMetrics
                                            ).toInt()

                                            val overlayWidth = (endX - startX).coerceAtLeast(minWidthPx)
                                            // Apply layout params
                                            val lp = transparentOverlay.layoutParams as ConstraintLayout.LayoutParams
                                            lp.width = overlayWidth
                                            lp.marginStart = startX
                                            transparentOverlay.layoutParams = lp
                                            transparentOverlay.visibility = View.VISIBLE
                                        }
//                                        progressBarLayout.post {
//                                            val total = burnedTarget + rangeEnd
//                                            val startFrac = if (total > 0) (rangeStart / total).toFloat() else 0f
//                                            val endFrac = if (total > 0) (rangeEnd / total).toFloat() else 0f
//                                            val barWidth = progressBarLayout.width
//                                            // Convert 5dp → pixels
//                                            val minWidthPx = TypedValue.applyDimension(
//                                                TypedValue.COMPLEX_UNIT_DIP,
//                                                5f,
//                                                resources.displayMetrics
//                                            ).toInt()
//                                            val startX = (barWidth * startFrac).toInt()
//                                            val endX = (barWidth * endFrac).toInt()
//                                            val overlayWidth = (endX - startX).coerceAtLeast(minWidthPx)
//                                            val lp = transparentOverlay.layoutParams as ConstraintLayout.LayoutParams
//                                            lp.width = overlayWidth
//                                            lp.marginStart = startX
//                                            transparentOverlay.layoutParams = lp
//                                            transparentOverlay.visibility = View.VISIBLE
//                                        }
                                    }
                                })
                            }
                            val heartRateZones = it.data.heartRateZones
                            val errorMessages = mutableListOf<String>()
                            if (activityFactorData.isEmpty() || activityFactorData.all { it == 0f }) {
                                text_no_data_activity_factor.visibility = View.VISIBLE
                                line_graph.visibility = View.GONE
                                //line_graph.setDataPoints(emptyList())
                            } else {
                                text_no_data_activity_factor.visibility = View.GONE
                                line_graph.visibility = View.VISIBLE
                                line_graph.setDataPoints(activityFactorData)
                            }
                           // line_graph.setDataPoints(activityFactorData)
                            withContext(Dispatchers.Main) {
                                if (it.data.steps.todayTotal > 0 || it.data.steps.averageSteps > 0 || it.data.steps.goalSteps > 0){
                                    stepNoDataLayout.visibility = View.GONE
                                    stepWithDataCardLayout.visibility = View.VISIBLE

                                    stepLineGraphView.clear()
                                    stepLineGraphView.addDataSet(todayStepsData, 0xFFFD6967.toInt()) // Red
                                    stepLineGraphView.addDataSet(averageStepsData, 0xFF707070.toInt()) // Gray
                                    stepLineGraphView.addDataSet(goalStepsData, 0xFF03B27B.toInt()) // Green (dotted)
                                    val blackLineData = FloatArray(goalStepsData.size) { 0f } // 7 zeros
                                    stepLineGraphView.addDataSet(blackLineData, 0xFFA7A7A7.toInt())
                                    stepLineGraphView.invalidate()
                                    if (goalStepCount <= 0) {
                                        horizontalStepsSection.visibility = View.VISIBLE
                                        stepsBottomSection.visibility = View.GONE
                                    } else {
                                        horizontalStepsSection.visibility = View.GONE
                                        stepsBottomSection.visibility = View.VISIBLE
                                    }
                                    todayStepsTv.text = todayStepCount.toString()
                                    today_steps_count.text = todayStepCount.toString()
                                    averageStepsTv.text = averageStepCount.toString()
                                    yesterday_steps_count.text = averageStepCount.toString()
                                    goalStepsTv.text = goalStepCount.toString()
                                    stepHeading.text = markdownToBold(comparisonMessage)
                                }else{
                                   if(it.data.caloriesBurned.today.toDouble() == 0.0){
                                       val result = hasAnyValueGreaterThanZero(avgHrData)
                                       if (result){
                                           step_forward_icon.visibility = View.VISIBLE
                                           stepNoDataLayout.visibility = View.GONE
                                           stepWithDataCardLayout.visibility = View.VISIBLE
                                           stepLineGraphView.clear()
                                           stepLineGraphView.addDataSet(todayStepsData, 0xFFFD6967.toInt()) // Red
                                           stepLineGraphView.addDataSet(averageStepsData, 0xFF707070.toInt()) // Gray
                                           stepLineGraphView.addDataSet(goalStepsData, 0xFF03B27B.toInt())
                                           val blackLineData = FloatArray(goalStepsData.size) { 0f } // 7 zeros
                                           stepLineGraphView.addDataSet(blackLineData, 0xFFA7A7A7.toInt())
                                           stepLineGraphView.invalidate()
                                           if (goalStepCount <= 0) {
                                               horizontalStepsSection.visibility = View.VISIBLE
                                               stepsBottomSection.visibility = View.GONE
                                           } else {
                                               horizontalStepsSection.visibility = View.GONE
                                               stepsBottomSection.visibility = View.VISIBLE
                                           }
                                           todayStepsTv.text = todayStepCount.toString()
                                           today_steps_count.text = todayStepCount.toString()
                                           averageStepsTv.text = averageStepCount.toString()
                                           yesterday_steps_count.text = averageStepCount.toString()
                                           goalStepsTv.text = goalStepCount.toString()
                                           stepHeading.text = markdownToBold(comparisonMessage)
                                       }else{
                                           step_forward_icon.visibility = View.INVISIBLE
                                           stepNoDataLayout.visibility = View.VISIBLE
                                           stepWithDataCardLayout.visibility = View.GONE
                                       }
                                   }else{
                                       step_forward_icon.visibility = View.VISIBLE
                                       stepNoDataLayout.visibility = View.GONE
                                       stepWithDataCardLayout.visibility = View.VISIBLE
                                       stepLineGraphView.clear()
                                       stepLineGraphView.addDataSet(todayStepsData, 0xFFFD6967.toInt()) // Red
                                       stepLineGraphView.addDataSet(averageStepsData, 0xFF707070.toInt()) // Gray
                                       stepLineGraphView.addDataSet(goalStepsData, 0xFF03B27B.toInt())
                                       val blackLineData = FloatArray(goalStepsData.size) { 0f } // 7 zeros
                                       stepLineGraphView.addDataSet(blackLineData, 0xFFA7A7A7.toInt())
                                       stepLineGraphView.invalidate()
                                       if (goalStepCount <= 0) {
                                           horizontalStepsSection.visibility = View.VISIBLE
                                           stepsBottomSection.visibility = View.GONE
                                       } else {
                                           horizontalStepsSection.visibility = View.GONE
                                           stepsBottomSection.visibility = View.VISIBLE
                                       }
                                       todayStepsTv.text = todayStepCount.toString()
                                       today_steps_count.text = todayStepCount.toString()
                                       averageStepsTv.text = averageStepCount.toString()
                                       yesterday_steps_count.text = averageStepCount.toString()
                                       goalStepsTv.text = goalStepCount.toString()
                                       stepHeading.text = markdownToBold(comparisonMessage)
                                   }
                                }
                            }
                            if (heartRateZones != null) {
                                // Check Light Zone
                                heartRateZoneNoDataTv.visibility = View.GONE
                                lightZoneBelow.visibility = View.VISIBLE
                                lightZoneHighl.visibility = View.VISIBLE
                                fatLossHighl.visibility = View.VISIBLE
                                cardioHighl.visibility = View.VISIBLE
                                peakHighl.visibility = View.VISIBLE
                                verticalLineStartLightBpmTv.visibility = View.VISIBLE
                                verticalLineFatLossBpmTv.visibility = View.VISIBLE
                                verticalLineCardioBpmTv.visibility = View.VISIBLE
                                verticalLinePeakBpmTv.visibility = View.VISIBLE
                                verticalLinePeakEndBpmTv.visibility = View.VISIBLE
                                if (heartRateZones.heartRateZones.lightZone?.size?.let { it >= 2 } == true) {
                                    lightZoneBelow.text = heartRateZones.heartRateZones.lightZone[0].toString()
                                    lightZoneHighl.text = heartRateZones.heartRateZones.lightZone[1].toString()
                                } else {
                                    lightZoneBelow.text = "N/A"
                                    lightZoneHighl.text = "N/A"
                                    errorMessages.add("Light Zone")
                                }

                                // Check Fat Burn Zone
                                if (heartRateZones.heartRateZones.fatBurnZone?.size?.let { it >= 2 } == true) {
                                    fatLossHighl.text = heartRateZones.heartRateZones.fatBurnZone[1].toString()
                                } else {
                                    fatLossHighl.text = "N/A"
                                    errorMessages.add("Fat Burn Zone")
                                }

                                // Check Cardio Zone
                                if (heartRateZones.heartRateZones.cardioZone?.size?.let { it >= 2 } == true) {
                                    cardioHighl.text = heartRateZones.heartRateZones.cardioZone[1].toString()
                                } else {
                                    cardioHighl.text = "N/A"
                                    errorMessages.add("Cardio Zone")
                                }

                                // Check Peak Zone
                                if (heartRateZones.heartRateZones.peakZone?.size?.let { it >= 2 } == true) {
                                    peakHighl.text = heartRateZones.heartRateZones.peakZone[1].toString()
                                } else {
                                    peakHighl.text = "N/A"
                                    errorMessages.add("Peak Zone")
                                }

                                // Show a single Toast for all errors
                                if (errorMessages.isNotEmpty()) {
                                    val message = "Incomplete data for: ${errorMessages.joinToString(", ")}"
                                    context?.let {
                                        Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                if (isAdded  && view != null){
                                    requireActivity().runOnUiThread {
                                        dismissLoader(requireView())
                                    }
                                }
                                heartRateZoneNoDataTv.visibility = View.VISIBLE
                                lightZoneBelow.visibility = View.GONE
                                lightZoneHighl.visibility = View.GONE
                                fatLossHighl.visibility = View.GONE
                                cardioHighl.visibility = View.GONE
                                peakHighl.visibility = View.GONE
                                verticalLineStartLightBpmTv.visibility = View.GONE
                                verticalLineFatLossBpmTv.visibility = View.GONE
                                verticalLineCardioBpmTv.visibility = View.GONE
                                verticalLinePeakBpmTv.visibility = View.GONE
                                verticalLinePeakEndBpmTv.visibility = View.GONE
                                lightZoneBelow.text = "N/A"
                                lightZoneHighl.text = "N/A"
                                fatLossHighl.text = "N/A"
                                cardioHighl.text = "N/A"
                                peakHighl.text = "N/A"
                                context?.let {
                                    Toast.makeText(it, "Heart Rate Zones data missing", Toast.LENGTH_SHORT).show()
                                }
                            }
                            // Update RecyclerView
                            adapter.updateItems(items)
                            recyclerView.adapter = adapter
                           // recyclerView.setHasFixedSize(true)

                            // Add ItemDecoration for spacing
                            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing) // Define in res/values/dimens.xml
                            recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount = 2, spacing = spacingInPixels, includeEdge = true))
                        }
                    } ?: withContext(Dispatchers.Main) {
                        if (isAdded  && view != null){
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                        context?.let {
                            Toast.makeText(it, "No data received from API", Toast.LENGTH_SHORT).show()
                        }
                    }
                  //  if (!isRepeat){
                        val availabilityStatus = context?.let { it }
                            ?.let { HealthConnectClient.getSdkStatus(it) }
                        if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
                            healthConnectClient = HealthConnectClient.getOrCreate(requireContext())
                            lifecycleScope.launch {
                                showCompactSyncView()
                                requestPermissionsAndReadAllData()
                            }
                        } else {
                            Toast.makeText(context?.let { it }, "Please install or update samsung from the Play Store.", Toast.LENGTH_LONG).show()
                            onSyncComplete()
                        }
                  //  }
                } else {
                    withContext(Dispatchers.Main) {
                        if (isAdded  && view != null){
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                        Toast.makeText(context?.let { it }, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                      //  if (!isRepeat){
                            val availabilityStatus = HealthConnectClient.getSdkStatus(requireContext())
                        showCompactSyncView()
                            if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
                                healthConnectClient = HealthConnectClient.getOrCreate(requireContext())
                                lifecycleScope.launch {
                                    requestPermissionsAndReadAllData()
                                }
                            } else {
                                Toast.makeText(context?.let { it }, "Please install or update samsung from the Play Store.", Toast.LENGTH_LONG).show()
                                onSyncComplete()
                            }
                        }
                 //   }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }
        }
    }

    fun messageForSteps(
        today: Int,
        yesterday: Int,
        goal: Int?
    ): String {
        // 1️⃣ User HAS set a goal
        if (goal != null && goal > 0) {
            return when {
                today < yesterday && today < goal ->
                    "You’ve done better — still time to close the gap!"
                today < yesterday && today >= goal ->
                    "Goal crushed — steady work pays off!"
                today >= yesterday && today < goal ->
                    "Pacing ahead of yesterday — now push for your goal!"
                else -> // today >= yesterday && today >= goal
                    "You beat yesterday *and* hit your goal — that's how it’s done!"
            }
        }
        // 2️⃣ User has NOT set a goal
        if (today < yesterday) {
            return "Still time to turn the day around — you’ve done more before!"
        }
        val tenPercent = (yesterday * 0.10).toInt()
        if (kotlin.math.abs(today - yesterday) <= tenPercent) {
            return "Consistency is a win — keep the streak alive!"
        }
        return "You’re outpacing yesterday — keep that momentum going!"
    }

    fun markdownToBold(text: String): Spanned {
        val htmlText = text
            .replace("\n", "<br>")
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
        return Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
    }

    private fun fetchMoveLandingAfterRefresh(recyclerView: RecyclerView, adapter: GridAdapter) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
               // if (isAdded  && view != null){
                    // requireActivity().runOnUiThread {
                   // showLoader(view.r)
                    //  }
            //    }
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val selectedDate = currentDateTime.format(formatter)
                val response = ApiClient.apiServiceFastApi.getMoveLanding(
                    userId = userId,
                    date = selectedDate
                )
                if (response.isSuccessful) {
                    val fitnessData = response.body()
                    fitnessData?.let {
                        val rhrData = padData(it.data.restingHeartRate.last7Days.map { day -> day.bpm }, 7)
                        val avgHrData = padData(it.data.averageHeartRate.last7Days.map { day -> day.heartRate }, 7)
                        val hrvData = padData(it.data.heartRateVariability.last7Days.map { day -> day.hrv }, 7)
                        val burnData = padData(it.data.caloriesBurned.last7Days.map { day -> day.caloriesBurned }, 7)
                        val activityFactorData = padData(it.data.activityFactor.last7Days.map { day -> day.activityFactor }, 7)
                        val todayStepCount = it.data.steps.todayTotal
                        val averageStepCount = it.data.steps.averageSteps
                        val goalStepCount = it.data.steps.goalSteps
                        val comparisonMessage = it.data.steps.comparisonMessage
                        val todayStepsData = it.data.steps.todayCumulativeSteps?.takeIf { it.isNotEmpty() }?.let { data ->
                            data.map { it.cumulativeSteps.toFloat() }.toFloatArray()
                        } ?: FloatArray(24) { 0f }.also {
                            //errorMessages.add("Today Steps")
                        }
                        val averageStepsData = it.data.steps.averageCumulativeSteps?.takeIf { it.isNotEmpty() }?.let { data ->
                            data.map { it.cumulativeSteps.toFloat() }.toFloatArray()
                        } ?: FloatArray(24) { 0f }.also {
                            //  errorMessages.add("Average Steps")
                        }
                        val goalStepsData = it.data.steps.goalSteps.let { goal ->
                            FloatArray(24) { goal.toFloat() }
                        } ?: FloatArray(24) { 0f }.also {
                            //errorMessages.add("Goal Steps")
                        }
                        val items = listOf(
                            GridItem(
                                name = "RHR",
                                imageRes = R.drawable.rhr_icon,
                                additionalInfo = it.data.restingHeartRate.unit,
                                fourthParameter = it.data.restingHeartRate.today.toInt().toString(),
                                dataPoints = rhrData
                            ),
                            GridItem(
                                name = "Avg HR",
                                imageRes = R.drawable.rhr_icon,
                                additionalInfo = it.data.averageHeartRate.unit,
                                fourthParameter = it.data.averageHeartRate.today.toInt().toString(),
                                dataPoints = avgHrData
                            ),
                            GridItem(
                                name = "HRV",
                                imageRes = R.drawable.hrv_icon,
                                additionalInfo = it.data.heartRateVariability.unit,
                                fourthParameter = it.data.heartRateVariability.today.toInt().toString(),
                                dataPoints = hrvData
                            ),
                            GridItem(
                                name = "Burn",
                                imageRes = R.drawable.burn_icon,
                                additionalInfo = it.data.caloriesBurned.unit,
                                fourthParameter = it.data.caloriesBurned.today.toInt().toString(),
                                dataPoints = burnData
                            )
                        )
                        // Heart rate zone checks and UI updates
                        withContext(Dispatchers.Main) {
                            text_activity.text = it.data.activityFactor.today.toString() ?: "0"
                            val allInvalid = (/*it.data.calorieBalance.calorieBurnTarget == null || it.data.calorieBalance.calorieBurnTarget == 0f) &&
                                    (it.data.calorieBalance.difference == null || it.data.calorieBalance.difference == 0f) &&*/
                                    it.data.calorieBalance.calorieIntake == null || it.data.calorieBalance.calorieIntake == 0.0)
                            // Always set layout to VISIBLE
                            calorie_no_data_filled_layout.visibility = View.GONE
                            calorie_layout_data_filled.visibility = View.VISIBLE

                            if (allInvalid) {
                                // No data state
                                calorie_no_data_filled_layout.visibility = View.GONE
                                calorie_layout_data_filled.visibility = View.VISIBLE
                                val calorieIntake = it.data.calorieBalance.calorieIntake
                                val calorieRange = it.data.calorieBalance.calorieRange // or an array [start, end]
                                val colorRes = if (calorieIntake in calorieRange[0]..calorieRange[1]) {
                                    R.color.color_eat_right   // ✅ green
                                } else {
                                    R.color.red              // ❌ red
                                }
                                context?.let {
                                    calorieCountText.setTextColor(ContextCompat.getColor(it, colorRes))
                                }
//                                val color = when (it.data.calorieBalance.goal) {
//                                    "weight_loss" -> {
//                                        if (it.data.calorieBalance.calorieIntake < it.data.calorieBalance.calorieBurnTarget) R.color.color_eat_right else R.color.red
//                                    }
//                                    "weight_gain" -> {
//                                        if (it.data.calorieBalance.calorieIntake < it.data.calorieBalance.calorieBurnTarget) R.color.red else R.color.color_eat_right
//                                    }
//                                    else -> {
//                                        R.color.color_eat_right
//                                    }
//                                }
//                                calorieCountText.setTextColor(ContextCompat.getColor(requireContext(), color))
                                calorie_no_data_filled_layout.visibility = View.GONE
                                calorie_layout_data_filled.visibility = View.VISIBLE
                                tvBurnValue.text = if (it.data.calorieBalance.calorieBurnTarget == null || it.data.calorieBalance.calorieBurnTarget == 0.0) "0" else it.data.calorieBalance.calorieBurnTarget.toInt().toString()
                                val intake = it.data.calorieBalance.calorieIntake ?: 0.0
                                val burnTarget = it.data.calorieBalance.calorieBurnTarget ?: 0.0
                                val difference = (intake - burnTarget).toInt()

                                calorieCountText.text = if (difference >= 0) {
                                    difference.toString() // Positive value without sign
                                } else {
                                    difference.toString() // Negative value with minus sign (automatic)
                                }
                                calorieCountText.text = difference.toString()
                                totalIntakeCalorieText.text = if (it.data.calorieBalance.calorieIntake == null || it.data.calorieBalance.calorieIntake == 0.0) "0" else it.data.calorieBalance.calorieIntake.toInt().toString()
                                calorieBalanceMessageTitle.text = it.data.calorieBalance.heading
                                calorieBalanceDescription.text = it.data.calorieBalance.message
                                progressBarCalorieBalance.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                                    override fun onGlobalLayout() {
                                        progressBarCalorieBalance.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                        val progressBarWidth = progressBarCalorieBalance.width.toFloat()
                                        val burnedTarget = it.data.calorieBalance.calorieBurnTarget ?: 0.0
                                        var rangeEnd : Double = 0.0
                                        val rangeStart = it.data.calorieBalance.calorieRange.getOrNull(0) ?: 0.0
                                        if (it.data.calorieBalance.calorieRange.size > 1){
                                            rangeEnd = it.data.calorieBalance.calorieRange.getOrNull(1) ?: 0.0
                                        }
                                        val totalCalorie = it.data.calorieBalance.calorieBurnTarget.toInt() * 2
                                        val percentage = (( it.data.calorieBalance.calorieBurnTarget - it.data.calorieBalance.calorieRange.get(0)) / (it.data.calorieBalance.calorieRange.get(1) - it.data.calorieBalance.calorieRange.get(0))).toFloat()
                                        //  val percentage = (it.data.calorieBalance.calorieRange.get(0) / it.data.calorieBalance.calorieBurnTarget) * 100
                                        val value = (percentage / 10)
                                        //val overlayPositionPercentage : Float = String.format("%.1f", value).toFloat()
                                        val overlayPositionPercentage = if (value.isFinite() && !value.isNaN()) value else 0f
                                        progressBarCalorieBalance.max = totalCalorie
                                        val max = progressBarCalorieBalance.max
                                        val rawProgress = it.data.calorieBalance.calorieIntake.toInt()
                                        progressBarCalorieBalance.progress = rawProgress.coerceIn(0, max)
                                        val progressPercentage = when {
                                            max <= 0 -> 0f
                                            rawProgress > max -> 0.96f
                                            rawProgress == max -> 0.96f
                                            rawProgress < 0 -> 0f
                                            else -> rawProgress.toFloat() / max.toFloat()
                                        }

                                        val constraintSet = ConstraintSet()
                                        constraintSet.clone(progressBarLayout)
                                        constraintSet.setGuidelinePercent(R.id.circleIndicatorGuideline, progressPercentage)
                                        constraintSet.setGuidelinePercent(R.id.overlayGuideline, overlayPositionPercentage)
                                        constraintSet.applyTo(progressBarLayout)
                                        // D) Text Zone Label
                                        val zoneText = when {
                                            burnedTarget < rangeStart -> "Weight Gain Zone"
                                            burnedTarget.toInt() == rangeStart.toInt() -> "Weight Maintain Zone"
                                            else -> "Weight Loss Zone"
                                        }
                                        weightLossZoneText.text = it.data.calorieBalance.goal_text
                                        progressBarCalorieBalance.post {
                                            val max = progressBarCalorieBalance.max.toFloat()
                                            if (max <= 0f) return@post
                                            val barWidth = progressBarCalorieBalance.width -
                                                    progressBarCalorieBalance.paddingStart -
                                                    progressBarCalorieBalance.paddingEnd
                                            // Fractions (relative positions inside progress bar)
                                            val startFrac = rangeStart / max
                                            val endFrac = rangeEnd / max
                                            // X coordinates
                                            val startX = (barWidth * startFrac).toInt() + progressBarCalorieBalance.paddingStart
                                            val endX = (barWidth * endFrac).toInt() + progressBarCalorieBalance.paddingStart
                                            // Overlay width
                                            val context = context ?: return@post
                                            val minWidthPx = TypedValue.applyDimension(
                                                TypedValue.COMPLEX_UNIT_DIP,
                                                5f,
                                                context.resources.displayMetrics
                                            ).toInt()
                                            val overlayWidth = (endX - startX).coerceAtLeast(minWidthPx)
                                            // Apply layout params
                                            val lp = transparentOverlay.layoutParams as ConstraintLayout.LayoutParams
                                            lp.width = overlayWidth
                                            lp.marginStart = startX
                                            transparentOverlay.layoutParams = lp
                                            transparentOverlay.visibility = View.VISIBLE
                                        }
                                    }
                                })
                            } else {
                                // Data state
                                val calorieIntake = it.data.calorieBalance.calorieIntake
                                val calorieRange = it.data.calorieBalance.calorieRange // or an array [start, end]
                                val colorRes = if (calorieIntake in calorieRange[0]..calorieRange[1]) {
                                    R.color.color_eat_right   // ✅ green
                                } else {
                                    R.color.red              // ❌ red
                                }
                                context?.let {
                                    calorieCountText.setTextColor(ContextCompat.getColor(it, colorRes))
                                }
//                                val color = when (it.data.calorieBalance.goal) {
//                                    "weight_loss" -> {
//                                        if (it.data.calorieBalance.calorieIntake < it.data.calorieBalance.calorieBurnTarget) R.color.color_eat_right else R.color.red
//                                    }
//                                    "weight_gain" -> {
//                                        if (it.data.calorieBalance.calorieIntake < it.data.calorieBalance.calorieBurnTarget) R.color.red else R.color.color_eat_right
//                                    }
//                                    else -> {
//                                        R.color.color_eat_right
//                                    }
//                                }
//                                calorieCountText.setTextColor(ContextCompat.getColor(requireContext(), color))
                                calorie_no_data_filled_layout.visibility = View.GONE
                                calorie_layout_data_filled.visibility = View.VISIBLE
                                tvBurnValue.text = if (it.data.calorieBalance.calorieBurnTarget == null || it.data.calorieBalance.calorieBurnTarget == 0.0) "0" else it.data.calorieBalance.calorieBurnTarget.toInt().toString()
                                val intake = it.data.calorieBalance.calorieIntake ?: 0.0
                                val burnTarget = it.data.calorieBalance.calorieBurnTarget ?: 0.0
                                val difference = (intake - burnTarget).toInt()

                                calorieCountText.text = if (difference >= 0) {
                                    difference.toString() // Positive value without sign
                                } else {
                                    difference.toString() // Negative value with minus sign (automatic)
                                }
                                calorieCountText.text = difference.toString()
                                totalIntakeCalorieText.text = if (it.data.calorieBalance.calorieIntake == null || it.data.calorieBalance.calorieIntake == 0.0) "0" else it.data.calorieBalance.calorieIntake.toInt().toString()
                                calorieBalanceMessageTitle.text = it.data.calorieBalance.heading
                                calorieBalanceDescription.text = it.data.calorieBalance.message
                                progressBarCalorieBalance.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                                    override fun onGlobalLayout() {
                                        progressBarCalorieBalance.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                        val progressBarWidth = progressBarCalorieBalance.width.toFloat()
                                        val burnedTarget = it.data.calorieBalance.calorieBurnTarget ?: 0.0
                                        var rangeEnd : Double = 0.0
                                        val rangeStart = it.data.calorieBalance.calorieRange.getOrNull(0) ?: 0.0
                                        if (it.data.calorieBalance.calorieRange.size > 1){
                                            rangeEnd = it.data.calorieBalance.calorieRange.getOrNull(1) ?: 0.0
                                        }
                                        val totalCalorie = it.data.calorieBalance.calorieBurnTarget.toInt() + rangeEnd.toInt()
                                        //val percentage = (( it.data.calorieBalance.calorieBurnTarget - it.data.calorieBalance.calorieRange.get(0)) / (it.data.calorieBalance.calorieRange.get(1) - it.data.calorieBalance.calorieRange.get(0))).toFloat()
                                        //  val percentage = (it.data.calorieBalance.calorieRange.get(0) / it.data.calorieBalance.calorieBurnTarget) * 100
                                        val denominator = (rangeEnd - rangeStart)
                                        val percentage = if (denominator != 0.0) {
                                            ((burnedTarget - rangeStart) / denominator).toFloat()
                                        } else 0f

                                        val value = (percentage / 10)
                                        // val overlayPositionPercentage : Float = String.format("%.1f", value).toFloat()
                                        val cleanValue = if (value.isFinite() && !value.isNaN()) {
                                            value
                                        } else {
                                            context?.let { it1 ->
                                                AnalyticsLogger.logEvent(
                                                    it1, AnalyticsEvent.MR_Number_Format_Exception,
                                                    mapOf(
                                                        AnalyticsParam.MR_Crash_Msg to 0f,
                                                        AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                                                    )
                                                )
                                            }
                                            0f
                                        }
                                        val overlayPositionPercentage = cleanValue

                                        progressBarCalorieBalance.max = totalCalorie
                                        val max = progressBarCalorieBalance.max
                                        val rawProgress = it.data.calorieBalance.calorieIntake.toInt()
                                        progressBarCalorieBalance.progress = rawProgress.coerceIn(0, max)
                                        val progressPercentage = when {
                                            max <= 0 -> 0f
                                            rawProgress > max -> 0.96f
                                            rawProgress == max -> 0.96f
                                            rawProgress < 0 -> 0f
                                            else -> rawProgress.toFloat() / max.toFloat()
                                        }
                                        val constraintSet = ConstraintSet()
                                        constraintSet.clone(progressBarLayout)
                                        constraintSet.setGuidelinePercent(R.id.circleIndicatorGuideline, progressPercentage)
                                        constraintSet.setGuidelinePercent(R.id.overlayGuideline, overlayPositionPercentage)
                                        constraintSet.applyTo(progressBarLayout)
                                        // D) Text Zone Label
                                        val zoneText = when {
                                            burnedTarget < rangeStart -> "Weight Gain Zone"
                                            burnedTarget.toInt() == rangeStart.toInt() -> "Weight Maintain Zone"
                                            else -> "Weight Loss Zone"
                                        }
                                        weightLossZoneText.text = it.data.calorieBalance.goal_text
                                        progressBarCalorieBalance.post {
                                            val max = progressBarCalorieBalance.max.toFloat()
                                            if (max <= 0f) return@post
                                            val barWidth = progressBarCalorieBalance.width -
                                                    progressBarCalorieBalance.paddingStart -
                                                    progressBarCalorieBalance.paddingEnd
                                            // Fractions (relative positions inside progress bar)
                                            val startFrac = rangeStart / max
                                            val endFrac = rangeEnd / max
                                            // X coordinates
                                            val startX = (barWidth * startFrac).toInt() + progressBarCalorieBalance.paddingStart
                                            val endX = (barWidth * endFrac).toInt() + progressBarCalorieBalance.paddingStart
                                            // Overlay width
                                            val ctx = context ?: return@post  // stop if fragment not attached
                                            val minWidthPx = TypedValue.applyDimension(
                                                TypedValue.COMPLEX_UNIT_DIP,
                                                5f,
                                                ctx.resources.displayMetrics
                                            ).toInt()

                                            val overlayWidth = (endX - startX).coerceAtLeast(minWidthPx)
                                            // Apply layout params
                                            val lp = transparentOverlay.layoutParams as ConstraintLayout.LayoutParams
                                            lp.width = overlayWidth
                                            lp.marginStart = startX
                                            transparentOverlay.layoutParams = lp
                                            transparentOverlay.visibility = View.VISIBLE
                                        }
//                                        progressBarLayout.post {
//                                            val total = burnedTarget + rangeEnd
//                                            val startFrac = if (total > 0) (rangeStart / total).toFloat() else 0f
//                                            val endFrac = if (total > 0) (rangeEnd / total).toFloat() else 0f
//                                            val barWidth = progressBarLayout.width
//                                            // Convert 5dp → pixels
//                                            val minWidthPx = TypedValue.applyDimension(
//                                                TypedValue.COMPLEX_UNIT_DIP,
//                                                5f,
//                                                resources.displayMetrics
//                                            ).toInt()
//                                            val startX = (barWidth * startFrac).toInt()
//                                            val endX = (barWidth * endFrac).toInt()
//                                            val overlayWidth = (endX - startX).coerceAtLeast(minWidthPx)
//                                            val lp = transparentOverlay.layoutParams as ConstraintLayout.LayoutParams
//                                            lp.width = overlayWidth
//                                            lp.marginStart = startX
//                                            transparentOverlay.layoutParams = lp
//                                            transparentOverlay.visibility = View.VISIBLE
//                                        }
                                    }
                                })
                            }
                            val heartRateZones = it.data.heartRateZones
                            val errorMessages = mutableListOf<String>()
                            if (activityFactorData.isEmpty() || activityFactorData.all { it == 0f }) {
                                text_no_data_activity_factor.visibility = View.VISIBLE
                                line_graph.visibility = View.GONE
                                //line_graph.setDataPoints(emptyList())
                            } else {
                                text_no_data_activity_factor.visibility = View.GONE
                                line_graph.visibility = View.VISIBLE
                                line_graph.setDataPoints(activityFactorData)
                            }
                            // line_graph.setDataPoints(activityFactorData)
                            withContext(Dispatchers.Main) {
                                if (it.data.steps.todayTotal > 0 || it.data.steps.averageSteps > 0 || it.data.steps.goalSteps > 0){
                                    stepNoDataLayout.visibility = View.GONE
                                    stepWithDataCardLayout.visibility = View.VISIBLE

                                    stepLineGraphView.clear()
                                    stepLineGraphView.addDataSet(todayStepsData, 0xFFFD6967.toInt()) // Red
                                    stepLineGraphView.addDataSet(averageStepsData, 0xFF707070.toInt()) // Gray
                                    stepLineGraphView.addDataSet(goalStepsData, 0xFF03B27B.toInt()) // Green (dotted)
                                    val blackLineData = FloatArray(goalStepsData.size) { 0f } // 7 zeros
                                    stepLineGraphView.addDataSet(blackLineData, 0xFFA7A7A7.toInt())
                                    stepLineGraphView.invalidate()
                                    if (goalStepCount <= 0) {
                                        horizontalStepsSection.visibility = View.VISIBLE
                                        stepsBottomSection.visibility = View.GONE
                                    } else {
                                        horizontalStepsSection.visibility = View.GONE
                                        stepsBottomSection.visibility = View.VISIBLE
                                    }
                                    todayStepsTv.text = todayStepCount.toString()
                                    today_steps_count.text = todayStepCount.toString()
                                    averageStepsTv.text = averageStepCount.toString()
                                    yesterday_steps_count.text = averageStepCount.toString()
                                    goalStepsTv.text = goalStepCount.toString()
                                    stepHeading.text = markdownToBold(comparisonMessage)
                                }else{
                                    if(it.data.caloriesBurned.today.toDouble() == 0.0){
                                        val result = hasAnyValueGreaterThanZero(avgHrData)
                                        if (result){
                                            step_forward_icon.visibility = View.VISIBLE
                                            stepNoDataLayout.visibility = View.GONE
                                            stepWithDataCardLayout.visibility = View.VISIBLE
                                            stepLineGraphView.clear()
                                            stepLineGraphView.addDataSet(todayStepsData, 0xFFFD6967.toInt()) // Red
                                            stepLineGraphView.addDataSet(averageStepsData, 0xFF707070.toInt()) // Gray
                                            stepLineGraphView.addDataSet(goalStepsData, 0xFF03B27B.toInt())
                                            val blackLineData = FloatArray(goalStepsData.size) { 0f } // 7 zeros
                                            stepLineGraphView.addDataSet(blackLineData, 0xFFA7A7A7.toInt())
                                            stepLineGraphView.invalidate()
                                            if (goalStepCount <= 0) {
                                                horizontalStepsSection.visibility = View.VISIBLE
                                                stepsBottomSection.visibility = View.GONE
                                            } else {
                                                horizontalStepsSection.visibility = View.GONE
                                                stepsBottomSection.visibility = View.VISIBLE
                                            }
                                            todayStepsTv.text = todayStepCount.toString()
                                            today_steps_count.text = todayStepCount.toString()
                                            averageStepsTv.text = averageStepCount.toString()
                                            yesterday_steps_count.text = averageStepCount.toString()
                                            goalStepsTv.text = goalStepCount.toString()
                                            stepHeading.text = markdownToBold(comparisonMessage)
                                        }else{
                                            step_forward_icon.visibility = View.INVISIBLE
                                            stepNoDataLayout.visibility = View.VISIBLE
                                            stepWithDataCardLayout.visibility = View.GONE
                                        }
                                    }else{
                                        step_forward_icon.visibility = View.VISIBLE
                                        stepNoDataLayout.visibility = View.GONE
                                        stepWithDataCardLayout.visibility = View.VISIBLE
                                        stepLineGraphView.clear()
                                        stepLineGraphView.addDataSet(todayStepsData, 0xFFFD6967.toInt()) // Red
                                        stepLineGraphView.addDataSet(averageStepsData, 0xFF707070.toInt()) // Gray
                                        stepLineGraphView.addDataSet(goalStepsData, 0xFF03B27B.toInt())
                                        val blackLineData = FloatArray(goalStepsData.size) { 0f } // 7 zeros
                                        stepLineGraphView.addDataSet(blackLineData, 0xFFA7A7A7.toInt())
                                        stepLineGraphView.invalidate()
                                        if (goalStepCount <= 0) {
                                            horizontalStepsSection.visibility = View.VISIBLE
                                            stepsBottomSection.visibility = View.GONE
                                        } else {
                                            horizontalStepsSection.visibility = View.GONE
                                            stepsBottomSection.visibility = View.VISIBLE
                                        }
                                        todayStepsTv.text = todayStepCount.toString()
                                        today_steps_count.text = todayStepCount.toString()
                                        averageStepsTv.text = averageStepCount.toString()
                                        yesterday_steps_count.text = averageStepCount.toString()
                                        goalStepsTv.text = goalStepCount.toString()
                                        stepHeading.text = markdownToBold(comparisonMessage)
                                    }
                                }
                            }
                            if (heartRateZones != null) {
                                // Check Light Zone
                                heartRateZoneNoDataTv.visibility = View.GONE
                                lightZoneBelow.visibility = View.VISIBLE
                                lightZoneHighl.visibility = View.VISIBLE
                                fatLossHighl.visibility = View.VISIBLE
                                cardioHighl.visibility = View.VISIBLE
                                peakHighl.visibility = View.VISIBLE
                                verticalLineStartLightBpmTv.visibility = View.VISIBLE
                                verticalLineFatLossBpmTv.visibility = View.VISIBLE
                                verticalLineCardioBpmTv.visibility = View.VISIBLE
                                verticalLinePeakBpmTv.visibility = View.VISIBLE
                                verticalLinePeakEndBpmTv.visibility = View.VISIBLE
                                if (heartRateZones.heartRateZones.lightZone?.size?.let { it >= 2 } == true) {
                                    lightZoneBelow.text = heartRateZones.heartRateZones.lightZone[0].toString()
                                    lightZoneHighl.text = heartRateZones.heartRateZones.lightZone[1].toString()
                                } else {
                                    lightZoneBelow.text = "N/A"
                                    lightZoneHighl.text = "N/A"
                                    errorMessages.add("Light Zone")
                                }

                                // Check Fat Burn Zone
                                if (heartRateZones.heartRateZones.fatBurnZone?.size?.let { it >= 2 } == true) {
                                    fatLossHighl.text = heartRateZones.heartRateZones.fatBurnZone[1].toString()
                                } else {
                                    fatLossHighl.text = "N/A"
                                    errorMessages.add("Fat Burn Zone")
                                }

                                // Check Cardio Zone
                                if (heartRateZones.heartRateZones.cardioZone?.size?.let { it >= 2 } == true) {
                                    cardioHighl.text = heartRateZones.heartRateZones.cardioZone[1].toString()
                                } else {
                                    cardioHighl.text = "N/A"
                                    errorMessages.add("Cardio Zone")
                                }

                                // Check Peak Zone
                                if (heartRateZones.heartRateZones.peakZone?.size?.let { it >= 2 } == true) {
                                    peakHighl.text = heartRateZones.heartRateZones.peakZone[1].toString()
                                } else {
                                    peakHighl.text = "N/A"
                                    errorMessages.add("Peak Zone")
                                }

                                // Show a single Toast for all errors
                                if (errorMessages.isNotEmpty()) {
                                    val message = "Incomplete data for: ${errorMessages.joinToString(", ")}"
                                    context?.let {
                                        Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                if (isAdded  && view != null){
                                    requireActivity().runOnUiThread {
                                        dismissLoader(requireView())
                                    }
                                }
                                heartRateZoneNoDataTv.visibility = View.VISIBLE
                                lightZoneBelow.visibility = View.GONE
                                lightZoneHighl.visibility = View.GONE
                                fatLossHighl.visibility = View.GONE
                                cardioHighl.visibility = View.GONE
                                peakHighl.visibility = View.GONE
                                verticalLineStartLightBpmTv.visibility = View.GONE
                                verticalLineFatLossBpmTv.visibility = View.GONE
                                verticalLineCardioBpmTv.visibility = View.GONE
                                verticalLinePeakBpmTv.visibility = View.GONE
                                verticalLinePeakEndBpmTv.visibility = View.GONE
                                lightZoneBelow.text = "N/A"
                                lightZoneHighl.text = "N/A"
                                fatLossHighl.text = "N/A"
                                cardioHighl.text = "N/A"
                                peakHighl.text = "N/A"
                                context?.let {
                                    Toast.makeText(it, "Heart Rate Zones data missing", Toast.LENGTH_SHORT).show()
                                }
                            }
                            // Update RecyclerView
                            adapter.updateItems(items)
                            recyclerView.adapter = adapter
                            // recyclerView.setHasFixedSize(true)

                            // Add ItemDecoration for spacing
//                            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing) // Define in res/values/dimens.xml
//                            recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount = 2, spacing = spacingInPixels, includeEdge = true))
                            swipeRefreshLayout.isRefreshing = false
                        }
                    } ?: withContext(Dispatchers.Main) {
                        if (isAdded  && view != null){
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                        context?.let {
                            Toast.makeText(it, "No data received from API", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        if (isAdded  && view != null){
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                        Toast.makeText(context?.let { it }, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                            e.message?.let { Log.d("REFRESH", it) }
                            swipeRefreshLayout.isRefreshing = false
                        }
                    }
                }
            }
        }
    }

    fun hasAnyValueGreaterThanZero(array: FloatArray): Boolean {
        return array.any { it > 0f }
    }

    override fun onResume() {
        super.onResume()
        if (!SharedPreferenceManager.getInstance(context?.let { it }).getAIReportGeneratedView()){
            if (SharedPreferenceManager.getInstance(context?.let { it }).userProfile?.isReportGenerated == true) {
                rightLifeReportCard.visibility = View.VISIBLE
            }else{
                rightLifeReportCard.visibility = View.GONE
            }
        } else {
            rightLifeReportCard.visibility = View.GONE
        }
        fetchUserWorkouts()
    }

    private fun padData(data: List<Float>, targetSize: Int): FloatArray {
        val result = FloatArray(targetSize) { 0f }
        data.forEachIndexed { index, value ->
            if (index < targetSize) result[index] = value
        }
        return result
    }

    private fun addDotsIndicator(count: Int) {
        dots = arrayOfNulls(count)
        dotsLayout.removeAllViews()
        for (i in 0 until count) {
            dots[i] = ImageView(context?.let { it }).apply {
                setImageDrawable(context?.let { it }
                    ?.let { ContextCompat.getDrawable(it, R.drawable.new_unselected_dot) })
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(8, 0, 8, 0) }
                layoutParams = params
            }
            dotsLayout.addView(dots[i])
        }
        updateDots(0)
    }

    private fun updateDots(position: Int) {
        dots.forEachIndexed { index, imageView ->
            imageView?.setImageResource(if (index == position) R.drawable.dot_selected else R.drawable.new_unselected_dot)
        }
    }

    private fun navigateToFragment(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    private suspend fun requestPermissionsAndReadAllData() {
        try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (allReadPermissions.all { it in granted }) {
                fetchAllHealthData()
            } else {
                requestPermissionsLauncher.launch(allReadPermissions)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error checking permissions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
        lifecycleScope.launch {
            if (granted.containsAll(allReadPermissions)) {
                fetchAllHealthData()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                }
                fetchAllHealthData()
            }
        }
    }

    fun convertUtcToInstant(utcString: String): Instant {
        val zonedDateTime = ZonedDateTime.parse(utcString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
        return zonedDateTime.toInstant()
    }
//    fun convertUtcToInstant(utcString: String): Instant {
//        return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(utcString))
//    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("This app needs health permissions to fetch your data. Please grant all permissions to continue.")
            .setPositiveButton("Grant Permissions") { _, _ ->
                // Launch permission request again when user clicks "Grant Permissions"
                requestPermissionsLauncher.launch(allReadPermissions)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(context, "Permissions denied. Some features may not work.", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false) // Prevent dismissing by back button
            .show()
    }

    private suspend fun fetchAllHealthDataFinal() {
        try {
            if (isAdded  && view != null){
                requireActivity().runOnUiThread {
                    showLoader(requireView())
                }
            }
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            val now = Instant.now()
            val syncTime = SharedPreferenceManager.getInstance(context?.let { it }).moveRightSyncTime.orEmpty()
            val startTime: Instant = if (syncTime.isBlank()) {
                // First-time sync: pull last 30 days
                now.minus(Duration.ofDays(30))
            } else {
                // Next sync: only fetch new data
                Instant.parse(syncTime)
            }
            val endTime: Instant = now
            // Trackers for incremental sync
            var latestModifiedTime: Instant? = null
            var recordsFound = false

            // Update function for lastModifiedTime
            fun updateLastSync(record: Record) {
                val modified = record.metadata.lastModifiedTime
                if (latestModifiedTime == null || modified.isAfter(latestModifiedTime)) {
                    latestModifiedTime = modified
                }
            }
//            var startTime = Instant.now()
//            val syncTime = SharedPreferenceManager.getInstance(context?.let { it }).moveRightSyncTime ?: ""
//            if (syncTime == "") {
//                endTime = Instant.now()
//                startTime = endTime.minus(Duration.ofDays(30))
//            }else{
//                endTime = Instant.now()
//                startTime = convertUtcToInstant(syncTime)
//            }
            if (HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class) in grantedPermissions) {
                if (syncTime == "") {
                    val totalCaloroieResponse = mutableListOf<TotalCaloriesBurnedRecord>()
                    val totalDuration = Duration.between(startTime, endTime)
                    val chunkDuration = totalDuration.dividedBy(15)
                    var chunkStart = startTime
                    repeat(15) { i ->
                        val chunkEnd = if (i == 14) endTime else chunkStart.plus(chunkDuration)
                        val response = healthConnectClient.readRecords(
                            ReadRecordsRequest(
                                recordType = TotalCaloriesBurnedRecord::class,
                                timeRangeFilter = TimeRangeFilter.between(chunkStart, chunkEnd)
                            )
                        )
                        totalCaloroieResponse.addAll(response.records)
                        Log.d("HealthData", "Chunk $i → ${response.records.size} Step records")
                        chunkStart = chunkEnd
                    }
                    totalCaloriesBurnedRecord = totalCaloroieResponse
                }else{
                    val caloriesResponse = healthConnectClient.readRecords(
                        ReadRecordsRequest(
                            recordType = TotalCaloriesBurnedRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                        )
                    )
                    totalCaloriesBurnedRecord = caloriesResponse.records
                }
                // Iterate each record individually
                totalCaloriesBurnedRecord?.forEach { record ->
                    val burnedCalories = record.energy.inKilocalories
                    val start = record.startTime
                    val end = record.endTime
                    recordsFound = true
                    updateLastSync(record)
                    Log.d("HealthData", "Total Calories Burned: $burnedCalories kcal | From: $start To: $end")
                }
            } else {
                totalCaloriesBurnedRecord = emptyList()
                Log.d("HealthData", "Total Calories Burned permission denied")
            }
            if (HealthPermission.getReadPermission(StepsRecord::class) in grantedPermissions) {
                if (syncTime == "") {
                    val stepsResponse = mutableListOf<StepsRecord>()
                    val totalDuration = Duration.between(startTime, endTime)
                    val chunkDuration = totalDuration.dividedBy(15)
                    var chunkStart = startTime
                    repeat(15) { i ->
                        val chunkEnd = if (i == 14) endTime else chunkStart.plus(chunkDuration)
                        val response = healthConnectClient.readRecords(
                            ReadRecordsRequest(
                                recordType = StepsRecord::class,
                                timeRangeFilter = TimeRangeFilter.between(chunkStart, chunkEnd)
                            )
                        )
                        stepsResponse.addAll(response.records)
                        Log.d("HealthData", "Chunk $i → ${response.records.size} Step records")
                        chunkStart = chunkEnd
                    }
                    stepsRecord = stepsResponse
                }else{
                    val stepsResponse = healthConnectClient.readRecords(
                        ReadRecordsRequest(
                            recordType = StepsRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                        )
                    )
                    stepsRecord = stepsResponse.records
                }
                stepsRecord?.forEach { record ->
                    recordsFound = true
                    updateLastSync(record)
                }
            } else {
                stepsRecord = emptyList()
                Log.d("HealthData", "Steps permission denied")
            }
            if (HealthPermission.getReadPermission(HeartRateRecord::class) in grantedPermissions) {
                if (syncTime == "") {
                    val results = mutableListOf<HeartRateRecord>()
                    val totalDuration = Duration.between(startTime, endTime)
                    val chunkDuration = totalDuration.dividedBy(15)
                    var chunkStart = startTime
                    repeat(15) { i ->
                        val chunkEnd = if (i == 14) endTime else chunkStart.plus(chunkDuration)
                        val response = healthConnectClient.readRecords(
                            ReadRecordsRequest(
                                recordType = HeartRateRecord::class,
                                timeRangeFilter = TimeRangeFilter.between(chunkStart, chunkEnd)
                            )
                        )
                        results.addAll(response.records)
                        Log.d("HealthData", "Chunk $i → ${results.size} HR records")
                        chunkStart = chunkEnd
                    }
                    heartRateRecord = results
                }else{
                    val response = healthConnectClient.readRecords(
                        ReadRecordsRequest(
                            recordType = HeartRateRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                        )
                    )
                    heartRateRecord = response.records
                    Log.d("HealthData", "Total HR records fetched: ${response.records.size}")
                }
                heartRateRecord?.forEach { record ->
                    recordsFound = true
                    updateLastSync(record)
                }
            }else {
                heartRateRecord = emptyList()
                Log.d("HealthData", "Heart rate permission denied")
            }
            if (HealthPermission.getReadPermission(RestingHeartRateRecord::class) in grantedPermissions) {
                val restingHRResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = RestingHeartRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                restingHeartRecord = restingHRResponse.records
                restingHeartRecord?.forEach { record ->
                    recordsFound = true
                    updateLastSync(record)
                    Log.d("HealthData", "Resting Heart Rate: ${record.beatsPerMinute} bpm, Time: ${record.time}")
                }
            }else {
                restingHeartRecord = emptyList()
                Log.d("HealthData", "Resting Heart rate permission denied")
            }
            if (HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class) in grantedPermissions) {
                    val activeCalorieResponse = healthConnectClient.readRecords(
                        ReadRecordsRequest(
                            recordType = ActiveCaloriesBurnedRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                        )
                    )
                    activeCalorieBurnedRecord = activeCalorieResponse.records
                    activeCalorieBurnedRecord?.forEach { record ->
                        recordsFound = true
                        updateLastSync(record)
                        Log.d("HealthData", "Active Calories Burn Rate: ${record.energy} kCal, Time: ${record.startTime}")
                    }
            }else {
                activeCalorieBurnedRecord = emptyList()
                Log.d("HealthData", "Active Calories burn permission denied")
            }
            if (HealthPermission.getReadPermission(BasalMetabolicRateRecord::class) in grantedPermissions) {
                val basalMetabolic = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = BasalMetabolicRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                basalMetabolicRateRecord = basalMetabolic.records
                basalMetabolicRateRecord?.forEach { record ->
                    Log.d("HealthData", "Basal Metabolic Rate: ${record.basalMetabolicRate}, Time: ${record.time}")
                }
            }else {
                basalMetabolicRateRecord = emptyList()
                Log.d("HealthData", "Basal Metabolic permission denied")
            }
            if (HealthPermission.getReadPermission(BloodPressureRecord::class) in grantedPermissions) {
                val bloodPressure = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = BloodPressureRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                bloodPressureRecord = bloodPressure.records
                bloodPressureRecord?.forEach { record ->
                    Log.d("HealthData", "Blood Pressure: ${record.systolic}, Time: ${record.time}")
                }
            }else {
                bloodPressureRecord = emptyList()
                Log.d("HealthData", "Blood Pressure  permission denied")
            }
            if (HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class) in grantedPermissions) {
                val restingVresponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = HeartRateVariabilityRmssdRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                heartRateVariability = restingVresponse.records
                heartRateVariability?.forEach { record ->
                    recordsFound = true
                    updateLastSync(record)
                    Log.d("HealthData", "Heart Rate Variability: ${record.heartRateVariabilityMillis}, Time: ${record.time}")
                }
            }else {
                heartRateVariability = emptyList()
                Log.d("HealthData", "Heart rate Variability permission denied")
            }
            if (HealthPermission.getReadPermission(SleepSessionRecord::class) in grantedPermissions) {
                val sleepResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = SleepSessionRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                sleepSessionRecord = sleepResponse.records
                sleepSessionRecord?.forEach { record ->
                    recordsFound = true
                    updateLastSync(record)
                    Log.d("HealthData", "Sleep Session: Start: ${record.startTime}, End: ${record.endTime}, Stages: ${record.stages}")
                }
            } else {
                sleepSessionRecord = emptyList()
                Log.d("HealthData", "Sleep session permission denied")
            }
            if (HealthPermission.getReadPermission(ExerciseSessionRecord::class) in grantedPermissions) {
                val exerciseResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = ExerciseSessionRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                exerciseSessionRecord = exerciseResponse.records
                exerciseSessionRecord?.forEach { record ->
                    recordsFound = true
                    updateLastSync(record)
                    Log.d("HealthData", "Exercise Session: Type: ${record.exerciseType}, Start: ${record.startTime}, End: ${record.endTime}")
                }
            } else {
                exerciseSessionRecord = emptyList()
                Log.d("HealthData", "Exercise session permission denied")
            }
            if (HealthPermission.getReadPermission(WeightRecord::class) in grantedPermissions) {
                val weightResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = WeightRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                weightRecord = weightResponse.records
                weightRecord?.forEach { record ->
                    Log.d("HealthData", "Weight: ${record.weight.inKilograms} kg, Time: ${record.time}")
                }
            } else {
                weightRecord = emptyList()
                Log.d("HealthData", "Weight permission denied")
            }
            if (HealthPermission.getReadPermission(BodyFatRecord::class) in grantedPermissions) {
                val bodyFatResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = BodyFatRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                bodyFatRecord = bodyFatResponse.records
                bodyFatRecord?.forEach { record ->
                    Log.d("HealthData", "Body Fat: ${record.percentage.value * 100}%, Time: ${record.time}")
                }
            } else {
                bodyFatRecord = emptyList()
                Log.d("HealthData", "Body Fat permission denied")
            }
            if (HealthPermission.getReadPermission(DistanceRecord::class) in grantedPermissions) {
                val distanceResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = DistanceRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                distanceRecord = distanceResponse.records
                val totalDistance = distanceRecord?.sumOf { it.distance.inMeters } ?: 0.0
                Log.d("HealthData", "Total Distance: $totalDistance meters")
            } else {
                distanceRecord = emptyList()
                Log.d("HealthData", "Distance permission denied")
            }
            if (HealthPermission.getReadPermission(OxygenSaturationRecord::class) in grantedPermissions) {
                val oxygenSaturationResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = OxygenSaturationRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                oxygenSaturationRecord = oxygenSaturationResponse.records
                oxygenSaturationRecord?.forEach { record ->
                    Log.d("HealthData", "Oxygen Saturation: ${record.percentage.value}%, Time: ${record.time}")
                }
            } else {
                oxygenSaturationRecord = emptyList()
                Log.d("HealthData", "Oxygen saturation permission denied")
            }
            if (HealthPermission.getReadPermission(RespiratoryRateRecord::class) in grantedPermissions) {
                val respiratoryRateResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = RespiratoryRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                respiratoryRateRecord = respiratoryRateResponse.records
                respiratoryRateRecord?.forEach { record ->
                    recordsFound = true
                    updateLastSync(record)
                    Log.d("HealthData", "Respiratory Rate: ${record.rate} breaths/min, Time: ${record.time}")
                }
            } else {
                respiratoryRateRecord = emptyList()
                Log.d("HealthData", "Respiratory rate permission denied")
            }
            var dataOrigin = "android phone"
            if (HealthPermission.getReadPermission(StepsRecord::class) in grantedPermissions) {
                val stepsResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                for (record in stepsResponse.records) {
                    dataOrigin = record.metadata.dataOrigin.packageName
                    val deviceInfo = record.metadata.device
                    if (deviceInfo != null) {
                        if (deviceInfo.manufacturer != "") {
                            SharedPreferenceManager.getInstance(context?.let { it }).saveDeviceName(deviceInfo.manufacturer)
                            Log.d("Device Info", """ Manufacturer: ${deviceInfo.manufacturer}
                Model: ${deviceInfo.model} Type: ${deviceInfo.type} """.trimIndent())
                            break
                        }else{
                            SharedPreferenceManager.getInstance(context?.let { it }).saveDeviceName(dataOrigin)
                            break
                        }
                    } else {
                        SharedPreferenceManager.getInstance(context?.let { it }).saveDeviceName(dataOrigin)
                        break
                    }
                }
            }
            if (recordsFound && latestModifiedTime != null) {
                context?.let {
                    SharedPreferenceManager.getInstance(it).saveMoveRightSyncTime(latestModifiedTime.toString())
                }
                Log.d("HealthSync", "✔ Saved new last sync time: $latestModifiedTime")

            } else {
                Log.d("HealthSync", "⚠ No new data found → NOT updating last sync time")
            }
            if (dataOrigin.equals("com.google.android.apps.fitness")){
                storeHealthData()
            }else if(dataOrigin.equals("com.sec.android.app.shealth")){
                storeSamsungHealthData()
            }else if(dataOrigin.equals("com.samsung.android.wear.shealth")){
                storeSamsungHealthData()
            }else{
                storeHealthData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
               // Toast.makeText(context, "Error fetching health data: ${e.message}", Toast.LENGTH_SHORT).show()
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        }
    }

    private suspend fun fetchAllHealthData() {
        try {
            showLoaderSafe()

            val ctx = context ?: return
            val client = healthConnectClient

            val granted = try {
                client.permissionController.getGrantedPermissions()
            } catch (e: Exception) {
                Log.e("HealthSync", "Permission fetch failed", e)
                emptySet()
            }

            Log.d("HealthSync", "Granted permissions = $granted")

            val now = Instant.now()

            val savedSync = SharedPreferenceManager
                .getInstance(ctx)
                .moveRightSyncTime
                .orEmpty()

            val isFirstSync = savedSync.isBlank()

            val defaultStart = now.minus(Duration.ofDays(30))
            val lastSyncInstant = if (isFirstSync) null else runCatching {
                Instant.parse(savedSync)
            }.getOrNull()

            // ✅ TIMEZONE SAFE
            val todayStart = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()

            val computedStartTime = when {
                isFirstSync -> defaultStart
                lastSyncInstant != null -> {
                    if (lastSyncInstant.isAfter(todayStart)) todayStart else lastSyncInstant
                }
                else -> defaultStart
            }

            val endTime = now

            Log.d("HealthSync", "StartTime = $computedStartTime")
            Log.d("HealthSync", "EndTime   = $endTime")

            var latestModified: Instant? = null
            var foundNewData = false

            fun markModified(record: Record) {
                foundNewData = true
                val modified = record.metadata.lastModifiedTime
                if (latestModified == null || modified.isAfter(latestModified)) {
                    latestModified = modified
                }
            }

            suspend fun <T : Record> fetchChunk(type: KClass<T>): List<T> {
                return try {
                    if (isFirstSync) {
                        fetchChunked(type, computedStartTime, endTime, 15)
                    } else {
                        client.readRecords(
                            ReadRecordsRequest(
                                type,
                                TimeRangeFilter.between(computedStartTime, endTime)
                            )
                        ).records
                    }
                } catch (e: Exception) {
                    Log.e("HealthSync", "Fetch failed for ${type.simpleName}", e)
                    emptyList()
                }
            }

            fun <T : Record> hasPermission(type: KClass<T>): Boolean {
                val perm = HealthPermission.getReadPermission(type)
                val grantedNow = perm in granted
                if (!grantedNow) {
                    Log.w("HealthSync", "Missing permission for ${type.simpleName}")
                }
                return grantedNow
            }

            suspend fun <T : Record> load(
                type: KClass<T>,
                assign: (List<T>) -> Unit
            ) {
                Log.d("HealthSync", "Loading ${type.simpleName}")

                if (!hasPermission(type)) {
                    assign(emptyList())
                    return
                }

                val records = fetchChunk(type)
                Log.d("HealthSync", "${type.simpleName} count = ${records.size}")

                assign(records)
                records.forEach { markModified(it) }
            }

            // ------------------------------
            // FETCH
            // ------------------------------
            load(TotalCaloriesBurnedRecord::class) { totalCaloriesBurnedRecord = it }
            load(StepsRecord::class) { stepsRecord = it }
            load(HeartRateRecord::class) { heartRateRecord = it }
            load(RestingHeartRateRecord::class) { restingHeartRecord = it }
            load(ActiveCaloriesBurnedRecord::class) { activeCalorieBurnedRecord = it }
            load(HeartRateVariabilityRmssdRecord::class) { heartRateVariability = it }
            load(SleepSessionRecord::class) { sleepSessionRecord = it }
            load(ExerciseSessionRecord::class) { exerciseSessionRecord = it }
            load(RespiratoryRateRecord::class) { respiratoryRateRecord = it }
            load(WeightRecord::class) { weightRecord = it }
            load(BodyFatRecord::class) { bodyFatRecord = it }
            load(DistanceRecord::class) { distanceRecord = it }
            load(OxygenSaturationRecord::class) { oxygenSaturationRecord = it }
            load(BasalMetabolicRateRecord::class) { basalMetabolicRateRecord = it }
            load(BloodPressureRecord::class) { bloodPressureRecord = it }

            // ------------------------------
            // DEVICE DETECTION
            // ------------------------------
            val devicePackage =
                stepsRecord?.firstOrNull()?.metadata?.dataOrigin?.packageName ?: "unknown"

            val deviceManufacturer =
                stepsRecord?.firstOrNull()?.metadata?.device?.manufacturer ?: devicePackage

            SharedPreferenceManager.getInstance(ctx).saveDeviceName(deviceManufacturer)

            // ------------------------------
            // SAVE SYNC TIME
            // ------------------------------
            if (foundNewData && latestModified != null) {
                SharedPreferenceManager
                    .getInstance(ctx)
                    .saveMoveRightSyncTime(latestModified.toString())

                Log.d("HealthSync", "Updated lastSync = $latestModified")
            } else {
                Log.d("HealthSync", "No new data. Sync time unchanged")
            }

            // ------------------------------
            // PUSH TO SERVER
            // ------------------------------
            when (devicePackage) {
                "com.google.android.apps.fitness" -> storeHealthData()
                "com.sec.android.app.shealth",
                "com.samsung.android.wear.shealth" -> storeSamsungHealthData()
                else -> storeHealthData()
            }

        } catch (e: Exception) {
            Log.e("HealthSync", "Fatal error", e)
        } finally {
            hideLoaderSafe()
        }
    }

    private fun Instant.hourStart(): Instant =
        atZone(ZoneId.systemDefault())
            .truncatedTo(ChronoUnit.HOURS)
            .toInstant()

    private fun Instant.dayStart(): Instant =
        atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()

    private fun Instant.zoneOffset(): ZoneOffset =
        ZoneId.systemDefault().rules.getOffset(this)

    private fun List<StepsRecord>.toHourlySteps(): List<StepsRecord> {
        if (isEmpty()) return emptyList()

        return groupBy { it.endTime.dayStart() }
            .flatMap { (_, dayRecords) ->

                dayRecords
                    .groupBy { it.endTime.hourStart() }
                    .toSortedMap()
                    .map { (hour, records) ->

                        val hourSteps = records.sumOf { it.count }
                        val offset = hour.zoneOffset()

                        StepsRecord(
                            startTime = hour,
                            startZoneOffset = offset,
                            endTime = hour.plus(Duration.ofHours(1)),
                            endZoneOffset = offset,
                            count = hourSteps, // ✅ ONLY this hour
                            metadata = records.first().metadata
                        )
                    }
            }
    }

    private fun List<ActiveCaloriesBurnedRecord>.toHourlyCumulativeActiveCalories()
            : List<ActiveCaloriesBurnedRecord> {

        if (isEmpty()) return emptyList()

        return groupBy { it.endTime.dayStart() }
            .flatMap { (_, dayRecords) ->

                val deltasByHour = dayRecords.groupBy {
                    it.endTime.hourStart()
                }.mapValues { (_, records) ->
                    records.sumOf { it.energy.inKilocalories }
                }

                var runningKcal = 0.0

                deltasByHour.keys.sorted().map { hour ->
                    runningKcal += deltasByHour[hour] ?: 0.0
                    val offset = hour.zoneOffset()

                    ActiveCaloriesBurnedRecord(
                        startTime = hour,
                        startZoneOffset = offset,
                        endTime = hour.plus(Duration.ofHours(1)),
                        endZoneOffset = offset,
                        energy = Energy.kilocalories(runningKcal),
                        metadata = dayRecords.first().metadata
                    )
                }
            }
    }

    private fun List<TotalCaloriesBurnedRecord>.toHourlyCumulativeTotalCalories()
            : List<TotalCaloriesBurnedRecord> {

        if (isEmpty()) return emptyList()

        return groupBy { it.endTime.dayStart() }
            .flatMap { (_, dayRecords) ->

                dayRecords.groupBy {
                    it.endTime.hourStart()
                }.mapNotNull { (_, records) ->
                    records.maxByOrNull { it.endTime }
                }
            }
    }

//    private suspend fun fetchAllHealthData() {
//        try {
//            showLoaderSafe()
//            val client = healthConnectClient
//            val granted = client.permissionController.getGrantedPermissions()
//            val now = Instant.now()
//            // ------------------------------
//            // 1) Load last sync time
//            // ------------------------------
//            val savedSync = SharedPreferenceManager.getInstance(context?.let { it }).moveRightSyncTime.orEmpty()
//            val isFirstSync = savedSync.isBlank()
//            // FIRST SYNC → last 30 days
//            val defaultStart = now.minus(Duration.ofDays(30))
//            // Next sync starts from last modified time
//            val lastSyncInstant = if (isFirstSync) null else Instant.parse(savedSync)
//            // ------------------------------
//            // 2) Always re-sync TODAY (Fix for Fitbit/Samsung)
//            // ------------------------------
//            val todayStart = LocalDate.now()
//                .atStartOfDay()
//                .toInstant(ZoneOffset.UTC)
//
//            val computedStartTime = when {
//                isFirstSync -> defaultStart
//                lastSyncInstant != null -> {
//                    // If lastSyncInstant lies inside today,
//                    // force resync whole today
//                    if (lastSyncInstant.isAfter(todayStart))
//                        todayStart
//                    else
//                        lastSyncInstant
//                }
//                else -> defaultStart
//            }
//            val endTime = now
//            Log.d("HealthSync", "StartTime = $computedStartTime")
//            Log.d("HealthSync", "EndTime   = $endTime")
//            var latestModified: Instant? = null
//            var foundNewData = false
//
//            // Update latest modified
//            fun markModified(record: Record) {
//                foundNewData = true
//                val modified = record.metadata.lastModifiedTime
//                if (latestModified == null || modified.isAfter(latestModified))
//                    latestModified = modified
//            }
//            // ------------------------------
//            // 3) Chunked reading for first sync
//            // ------------------------------
//            suspend fun <T : Record> fetchChunk(type: KClass<T>): List<T> {
//                return if (isFirstSync)
//                    fetchChunked(type, computedStartTime, endTime, 15)
//                else
//                    client.readRecords(
//                        ReadRecordsRequest(
//                            type,
//                            TimeRangeFilter.between(computedStartTime, endTime)
//                        )
//                    ).records
//            }
//            // ------------------------------
//            // 4) Permission check
//            // ------------------------------
//            fun <T : Record> hasPermission(type: KClass<T>) =
//                HealthPermission.getReadPermission(type) in granted
//            // ------------------------------
//            // 5) Loader & assignment helper
//            // ------------------------------
//            suspend fun <T : Record> load(
//                type: KClass<T>,
//                assign: (List<T>) -> Unit
//            ) {
//                if (!hasPermission(type)) {
//                    Log.w("HealthSync", "Permission missing for ${type.simpleName}")
//                    assign(emptyList())
//                    return
//                }
//                val records = fetchChunk(type)
//                assign(records)
//                records.forEach { markModified(it) }
//            }
//            // ------------------------------
//            // 6) Fetch all record types
//            // ------------------------------
//            load(TotalCaloriesBurnedRecord::class) { totalCaloriesBurnedRecord = it }
//            load(StepsRecord::class) { stepsRecord = it }
//            load(HeartRateRecord::class) { heartRateRecord = it }
//            load(RestingHeartRateRecord::class) { restingHeartRecord = it }
//            load(ActiveCaloriesBurnedRecord::class) { activeCalorieBurnedRecord = it }
//            load(HeartRateVariabilityRmssdRecord::class) { heartRateVariability = it }
//            load(SleepSessionRecord::class) { sleepSessionRecord = it }
//            load(ExerciseSessionRecord::class) { exerciseSessionRecord = it }
//            load(RespiratoryRateRecord::class) { respiratoryRateRecord = it }
//            load(WeightRecord::class) { weightRecord = it }
//            load(BodyFatRecord::class) { bodyFatRecord = it }
//            load(DistanceRecord::class) { distanceRecord = it }
//            load(OxygenSaturationRecord::class) { oxygenSaturationRecord = it }
//            load(BasalMetabolicRateRecord::class) { basalMetabolicRateRecord = it }
//            load(BloodPressureRecord::class) { bloodPressureRecord = it }
//            // ------------------------------
//            // 7) Device origin detection
//            // ------------------------------
//            val devicePackage =
//                stepsRecord?.firstOrNull()?.metadata?.dataOrigin?.packageName ?: "unknown"
//            val deviceManufacturer =
//                stepsRecord?.firstOrNull()?.metadata?.device?.manufacturer ?: devicePackage
//            SharedPreferenceManager.getInstance(context?.let { it }).saveDeviceName(deviceManufacturer)
//            // ------------------------------
//            // 8) Save updated sync time
//            // ------------------------------
//            if (foundNewData && latestModified != null) {
//                SharedPreferenceManager.getInstance(context?.let { it }).saveMoveRightSyncTime(latestModified.toString())
//                Log.d("HealthSync", "Updated lastSync = $latestModified")
//            } else {
//                Log.d("HealthSync", "No new data. Sync time unchanged")
//            }
//            // ------------------------------
//            // 9) Push to your server
//            // ------------------------------
//            when (devicePackage) {
//                "com.google.android.apps.fitness" -> storeHealthData()
//                "com.sec.android.app.shealth",
//                "com.samsung.android.wear.shealth" -> storeSamsungHealthData()
//                else -> storeHealthData()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            hideLoaderSafe()
//        }
//    }

    private fun <T : Record> hasPermission(
        granted: Set<String>,
        type: KClass<T>
    ): Boolean {
        return HealthPermission.getReadPermission(type) in granted
    }

    private suspend fun <T : Record> fetchChunked(
        type: KClass<T>,
        start: Instant,
        end: Instant,
        chunks: Int
    ): List<T> {
        val output = mutableListOf<T>()
        val total = Duration.between(start, end)
        val chunk = total.dividedBy(chunks.toLong())

        var cursor = start
        repeat(chunks) { i ->
            val next = if (i == chunks - 1) end else cursor.plus(chunk)
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(type, TimeRangeFilter.between(cursor, next))
            )
            output.addAll(response.records)
            cursor = next
        }
        return output
    }

    private fun showLoaderSafe() {
        if (isAdded && view != null) {
            requireActivity().runOnUiThread { showLoader(requireView()) }
        }
    }

    private fun hideLoaderSafe() {
        if (isAdded && view != null) {
            requireActivity().runOnUiThread { dismissLoader(requireView()) }
        }
    }

    private fun fetchUserWorkouts() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userid = SharedPreferenceManager.getInstance(requireActivity()).userId
                val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val response = ApiClient.apiServiceFastApi.getNewUserWorkouts(
                    userId = userid,
                    start_date = currentDate,
                    end_date = currentDate,
                    page = 1,
                    limit = 10
                )
                if (response.isSuccessful) {
                    val workouts = response.body()
                    workouts?.let {
                        // Define default instances for heart rate data
                        val defaultHeartRateZones = HeartRateZones(
                            lightZone = emptyList(),
                            fatBurnZone = emptyList(),
                            cardioZone = emptyList(),
                            peakZone = emptyList()
                        )
                        val defaultHeartRateZoneMinutes = HeartRateZoneMinutes(
                            belowLight = 0,
                            lightZone = 0,
                            fatBurnZone = 0,
                            cardioZone = 0,
                            peakZone = 0
                        )
                        val defaultHeartRateZonePercentages = HeartRateZonePercentages(
                            belowLight = 0f,
                            lightZone = 0f,
                            fatBurnZone = 0f,
                            cardioZone = 0f,
                            peakZone = 0f
                        )
                        // Map syncedWorkouts to CardItem objects
                        val syncedCardItems = it.syncedWorkouts.map { workout ->
                            val durationDouble = workout.duration.toDoubleOrNull() ?: 0.0
                            val durationMinutes = durationDouble.toInt()
                            val hours = durationMinutes / 60
                            val minutes = durationMinutes % 60
                            val durationText = if (hours > 0) "$hours hr ${minutes.toString().padStart(2, '0')} mins" else "$minutes mins"
                            val caloriesText = "${workout.caloriesBurned} cal"
                            val avgHeartRate = if (workout.heartRateData.isNotEmpty()) {
                                val totalHeartRate = workout.heartRateData.sumOf { it.heartRate }
                                val count = workout.heartRateData.size
                                "${(totalHeartRate / count).toInt()} bpm"
                            } else "N/A"
                            workout.heartRateData.forEach { heartRateData ->
                                heartRateData.trendData.addAll(listOf(listOf(110, 112, 115, 118, 120, 122, 125).toString()))
                            }
                            CardItem(
                                title = workout.workoutType,
                                duration = durationText,
                                caloriesBurned = caloriesText,
                                icon = "",
                                avgHeartRate = avgHeartRate,
                                heartRateData = workout.heartRateData,
                                heartRateZones = workout.heartRateZones ?: defaultHeartRateZones,
                                heartRateZoneMinutes = workout.heartRateZoneMinutes ?: defaultHeartRateZoneMinutes,
                                heartRateZonePercentages = workout.heartRateZonePercentages ?: defaultHeartRateZonePercentages,
                                isSynced = true
                            )
                        }

                        // Map unsyncedWorkouts to CardItem objects
                        val unsyncedCardItems = it.unsyncedWorkouts.map { workout ->
                            val durationDouble = workout.duration.toDoubleOrNull() ?: 0.0
                            val durationMinutes = durationDouble.toInt()
                            val hours = durationMinutes / 60
                            val minutes = durationMinutes % 60
                            val durationText = if (hours > 0) "$hours hr ${minutes.toString().padStart(2, '0')} mins" else "$minutes mins"
                            val caloriesDouble = workout.caloriesBurned.toDoubleOrNull() ?: 0.0
                            val caloriesText = "${caloriesDouble.toInt()} cal"
                            CardItem(
                                title = workout.workoutType,
                                duration = durationText,
                                caloriesBurned = caloriesText,
                                avgHeartRate = "N/A",
                                icon = workout.icon,
                                heartRateData = emptyList(),
                                heartRateZones = workout.heartRateZones ?: defaultHeartRateZones,
                                heartRateZoneMinutes = workout.heartRateZoneMinutes ?: defaultHeartRateZoneMinutes,
                                heartRateZonePercentages = workout.heartRateZonePercentages ?: defaultHeartRateZonePercentages,
                                isSynced = false
                            )
                        }

                        // Combine synced and unsynced CardItems
                        val allCardItems = syncedCardItems + unsyncedCardItems

                        if (isAdded && view != null) {
                            requireActivity().runOnUiThread {
                                view?.let {
                                    dismissLoader(it)
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            if (allCardItems.isNotEmpty()) {
                                val hasValidHeartRate = syncedCardItems.any { item ->
                                    item.heartRateData.any { it.heartRate > 0 }
                                }
                                workoutImageIcon.visibility = if (hasValidHeartRate) View.VISIBLE else View.GONE
                                dataFilledworkout.visibility = View.VISIBLE
                                nodataWorkout.visibility = View.GONE
                                isSyncData = true
//                                step_forward_icon.visibility = View.VISIBLE
//                                stepNoDataLayout.visibility = View.GONE
//                                stepWithDataCardLayout.visibility = View.VISIBLE
//                                stepLineGraphView.clear()
//                                stepLineGraphView.addDataSet( FloatArray(24) { 0f }, 0xFFFD6967.toInt()) // Red
//                                stepLineGraphView.addDataSet( FloatArray(24) { 0f }, 0xFF707070.toInt()) // Gray
//                                stepLineGraphView.addDataSet( FloatArray(24) { 0f }, 0xFF03B27B.toInt()) // Green (dotted)
//                                stepLineGraphView.invalidate()
                                val adapter = CarouselAdapter(allCardItems) { cardItem, position ->
                                    val fragment = WorkoutAnalyticsFragment().apply {
                                        arguments = Bundle().apply { putParcelable("cardItem", cardItem) }
                                    }
                                    requireActivity().supportFragmentManager.beginTransaction()
                                        .replace(R.id.flFragment, fragment, "workoutAnalysisFragment")
                                        .addToBackStack(null)
                                        .commit()
                                }
                                carouselViewPager.adapter = adapter
                                addDotsIndicator(allCardItems.size)
                                carouselViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                                    override fun onPageSelected(position: Int) {
                                        updateDots(position)
                                    }
                                })
                                carouselViewPager.setPageTransformer { page, position ->
                                    val offset = abs(position)
                                    page.scaleY = 1 - (offset * 0.1f)
                                }
                                // Set click listener for workoutImageIcon to navigate to WorkoutAnalyticsFragment with current CardItem
                                workoutImageIcon.setOnClickListener {
                                    val currentPosition = carouselViewPager.currentItem
                                    if (currentPosition >= 0 && currentPosition < allCardItems.size) {
                                        val selectedCardItem = allCardItems[currentPosition]
                                        val fragment = WorkoutAnalyticsFragment().apply {
                                            arguments = Bundle().apply { putParcelable("cardItem", selectedCardItem) }
                                        }
                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.flFragment, fragment, "workoutAnalysisFragment")
                                            .addToBackStack(null)
                                            .commit()
                                    } else {
                                        Toast.makeText(context?.let { it }, "No workout selected", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                nodataWorkout.visibility = View.VISIBLE
                                dataFilledworkout.visibility = View.GONE
                               // Toast.makeText(requireContext(), "No workout data available", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } ?: withContext(Dispatchers.Main) {
                        if (isAdded  && view != null){
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                        nodataWorkout.visibility = View.VISIBLE
                        dataFilledworkout.visibility = View.GONE
                        Toast.makeText(context?.let { it }, "No workout data received", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    withContext(Dispatchers.Main) {
                        nodataWorkout.visibility = View.VISIBLE
                        dataFilledworkout.visibility = View.GONE
                        Toast.makeText(context?.let { it }, "Error: ${response.code()} - ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
                withContext(Dispatchers.Main) {
                    nodataWorkout.visibility = View.VISIBLE
                    dataFilledworkout.visibility = View.GONE
                    Toast.makeText(context?.let { it }, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun storeHealthData() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val timeZone = ZoneId.systemDefault().id
                val userid = SharedPreferenceManager.getInstance(requireActivity()).userId
                var activeEnergyBurned : List<EnergyBurnedRequest>? = null
                if (activeCalorieBurnedRecord!!.isNotEmpty()){
                    activeEnergyBurned = activeCalorieBurnedRecord?.mapNotNull { record ->
                        if (record.energy.inKilocalories > 0) {
                            EnergyBurnedRequest(
                                start_datetime = convertToTargetFormat(record.startTime.toString()),
                                end_datetime = convertToTargetFormat(record.endTime.toString()),
                                record_type = "ActiveEnergyBurned",
                                unit = "kcal",
                                value = record.energy.inKilocalories.toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    } ?: emptyList()
                }else{
                    activeEnergyBurned = totalCaloriesBurnedRecord?.mapNotNull { record ->
                        if (record.energy.inKilocalories > 0) {
                            EnergyBurnedRequest(
                                start_datetime = convertToTargetFormat(record.startTime.toString()),
                                end_datetime = convertToTargetFormat(record.endTime.toString()),
                                record_type = "ActiveEnergyBurned",
                                unit = "kcal",
                                value = record.energy.inKilocalories.toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    } ?: emptyList()
                }
                val basalEnergyBurned = basalMetabolicRateRecord?.map { record ->
                    EnergyBurnedRequest(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BasalMetabolic",
                        unit = "power",
                        value = record.basalMetabolicRate.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val distanceWalkingRunning = distanceRecord?.mapNotNull { record ->
                    if (record.distance.inKilometers > 0) {
                        val km = record.distance.inKilometers
                        val safeKm = if (km.isFinite()) km else 0.0
                        Distance(
                            start_datetime = convertToTargetFormat(record.startTime.toString()),
                            end_datetime = convertToTargetFormat(record.endTime.toString()),
                            record_type = "DistanceWalkingRunning",
                            unit = "km",
                            value = String.format(Locale.US, "%.2f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val stepCount = stepsRecord?.mapNotNull { record ->
                    if (record.count > 0) {
                        StepCountRequest(
                            start_datetime = convertToTargetFormat(record.startTime.toString()),
                            end_datetime = convertToTargetFormat(record.endTime.toString()),
                            record_type = "StepCount",
                            unit = "count",
                            value = record.count.toString(),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val heartRate = heartRateRecord?.flatMap { record ->
                    record.samples.mapNotNull { sample ->
                        if (sample.beatsPerMinute > 0) {
                            HeartRateRequest(
                                start_datetime = convertToTargetFormat(record.startTime.toString()),
                                end_datetime = convertToTargetFormat(record.endTime.toString()),
                                record_type = "HeartRate",
                                unit = "bpm",
                                value = sample.beatsPerMinute.toInt().toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    }
                } ?: emptyList()
                val heartRateVariability = heartRateVariability?.map { record ->
                    HeartRateVariabilityRequest(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "HeartRateVariability",
                        unit = "double",
                        value = record.heartRateVariabilityMillis.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val restingHeartRate = restingHeartRecord?.map { record ->
                    HeartRateRequest(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "RestingHeartRate",
                        unit = "bpm",
                        value = record.beatsPerMinute.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val respiratoryRate = respiratoryRateRecord?.mapNotNull { record ->
                    if (record.rate > 0) {
                        val rate = record.rate
                        val safeRate = if (rate.isFinite()) rate else 0.0
                        RespiratoryRate(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "RespiratoryRate",
                            unit = "breaths/min",
                            value = String.format(Locale.US,"%.1f", safeRate),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val oxygenSaturation = oxygenSaturationRecord?.mapNotNull { record ->
                    if (record.percentage.value > 0) {
                        val km = record.percentage.value
                        val safeKm = if (km.isFinite()) km else 0.0
                        OxygenSaturation(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "OxygenSaturation",
                            unit = "%",
                            value = String.format(Locale.US,"%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val bloodPressureSystolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BloodPressureSystolic",
                        unit = "millimeterOfMercury",
                        value = record.systolic.inMillimetersOfMercury.toString(),
                        source_name =record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val bloodPressureDiastolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BloodPressureDiastolic",
                        unit = "millimeterOfMercury",
                        value = record.diastolic.inMillimetersOfMercury.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val bodyMass = weightRecord?.mapNotNull { record ->
                    if (record.weight.inKilograms > 0) {
                        val km = record.weight.inKilograms
                        val safeKm = if (km.isFinite()) km else 0.0
                        BodyMass(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "BodyMass",
                            unit = "kg",
                            value = String.format(Locale.US,"%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val bodyFatPercentage = bodyFatRecord?.mapNotNull { record ->
                    val km = record.percentage
                    val safeKm = if (km.value.isFinite()) km else 0.0
                    BodyFatPercentage(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BodyFat",
                        unit = "percentage",
                        value =String.format(Locale.US,"%.1f", safeKm),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val sleepStage = sleepSessionRecord?.flatMap { record ->
                    if (record.stages.isEmpty()) {
                        // No stages → return default "sleep"
                        listOf(
                            SleepStageJson(
                                start_datetime = convertToTargetFormat(record.startTime.toString()),
                                end_datetime = convertToTargetFormat(record.endTime.toString()),
                                record_type = "Asleep",
                                unit = "stage",
                                value = "Asleep",
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        )
                    } else {
                        // Map actual stages
                        record.stages.mapNotNull { stage ->
                            val stageValue = when (stage.stage) {
                                SleepSessionRecord.STAGE_TYPE_DEEP -> "Deep Sleep"
                                SleepSessionRecord.STAGE_TYPE_LIGHT -> "Light Sleep"
                                SleepSessionRecord.STAGE_TYPE_REM -> "REM Sleep"
                                SleepSessionRecord.STAGE_TYPE_AWAKE -> "Awake"
                                else -> null
                            }
                            stageValue?.let {
                                SleepStageJson(
                                    start_datetime = convertToTargetFormat(stage.startTime.toString()),
                                    end_datetime = convertToTargetFormat(stage.endTime.toString()),
                                    record_type = it,
                                    unit = "sleep_stage",
                                    value = it,
                                    source_name = record.metadata.dataOrigin.packageName
                                )
                            }
                        }
                    }
                } ?: emptyList()
                val workout = exerciseSessionRecord?.mapNotNull { record ->
                    val workoutType = when (record.exerciseType) {
                        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "Running"
                        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "Walking"
                        ExerciseSessionRecord.EXERCISE_TYPE_GYMNASTICS -> "Gym"
                        ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT -> "Other Workout"
                        ExerciseSessionRecord.EXERCISE_TYPE_MARTIAL_ARTS -> "Martial Arts"
                        ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "Biking"
                        ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY -> "Biking Stationary"
                        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "Cycling"
                        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> "Swimming"
                        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "Strength Training"
                        ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "Yoga"
                        ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> "HIIT"
                        ExerciseSessionRecord.EXERCISE_TYPE_BADMINTON -> "Badminton"
                        ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL -> "Basketball"
                        ExerciseSessionRecord.EXERCISE_TYPE_BASEBALL -> "Baseball"
                        else -> "Other"
                    }
                    val distance = record.metadata.dataOrigin?.let { 5.0 } ?: 0.0
                    val safeDistance = if (distance.isFinite()) distance else 0.0
                    WorkoutRequest(
                        start_datetime = convertToTargetFormat(record.startTime.toString()),
                        end_datetime = convertToTargetFormat(record.endTime.toString()),
                        source_name = record.metadata.dataOrigin.packageName ,
                        record_type = "Workout",
                        workout_type = workoutType,
                        duration = ((record.endTime.toEpochMilli() - record.startTime.toEpochMilli()) / 1000 / 60).toString(),
                        calories_burned = "",
                        distance = String.format(Locale.US, "%.1f", safeDistance),
                        duration_unit = "minutes",
                        calories_unit = "kcal",
                        distance_unit = "km"
                    )
                } ?: emptyList()
                val request = StoreHealthDataRequest(
                    user_id = userid,
                    source = "android",
                    active_energy_burned = activeEnergyBurned,
                    basal_energy_burned = basalEnergyBurned,
                    distance_walking_running = distanceWalkingRunning,
                    step_count = stepCount,
                    heart_rate = heartRate,
                    heart_rate_variability_SDNN = heartRateVariability,
                    resting_heart_rate = restingHeartRate,
                    respiratory_rate = respiratoryRate,
                    oxygen_saturation = oxygenSaturation,
                    blood_pressure_systolic = bloodPressureSystolic,
                    blood_pressure_diastolic = bloodPressureDiastolic,
                    body_mass = bodyMass,
                    body_fat_percentage = bodyFatPercentage,
                    sleep_stage = sleepStage,
                    workout = workout,
                    time_zone = timeZone
                )
                val gson = Gson()
                val allRecords = mutableListOf<Any>()
                allRecords.addAll(activeEnergyBurned)
                allRecords.addAll(basalEnergyBurned)
                allRecords.addAll(distanceWalkingRunning)
                allRecords.addAll(stepCount)
                allRecords.addAll(heartRate)
                allRecords.addAll(heartRateVariability)
                allRecords.addAll(restingHeartRate)
                allRecords.addAll(respiratoryRate)
                allRecords.addAll(oxygenSaturation)
                allRecords.addAll(bloodPressureSystolic)
                allRecords.addAll(bloodPressureDiastolic)
                allRecords.addAll(bodyMass)
                allRecords.addAll(bodyFatPercentage)
                allRecords.addAll(sleepStage)
                allRecords.addAll(workout)

                // Chunk upload variables
                var currentBatch = mutableListOf<Any>()
                var currentSize = 0
                val maxSize = 10 * 1024 * 1024 // 10MB

                suspend fun uploadBatch(batch: List<Any>) {
                    if (batch.isEmpty()) return
                    val req = StoreHealthDataRequest(
                        user_id = userid,
                        source = "android",
                        active_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>().filter { it.record_type == "ActiveEnergyBurned" },
                        basal_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>().filter { it.record_type == "BasalMetabolic" },
                        distance_walking_running = batch.filterIsInstance<Distance>(),
                        step_count = batch.filterIsInstance<StepCountRequest>(),
                        heart_rate = batch.filterIsInstance<HeartRateRequest>().filter { it.record_type == "HeartRate" },
                        heart_rate_variability_SDNN = batch.filterIsInstance<HeartRateVariabilityRequest>(),
                        resting_heart_rate = batch.filterIsInstance<HeartRateRequest>().filter { it.record_type == "RestingHeartRate" },
                        respiratory_rate = batch.filterIsInstance<RespiratoryRate>(),
                        oxygen_saturation = batch.filterIsInstance<OxygenSaturation>(),
                        blood_pressure_systolic = batch.filterIsInstance<BloodPressure>().filter { it.record_type == "BloodPressureSystolic" },
                        blood_pressure_diastolic = batch.filterIsInstance<BloodPressure>().filter { it.record_type == "BloodPressureDiastolic" },
                        body_mass = batch.filterIsInstance<BodyMass>(),
                        body_fat_percentage = batch.filterIsInstance<BodyFatPercentage>(),
                        sleep_stage = batch.filterIsInstance<SleepStageJson>(),
                        workout = batch.filterIsInstance<WorkoutRequest>(),
                        time_zone = timeZone
                    )
                    val response = ApiClient.apiServiceFastApi.storeHealthData(req)
                    if (!response.isSuccessful) {
                        throw Exception("Batch upload failed with code: ${response.code()}")
                    }
                }
                // Loop through all records and split into chunks
                for (record in allRecords) {
                    val json = gson.toJson(record)
                    val size = json.toByteArray().size
                    if (currentSize + size > maxSize) {
                        uploadBatch(currentBatch)
                        currentBatch = mutableListOf()
                        currentSize = 0
                    }
                    currentBatch.add(record)
                    currentSize += size
                }
                // Upload remaining batch
                if (currentBatch.isNotEmpty()) {
                    uploadBatch(currentBatch)
                }
                // ✅ Done, update sync time
                withContext(Dispatchers.Main) {
                    if (isAdded && view != null) dismissLoader(requireView())
//                    context?.let {
//                        SharedPreferenceManager.getInstance(it).saveMoveRightSyncTime(Instant.now().toString())
//                    }
                    isRepeat = true
                    fetchMoveLandingAfterRefresh(recyclerView, adapter)
                    fetchUserWorkouts()
                    onSyncComplete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    context?.let {
                        Toast.makeText(it, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    isRepeat = true
                    if (isAdded && view != null) dismissLoader(requireView())
                    onSyncComplete()
                }
            }
        }
    }

    private fun storeSamsungHealthData() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val timeZone = ZoneId.systemDefault().id
                val userid = SharedPreferenceManager.getInstance(requireActivity()).userId
                var activeEnergyBurned : List<EnergyBurnedRequest>? = null
                if (activeCalorieBurnedRecord!!.isNotEmpty()){
                    activeEnergyBurned = activeCalorieBurnedRecord?.mapNotNull { record ->
                        if (record.energy.inKilocalories > 0) {
                            EnergyBurnedRequest(
                                start_datetime = convertToSamsungFormat(record.startTime.toString()),
                                end_datetime = convertToSamsungFormat(record.endTime.toString()),
                                record_type = "ActiveEnergyBurned",
                                unit = "kcal",
                                value = record.energy.inKilocalories.toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    } ?: emptyList()
                }else{
                    activeEnergyBurned = totalCaloriesBurnedRecord?.mapNotNull { record ->
                        if (record.energy.inKilocalories > 0) {
                            EnergyBurnedRequest(
                                start_datetime = convertToSamsungFormat(record.startTime.toString()),
                                end_datetime = convertToSamsungFormat(record.endTime.toString()),
                                record_type = "ActiveEnergyBurned",
                                unit = "kcal",
                                value = record.energy.inKilocalories.toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    } ?: emptyList()
                }
                val basalEnergyBurned = basalMetabolicRateRecord?.map { record ->
                    EnergyBurnedRequest(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BasalMetabolic",
                        unit = "power",
                        value = record.basalMetabolicRate.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val distanceWalkingRunning = distanceRecord?.mapNotNull { record ->
                    if (record.distance.inKilometers > 0) {
                        val km = record.distance.inKilometers
                        val safeKm = if (km.isFinite()) km else 0.0
                        Distance(
                            start_datetime = convertToSamsungFormat(record.startTime.toString()),
                            end_datetime = convertToSamsungFormat(record.endTime.toString()),
                            record_type = "DistanceWalkingRunning",
                            unit = "km",
                            value = String.format(Locale.US,"%.2f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val stepCount = stepsRecord?.mapNotNull { record ->
                    if (record.count > 0) {
                        StepCountRequest(
                            start_datetime = convertToSamsungFormat(record.startTime.toString()),
                            end_datetime = convertToSamsungFormat(record.endTime.toString()),
                            record_type = "StepCount",
                            unit = "count",
                            value = record.count.toString(),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val heartRate = heartRateRecord?.flatMap { record ->
                    record.samples.mapNotNull { sample ->
                        if (sample.beatsPerMinute > 0) {
                            HeartRateRequest(
                                start_datetime = convertToSamsungFormat(record.startTime.toString()),
                                end_datetime = convertToSamsungFormat(record.endTime.toString()),
                                record_type = "HeartRate",
                                unit = "bpm",
                                value = sample.beatsPerMinute.toInt().toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    }
                } ?: emptyList()
                val heartRateVariability = heartRateVariability?.map { record ->
                    HeartRateVariabilityRequest(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "HeartRateVariability",
                        unit = "double",
                        value = record.heartRateVariabilityMillis.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val restingHeartRate = restingHeartRecord?.map { record ->
                    HeartRateRequest(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "RestingHeartRate",
                        unit = "bpm",
                        value = record.beatsPerMinute.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val respiratoryRate = respiratoryRateRecord?.mapNotNull { record ->
                    if (record.rate > 0) {
                        val km = record.rate
                        val safeKm = if (km.isFinite()) km else 0.0
                        RespiratoryRate(
                            start_datetime = convertToSamsungFormat(record.time.toString()),
                            end_datetime = convertToSamsungFormat(record.time.toString()),
                            record_type = "RespiratoryRate",
                            unit = "breaths/min",
                            value = String.format(Locale.US,"%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val oxygenSaturation = oxygenSaturationRecord?.mapNotNull { record ->
                    if (record.percentage.value > 0) {
                        val km = record.percentage.value
                        val safeKm = if (km.isFinite()) km else 0.0
                        OxygenSaturation(
                            start_datetime = convertToSamsungFormat(record.time.toString()),
                            end_datetime = convertToSamsungFormat(record.time.toString()),
                            record_type = "OxygenSaturation",
                            unit = "%",
                            value = String.format(Locale.US,"%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val bloodPressureSystolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BloodPressureSystolic",
                        unit = "millimeterOfMercury",
                        value = record.systolic.inMillimetersOfMercury.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val bloodPressureDiastolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BloodPressureDiastolic",
                        unit = "millimeterOfMercury",
                        value = record.diastolic.inMillimetersOfMercury.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val bodyMass = weightRecord?.mapNotNull { record ->
                    if (record.weight.inKilograms > 0) {
                        val km = record.weight.inKilograms
                        val safeKm = if (km.isFinite()) km else 0.0
                        BodyMass(
                            start_datetime = convertToSamsungFormat(record.time.toString()),
                            end_datetime = convertToSamsungFormat(record.time.toString()),
                            record_type = "BodyMass",
                            unit = "kg",
                            value = String.format(Locale.US,"%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val bodyFatPercentage = bodyFatRecord?.mapNotNull { record ->
                    val km =  record.percentage.value
                    val safeKm = if (km.isFinite()) km else 0.0
                    BodyFatPercentage(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BodyFat",
                        unit = "percentage",
                        value = String.format(Locale.US,"%.1f", safeKm),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val sleepStage = sleepSessionRecord?.flatMap { record ->
                    if (record.stages.isEmpty()) {
                        // No stages → return default "sleep"
                        listOf(
                            SleepStageJson(
                                start_datetime = convertToSamsungFormat(record.startTime.toString()),
                                end_datetime = convertToSamsungFormat(record.endTime.toString()),
                                record_type = "Asleep",
                                unit = "stage",
                                value = "Asleep",
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        )
                    } else {
                        // Map actual stages
                        record.stages.mapNotNull { stage ->
                            val stageValue = when (stage.stage) {
                                SleepSessionRecord.STAGE_TYPE_DEEP -> "Deep Sleep"
                                SleepSessionRecord.STAGE_TYPE_LIGHT -> "Light Sleep"
                                SleepSessionRecord.STAGE_TYPE_REM -> "REM Sleep"
                                SleepSessionRecord.STAGE_TYPE_AWAKE -> "Awake"
                                else -> null
                            }
                            stageValue?.let {
                                SleepStageJson(
                                    start_datetime = convertToSamsungFormat(stage.startTime.toString()),
                                    end_datetime = convertToSamsungFormat(stage.endTime.toString()),
                                    record_type = it,
                                    unit = "sleep_stage",
                                    value = it,
                                    source_name = record.metadata.dataOrigin.packageName
                                )
                            }
                        }
                    }
                } ?: emptyList()
                val workout = exerciseSessionRecord?.mapNotNull { record ->
                    val workoutType = when (record.exerciseType) {
                        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "Running"
                        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "Walking"
                        ExerciseSessionRecord.EXERCISE_TYPE_GYMNASTICS -> "Gym"
                        ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT -> "Other Workout"
                        ExerciseSessionRecord.EXERCISE_TYPE_MARTIAL_ARTS -> "Martial Arts"
                        ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "Biking"
                        ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY -> "Biking Stationary"
                        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "Cycling"
                        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> "Swimming"
                        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "Strength Training"
                        ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "Yoga"
                        ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> "HIIT"
                        ExerciseSessionRecord.EXERCISE_TYPE_BADMINTON -> "Badminton"
                        ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL -> "Basketball"
                        ExerciseSessionRecord.EXERCISE_TYPE_BASEBALL -> "Baseball"
                        else -> "Other"
                    }
                    val distance = record.metadata.dataOrigin?.let { 5.0 } ?: 0.0
                    val safeDistance = if (distance.isFinite()) distance else 0.0
                        WorkoutRequest(
                            start_datetime = convertToSamsungFormat(record.startTime.toString()),
                            end_datetime = convertToSamsungFormat(record.endTime.toString()),
                            source_name = record.metadata.dataOrigin.packageName,
                            record_type = "Workout",
                            workout_type = workoutType,
                            duration = ((record.endTime.toEpochMilli() - record.startTime.toEpochMilli()) / 1000 / 60).toString(),
                            calories_burned = "",
                            distance = String.format(Locale.US, "%.1f", safeDistance),
                            duration_unit = "minutes",
                            calories_unit = "kcal",
                            distance_unit = "km"
                        )
                } ?: emptyList()
                val request = StoreHealthDataRequest(
                    user_id = userid,
                    source = "android",
                    active_energy_burned = activeEnergyBurned,
                    basal_energy_burned = basalEnergyBurned,
                    distance_walking_running = distanceWalkingRunning,
                    step_count = stepCount,
                    heart_rate = heartRate,
                    heart_rate_variability_SDNN = heartRateVariability,
                    resting_heart_rate = restingHeartRate,
                    respiratory_rate = respiratoryRate,
                    oxygen_saturation = oxygenSaturation,
                    blood_pressure_systolic = bloodPressureSystolic,
                    blood_pressure_diastolic = bloodPressureDiastolic,
                    body_mass = bodyMass,
                    body_fat_percentage = bodyFatPercentage,
                    sleep_stage = sleepStage,
                    workout = workout,
                    time_zone = timeZone
                )
                val gson = Gson()
                val allRecords = mutableListOf<Any>()
                allRecords.addAll(activeEnergyBurned)
                allRecords.addAll(basalEnergyBurned)
                allRecords.addAll(distanceWalkingRunning)
                allRecords.addAll(stepCount)
                allRecords.addAll(heartRate)
                allRecords.addAll(heartRateVariability)
                allRecords.addAll(restingHeartRate)
                allRecords.addAll(respiratoryRate)
                allRecords.addAll(oxygenSaturation)
                allRecords.addAll(bloodPressureSystolic)
                allRecords.addAll(bloodPressureDiastolic)
                allRecords.addAll(bodyMass)
                allRecords.addAll(bodyFatPercentage)
                allRecords.addAll(sleepStage)
                allRecords.addAll(workout)

                // Chunk upload variables
                var currentBatch = mutableListOf<Any>()
                var currentSize = 0
                val maxSize = 10 * 1024 * 1024 // 10MB

                suspend fun uploadBatch(batch: List<Any>) {
                    if (batch.isEmpty()) return
                    val req = StoreHealthDataRequest(
                        user_id = userid,
                        source = "android",
                        active_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>().filter { it.record_type == "ActiveEnergyBurned" },
                        basal_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>().filter { it.record_type == "BasalMetabolic" },
                        distance_walking_running = batch.filterIsInstance<Distance>(),
                        step_count = batch.filterIsInstance<StepCountRequest>(),
                        heart_rate = batch.filterIsInstance<HeartRateRequest>().filter { it.record_type == "HeartRate" },
                        heart_rate_variability_SDNN = batch.filterIsInstance<HeartRateVariabilityRequest>(),
                        resting_heart_rate = batch.filterIsInstance<HeartRateRequest>().filter { it.record_type == "RestingHeartRate" },
                        respiratory_rate = batch.filterIsInstance<RespiratoryRate>(),
                        oxygen_saturation = batch.filterIsInstance<OxygenSaturation>(),
                        blood_pressure_systolic = batch.filterIsInstance<BloodPressure>().filter { it.record_type == "BloodPressureSystolic" },
                        blood_pressure_diastolic = batch.filterIsInstance<BloodPressure>().filter { it.record_type == "BloodPressureDiastolic" },
                        body_mass = batch.filterIsInstance<BodyMass>(),
                        body_fat_percentage = batch.filterIsInstance<BodyFatPercentage>(),
                        sleep_stage = batch.filterIsInstance<SleepStageJson>(),
                        workout = batch.filterIsInstance<WorkoutRequest>(),
                        time_zone = timeZone
                    )
                    val response = ApiClient.apiServiceFastApi.storeHealthData(req)
                    if (!response.isSuccessful) {
                        throw Exception("Batch upload failed with code: ${response.code()}")
                    }
                }
                // Loop through all records and split into chunks
                for (record in allRecords) {
                    val json = gson.toJson(record)
                    val size = json.toByteArray().size
                    if (currentSize + size > maxSize) {
                        uploadBatch(currentBatch)
                        currentBatch = mutableListOf()
                        currentSize = 0
                    }
                    currentBatch.add(record)
                    currentSize += size
                }
                // Upload remaining batch
                if (currentBatch.isNotEmpty()) {
                    uploadBatch(currentBatch)
                }
                // ✅ Done, update sync time
                withContext(Dispatchers.Main) {
                    if (isAdded && view != null) dismissLoader(requireView())
//                    context?.let {
//                        SharedPreferenceManager.getInstance(it).saveMoveRightSyncTime(Instant.now().toString())
//                    }
                    isRepeat = true
                    fetchMoveLandingAfterRefresh(recyclerView, adapter)
                    fetchUserWorkouts()
                    onSyncComplete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    context?.let {
                        Toast.makeText(it, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                   isRepeat = true
                    if (isAdded && view != null) dismissLoader(requireView())
                    onSyncComplete()
                }
            }
        }
    }

    private fun showCompactSyncView() {
        // isSyncing.value = true
        compactSyncIndicator.apply {
            visibility = View.VISIBLE
            alpha = 0f
            scaleX = 0.6f
            scaleY = 0.6f
            animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(400)
                .setInterpolator(OvershootInterpolator(1.5f))
                .start()
        }
        startHeartPulse(compactHeartIcon, false)
    }

    fun startHeartPulse(target: ImageView, isFull: Boolean) {
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            target,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f)
        ).apply {
            duration = 800
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        if (isFull) {
            fullHeartAnimator?.cancel()
            fullHeartAnimator = animator
        } else {
            compactHeartAnimator?.cancel()
            compactHeartAnimator = animator
        }
        animator.start()
    }

    private fun onSyncComplete() {
        // 1. Define colors for Success State
        //isSyncing.value = false
        val ctx = context ?: return
        val colorGreen = ContextCompat.getColor(ctx, R.color.color_green)
        val colorStateList = ColorStateList.valueOf(colorGreen)
        val colorRed = ContextCompat.getColor(ctx, R.color.red)
        val colorStateListRed = ColorStateList.valueOf(colorRed)

        // --- Compact View Completion ---
        compactHeartAnimator?.cancel()

        compactSyncIndicator.apply {
            visibility = View.VISIBLE
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
        }

        compactRotatingArc.visibility = View.GONE
        compactHeartIcon.apply {
            imageTintList = colorStateList
            scaleX = 1f
            scaleY = 1f
        }

        // 3. Auto-hide with Shrink animation after 2.5 seconds
        binding.root.postDelayed({
            if (!isAdded || view == null) return@postDelayed

            compactSyncIndicator.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setDuration(400)
                .withEndAction {
                    compactSyncIndicator.visibility = View.GONE
                    compactRotatingArc.visibility = View.VISIBLE

                    compactHeartIcon.apply {
                        imageTintList = colorStateListRed
                        scaleX = 0f
                        scaleY = 0f
                    }
                }
                .start()
        }, 2500)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchThinkRecomendedData() {

        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val call = ApiClient.apiService.fetchThinkRecomended(token, "HOME", "MOVE_RIGHT")
        call.enqueue(object : Callback<ThinkRecomendedResponse> {
            override fun onResponse(
                call: Call<ThinkRecomendedResponse>,
                response: Response<ThinkRecomendedResponse>
            ) {
                val ctx = context ?: return  // ✅ Stop if fragment is not attached
             //   val viewLifecycleOwner = viewLifecycleOwner
                if (response.isSuccessful) {
                    val data = response.body()?.data?.contentList ?: return
                    if (data.isNotEmpty()) {
                        recomendationAdapter = RecommendedAdapterSleep(ctx, data)
                        recomendationRecyclerView.apply {
                            layoutManager = LinearLayoutManager(ctx)
                            adapter = recomendationAdapter
                        }
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<ThinkRecomendedResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
            }
        })
    }

    private fun storeBodyFatData(): List<BodyFatPercentage>? {
        val jsonData: BodyFatJson = loadBodyFatJsonData()
        val fullList = jsonData.dataFatPoints
        var bodyFatList: List<BodyFatPercentage> = emptyList()
        bodyFatList = fullList.map {
            BodyFatPercentage(
                value = it.fitValue[0].value?.fpVal.toString(),
                record_type = "bodyFatPercentage",
                end_datetime = convertToTargetFormat(it.endTimeNanos.toString()),
                unit = it.dataTypeName.toString(),
                start_datetime = convertToTargetFormat(it.startTimeNanos.toString()),
                source_name = "google"
            )
        }
        return bodyFatList
    }

    private fun loadBodyFatJsonData(): BodyFatJson {
        val json = context?.assets?.open("assets/fit/alldata/derived_com.google.body.fat.percentage_com.goo.json")
            ?.bufferedReader().use { it?.readText() }
        return Gson().fromJson(json, object : TypeToken<BodyFatJson>() {}.type)
    }

    fun convertToSamsungFormat(input: String): String {
        val possibleFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ssX",         // ISO with timezone
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",     // ISO with milliseconds
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX", // ISO with nanoseconds
            "yyyy-MM-dd HH:mm:ss",            // Common DB format
            "yyyy/MM/dd HH:mm:ss",            // Slash format
            "dd-MM-yyyy HH:mm:ss",            // Day-Month-Year
            "MM/dd/yyyy HH:mm:ss",            // US format
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        )

        // Check if it's a nanosecond timestamp
        if (input.matches(Regex("^\\d{18,}$"))) {
            return try {
                val nanos = input.toLong()
                val seconds = nanos / 1_000_000_000
                val nanoAdjustment = (nanos % 1_000_000_000).toInt()
                val instant = Instant.ofEpochSecond(seconds, nanoAdjustment.toLong())
                val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
                targetFormatter.format(instant)
            } catch (e: Exception) {
                ""
            }
        }

        // Try known patterns
        for (pattern in possibleFormats) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern)

                return if (pattern.contains("X") || pattern.contains("'Z'")) {
                    // Pattern has timezone — parse as instant
                    val temporal = formatter.parse(input)
                    val instant = Instant.from(temporal)
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .withZone(ZoneOffset.UTC)
                        .format(instant)
                } else {
                    // No timezone info — treat as local time and convert to UTC
                    val localDateTime = LocalDateTime.parse(input, formatter)
                    val zonedDateTime = localDateTime.atZone(ZoneId.systemDefault())
                    val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC)
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .withZone(ZoneOffset.UTC)
                        .format(utcDateTime)
                }

            } catch (e: DateTimeParseException) {
                // Try next format
            }
        }

        return "" // Unable to parse
    }

    fun convertToTargetFormat(input: String): String {
        val possibleFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ssX",        // ISO with timezone
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",    // ISO with milliseconds
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX", // ISO with nanoseconds
            "yyyy-MM-dd HH:mm:ss",           // Common DB format
            "yyyy/MM/dd HH:mm:ss",           // Slash format
            "dd-MM-yyyy HH:mm:ss",           // Day-Month-Year
            "MM/dd/yyyy HH:mm:ss",            // US format
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        )

        // Check if it's a nanosecond timestamp (very large number)
        if (input.matches(Regex("^\\d{18,}$"))) {
            return try {
                val nanos = input.toLong()
                val seconds = nanos / 1_000_000_000
                val nanoAdjustment = (nanos % 1_000_000_000).toInt()
                val instant = Instant.ofEpochSecond(seconds, nanoAdjustment.toLong())
                val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
                return targetFormatter.format(instant)
            } catch (e: Exception) {
                ""
            }
        }

        // Try known patterns
        for (pattern in possibleFormats) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneOffset.UTC)
                val temporal = formatter.parse(input)
                val instant = Instant.from(temporal)
                val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
                return targetFormatter.format(instant)
            } catch (e: DateTimeParseException) {
                // Try next format
            }
        }

        return "" // Unable to parse
    }

    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = ("package:" + context.packageName).toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Unable to open settings", Toast.LENGTH_SHORT).show()
        }
    }

    fun isHealthConnectAvailable(context: Context): Boolean {
        val packageManager = context.packageManager
        return try {
            packageManager.getPackageInfo("com.google.android.apps.healthdata", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun installHealthConnect(context: Context) {
        val uri = "https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun requestHealthConnectPermission(activity: Activity) {
        val permissions = setOf(
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
            HealthPermission.getReadPermission(RespiratoryRateRecord::class),
            HealthPermission.getReadPermission(OxygenSaturationRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(WeightRecord::class),
            HealthPermission.getReadPermission(BodyFatRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        )
        val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
        val permissionLauncher = requireActivity().registerForActivityResult(requestPermissionActivityContract) { granted ->
            if (granted.containsAll(permissions)) {
                Toast.makeText(activity, "samsung Permissions Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Permissions Denied", Toast.LENGTH_SHORT).show()
            }
        }
        permissionLauncher.launch(permissions)
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }

    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    private fun fetchDataFromApi() {
        swipeRefreshLayout.isRefreshing = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val ctx = context ?: return@launch
                showCompactSyncView()
                val availabilityStatus = HealthConnectClient.getSdkStatus(ctx)
                if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
                    healthConnectClient = HealthConnectClient.getOrCreate(ctx)
                    requestPermissionsAndReadAllData()
                } else {
                    Toast.makeText(ctx,"Please install or update Health Connect", Toast.LENGTH_LONG).show()
                    onSyncComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // ❗ STOP refresh ONLY AFTER ALL WORK IS DONE
                swipeRefreshLayout.isRefreshing = false
                onSyncComplete()
            }
        }
    }
}
