package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.BundleCompat
import androidx.fragment.app.setFragmentResult
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BuildConfig
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.ActivityModel
import com.jetsynthesys.rightlife.ai_package.model.CalculateCaloriesRequest
import com.jetsynthesys.rightlife.ai_package.model.CalculateCaloriesResponse
import com.jetsynthesys.rightlife.ai_package.model.RoutineWorkoutDisplayModel
import com.jetsynthesys.rightlife.ai_package.model.WorkoutList
import com.jetsynthesys.rightlife.ai_package.model.WorkoutRoutineItem
import com.jetsynthesys.rightlife.ai_package.model.WorkoutSessionRecord
import com.jetsynthesys.rightlife.ai_package.model.request.CreateWorkoutRequest
import com.jetsynthesys.rightlife.ai_package.model.request.UpdateCaloriesRequest
import com.jetsynthesys.rightlife.ai_package.ui.moveright.customProgressBar.CustomProgressBar
import com.jetsynthesys.rightlife.databinding.FragmentAddWorkoutSearchBinding
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.shawnlin.numberpicker.NumberPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class AddWorkoutSearchFragment : BaseFragment<FragmentAddWorkoutSearchBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAddWorkoutSearchBinding
        get() = FragmentAddWorkoutSearchBinding::inflate

    // Handler at class level (only one)
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var intensityProgressBar: CustomProgressBar
    private lateinit var caloriesText: TextView
    private lateinit var addLog: LinearLayoutCompat
    private var isPickerChanged = false
    private var selectedTime: String = "1 hr 0 min"
    private var selectedIntensity: String = "Low"
    private var edit: String = ""
    private var calorieBurnedNew: Double = 0.0
    private var edit_routine: String = ""
    private var routineId: String = ""
    private var workout: WorkoutList? = null
    private var activityModel: ActivityModel? = null
    private var workoutModel: WorkoutRoutineItem? = null
    private var lastWorkoutRecord: WorkoutSessionRecord? = null
    private var editWorkoutRoutineItem: RoutineWorkoutDisplayModel? = null
    private var routine: String = ""
    private var editWorkoutRoutine: String = ""
    private var routineName: String = ""
    private var mSelectedDate: String = ""
    private var moduleName : String = ""
    var moduleIcon: String? = null
    private var currentToast: Toast? = null
    private lateinit var workoutName: TextView
    private lateinit var workoutIcon: ImageView
    private var workoutListRoutine = ArrayList<WorkoutSessionRecord>()

    // Normalize intensity to API format
    private fun normalizeIntensity(intensity: String): String {
        return when (intensity.lowercase()) {
            "low" -> "Low"
            "medium", "moderate" -> "Moderate"
            "high" -> "High"
            "very high", "veryhigh" -> "Very High"
            else -> "Low"
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve arguments
        routine = arguments?.getString("routine").toString()
        routineName = arguments?.getString("routineName").toString()
        routineId = arguments?.getString("routineId").toString()
        editWorkoutRoutine = arguments?.getString("editworkoutRoutine").toString()
        editWorkoutRoutineItem = arguments?.getParcelable("workoutItem")
        mSelectedDate = arguments?.getString("selected_date").toString()
        workoutListRoutine = arguments?.getParcelableArrayList("workoutList") ?: ArrayList()
        activityModel = arguments?.getParcelable("ACTIVITY_MODEL")
        workoutModel = arguments?.getParcelable("WORKOUT_MODEL")
        edit = arguments?.getString("edit") ?: ""
        edit_routine = arguments?.getString("edit_routine") ?: ""
        val allworkout = arguments?.getString("allworkout") ?: ""
        moduleName = arguments?.getString("ModuleName").toString()
        workout = arguments?.getParcelable("workout")
        context?.let { it1 ->
            AnalyticsLogger.logEvent(
                it1, AnalyticsEvent.MR_Workout_AddToLog
            )
        }
        // Views
        caloriesText = view.findViewById(R.id.calories_text)
        val hourPicker = view.findViewById<NumberPicker>(R.id.hourPicker)
        val minutePicker = view.findViewById<NumberPicker>(R.id.minutePicker)
        addLog = view.findViewById(R.id.layout_btn_log_meal)
        val addSearchFragmentBackButton = view.findViewById<ImageView>(R.id.back_button)
        workoutName = view.findViewById(R.id.workoutName)
        workoutIcon = view.findViewById(R.id.workoutIcon)
        intensityProgressBar = view.findViewById(R.id.customSeekBar)

        // Picker setup
        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        hourPicker.selectedTextColor = Color.parseColor("#FD6967")
        minutePicker.selectedTextColor = Color.parseColor("#FD6967")
        val boldFont = context?.let { ResourcesCompat.getFont(it, R.font.dmsans_bold) }
        hourPicker.setTypeface(boldFont)
        hourPicker.setSelectedTypeface(boldFont)
        minutePicker.setTypeface(boldFont)
        minutePicker.setSelectedTypeface(boldFont)

        if (mSelectedDate.isNullOrEmpty()) {
            mSelectedDate = getCurrentDate()
        }

        lastWorkoutRecord = arguments?.let { BundleCompat.getParcelable(it, "workoutRecord", WorkoutSessionRecord::class.java) }
        //addLog.isEnabled = false

        // Back button
        addSearchFragmentBackButton.setOnClickListener {
            if (edit == "edit") {
                navigateToFragment(YourActivityFragment(), "YourActivityFragment")
            } else if (editWorkoutRoutine == "editworkoutRoutine") {
                navigateToCreateRoutineFragment()
            } else if (routine == "routine") {
                if (workoutListRoutine.isNotEmpty()) workoutListRoutine.removeAt(workoutListRoutine.size - 1)
                navigateToCreateRoutineFragment()
            } else if (routine == "edit_routine") {
                if (workoutListRoutine.isNotEmpty()) workoutListRoutine.removeAt(workoutListRoutine.size - 1)
                navigateToFragmentData(SearchWorkoutFragment(), "SearchWorkoutFragment")
            } else if (allworkout == "allworkout") {
                navigateToFragment(SearchWorkoutFragment(), "SearchWorkoutFragment")
            } else {
                navigateToFragment(YourActivityFragment(), "YourActivityFragment")
            }
        }

        // Add Log Button
        addLog.setOnClickListener {
            val hours = hourPicker.value
            val minutes = minutePicker.value
            val durationMinutes = hours * 60 + minutes

            if (edit == "edit") {
                activityModel?.id?.let { calorieId ->
                    val normalizedIntensity = normalizeIntensity(selectedIntensity)
                    if (durationMinutes != 0) {
                        updateCalories(calorieId, durationMinutes, normalizedIntensity)
                    } else {
                      //  Toast.makeText(requireContext(), "Duration cannot be 0", Toast.LENGTH_SHORT).show()
                        showCustomToast(requireContext(), "Duration cannot be 0")
                    }
                } ?: showCustomToast(requireContext(), "Calorie ID is missing")
            } else if (editWorkoutRoutine == "editworkoutRoutine") {
                if (durationMinutes > 0) {
                    editWorkoutRoutineItem?.id?.let { updateWorkoutEntryById(it, durationMinutes, normalizeIntensity(selectedIntensity)) }
                } else {
                    showCustomToast(requireContext(), "Please select a duration")
                }
            } else if (routine == "routine" || routine == "edit_routine") {
                if (durationMinutes > 0) {
                    updateLastEntryCalories(durationMinutes, normalizeIntensity(selectedIntensity))
                } else {
                    showCustomToast(requireContext(), "Please select a duration")
                }
            } else {
                workout?.let { w ->
                    val normalizedIntensity = normalizeIntensity(selectedIntensity)
                    val newRecord = WorkoutSessionRecord(
                        userId = SharedPreferenceManager.getInstance(requireActivity()).userId,
                        activityId = w._id,
                        durationMin = durationMinutes,
                        intensity = normalizedIntensity,
                        sessions = 1,
                        message = lastWorkoutRecord?.message,
                        caloriesBurned = lastWorkoutRecord?.caloriesBurned,
                        activityFactor = lastWorkoutRecord?.activityFactor,
                        moduleName = w.title.toString(),
                        moduleIcon = w.iconUrl
                    )
                    lastWorkoutRecord = newRecord
                    if (durationMinutes > 0 && caloriesText.text != "0") {
                        createWorkout(lastWorkoutRecord)
                    } else {
                        showCustomToast(requireContext(), "Please select a duration")
                    }
                } ?: showCustomToast(requireContext(), "Please select a workout")
            }
        }

        // Back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                addSearchFragmentBackButton.performClick()
            }
        })

        // === EDIT MODE: Set initial values ===
        if (edit == "edit" && activityModel != null) {
            val timeStr = activityModel?.duration!!
            val numbers = Regex("\\d+").findAll(timeStr).map { it.value.toInt() }.toList()
            val hours = if (numbers.size > 1) numbers[0] else 0
            val minutes = if (numbers.size > 1) numbers[1] else numbers.getOrNull(0) ?: 0
            hourPicker.value = hours
            minutePicker.value = minutes
            selectedTime = if (hours > 0) "$hours hr $minutes min" else "$minutes min"
            caloriesText.text = activityModel?.caloriesBurned?.toDoubleOrNull()?.roundToInt()?.toString() ?: "0"
            selectedIntensity = normalizeIntensity(activityModel?.intensity ?: "Low")
            intensityProgressBar.progress = when (selectedIntensity) {
                "Low" -> 0.0f
                "Moderate" -> 0.3333f
                "High" -> 0.6666f
                "Very High" -> 1.0f
                else -> 0.0f
            }
            addLog.isEnabled = true
        } else if (editWorkoutRoutine == "editworkoutRoutine" && editWorkoutRoutineItem != null) {
            val durationMinutes = editWorkoutRoutineItem?.duration?.replace(" min", "")?.toIntOrNull() ?: 0
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            hourPicker.value = hours
            minutePicker.value = minutes
            caloriesText.text = editWorkoutRoutineItem?.caloriesBurned?.toDoubleOrNull()?.roundToInt()?.toString() ?: "0"
            selectedIntensity = normalizeIntensity(editWorkoutRoutineItem?.intensity ?: "Low")
            intensityProgressBar.progress = when (selectedIntensity) {
                "Low" -> 0.0f
                "Moderate" -> 0.3333f
                "High" -> 0.6666f
                "Very High" -> 1.0f
                else -> 0.0f
            }
            workoutName.text = editWorkoutRoutineItem?.name
            setWorkoutIcon(editWorkoutRoutineItem?.name)
            addLog.isEnabled = true
        }

        // === PICKER LISTENERS (AFTER INITIAL SET) ===
        val debounceDelay = 300L
        val debouncedRunnable = Runnable {
            timeListener(hourPicker.value, minutePicker.value)
        }

        hourPicker.setOnValueChangedListener { _, oldVal, newVal ->
            if (oldVal != newVal) {
                hourPicker.selectedTextColor = Color.parseColor("#FD6967")
                minutePicker.selectedTextColor = Color.parseColor("#FD6967")
                handler.removeCallbacks(debouncedRunnable)
                handler.postDelayed(debouncedRunnable, debounceDelay)
            }
        }

        minutePicker.setOnValueChangedListener { _, oldVal, newVal ->
            if (oldVal != newVal) {
                hourPicker.selectedTextColor = Color.parseColor("#FD6967")
                minutePicker.selectedTextColor = Color.parseColor("#FD6967")
                handler.removeCallbacks(debouncedRunnable)
                handler.postDelayed(debouncedRunnable, debounceDelay)
            }
        }

        hourPicker.setOnScrollListener { _, scrollState ->
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                handler.removeCallbacks(debouncedRunnable)
                handler.postDelayed(debouncedRunnable, debounceDelay)
            }
        }

        minutePicker.setOnScrollListener { _, scrollState ->
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                handler.removeCallbacks(debouncedRunnable)
                handler.postDelayed(debouncedRunnable, debounceDelay)
            }
        }

        // === INTENSITY CHANGE ===
        intensityProgressBar.setOnProgressChangedListener { _ ->
            selectedIntensity = intensityProgressBar.getCurrentIntensity()
            when (intensityProgressBar.progress) {
                0.0f -> selectedIntensity = "Low"
                0.3333f -> selectedIntensity = "Moderate"
                0.6666f -> selectedIntensity = "High"
                1.0f -> selectedIntensity = "Very High"
            }
            val duration = hourPicker.value * 60 + minutePicker.value
            val activityId = when {
                edit == "edit" -> activityModel?.activityId
                editWorkoutRoutine == "editworkoutRoutine" -> editWorkoutRoutineItem?.id
                routine == "edit_routine" -> workoutListRoutine.lastOrNull()?.activityId
                else -> workout?._id
            }
            if (activityId != null && duration > 0) {
                calculateUserCalories(duration, selectedIntensity, activityId)
                addLog.isEnabled = true
            }
        }

        // === ICON & NAME ===
        if (workout != null) {
            workoutName.text = workout?.title
            val imageBaseUrl = BuildConfig.CDN_URL + workout?.iconUrl
            moduleIcon = imageBaseUrl
           /* Glide.with(requireContext())
                .load(imageBaseUrl)
                .placeholder(R.drawable.athelete_search)
                .error(R.drawable.athelete_search)
                .into(workoutIcon)*/
        } else if (activityModel != null) {
            workoutName.text = activityModel?.workoutType
            moduleIcon = activityModel?.icon
          /*  Glide.with(requireContext())
                .load(moduleIcon)
                .placeholder(R.drawable.athelete_search)
                .error(R.drawable.athelete_search)
                .into(workoutIcon)*/
        } else if (workoutModel != null) {
            workoutName.text = workoutModel?.activityName
        }

        // === NON-EDIT DEFAULT ===
        if (edit != "edit" && editWorkoutRoutine != "editworkoutRoutine") {
            hourPicker.value = 0
            minutePicker.value = 0
            caloriesText.text = "0"
            addLog.isEnabled = true
        }
        when (workoutName.text.toString()) {
            "American Football" -> workoutIcon.setImageResource(R.drawable.american_football)
            "Archery" -> workoutIcon.setImageResource(R.drawable.archery)
            "Athletics" -> workoutIcon.setImageResource(R.drawable.athelete_search)
            "Australian Football" -> workoutIcon.setImageResource(R.drawable.australian_football)
            "Badminton" -> workoutIcon.setImageResource(R.drawable.badminton)
            "Barre" -> workoutIcon.setImageResource(R.drawable.barre)
            "Baseball" -> workoutIcon.setImageResource(R.drawable.baseball)
            "Basketball" -> workoutIcon.setImageResource(R.drawable.basketball)
            "Boxing" -> workoutIcon.setImageResource(R.drawable.boxing)
            "Climbing" -> workoutIcon.setImageResource(R.drawable.climbing)
            "Core Training" -> workoutIcon.setImageResource(R.drawable.core_training)
            "Cycling" -> workoutIcon.setImageResource(R.drawable.cycling)
            "Cricket" -> workoutIcon.setImageResource(R.drawable.cricket)
            "Cross Training" -> workoutIcon.setImageResource(R.drawable.cross_training)
            "Dance" -> workoutIcon.setImageResource(R.drawable.dance)
            "Disc Sports" -> workoutIcon.setImageResource(R.drawable.disc_sports)
            "Elliptical" -> workoutIcon.setImageResource(R.drawable.elliptical)
            "Football" -> workoutIcon.setImageResource(R.drawable.football)
            "Functional Strength Training" -> workoutIcon.setImageResource(R.drawable.functional_strength_training)
            "Golf" -> workoutIcon.setImageResource(R.drawable.golf)
            "Gymnastics" -> workoutIcon.setImageResource(R.drawable.gymnastics)
            "Handball" -> workoutIcon.setImageResource(R.drawable.handball)
            "Hiking" -> workoutIcon.setImageResource(R.drawable.hockey)
            "Hockey" -> workoutIcon.setImageResource(R.drawable.hiit)
            "HIIT", "High Intensity Interval Training" -> workoutIcon.setImageResource(R.drawable.hiking)
            "Kickboxing" -> workoutIcon.setImageResource(R.drawable.kickboxing)
            "Martial Arts" -> workoutIcon.setImageResource(R.drawable.martial_arts)
            "Other" -> workoutIcon.setImageResource(R.drawable.other)
            "Pickleball" -> workoutIcon.setImageResource(R.drawable.pickleball)
            "Pilates" -> workoutIcon.setImageResource(R.drawable.pilates)
            "Power Yoga" -> workoutIcon.setImageResource(R.drawable.power_yoga)
            "Powerlifting" -> workoutIcon.setImageResource(R.drawable.powerlifting)
            "Pranayama" -> workoutIcon.setImageResource(R.drawable.pranayama)
            "Running" -> workoutIcon.setImageResource(R.drawable.running)
            "Rowing Machine" -> workoutIcon.setImageResource(R.drawable.rowing_machine)
            "Rugby" -> workoutIcon.setImageResource(R.drawable.rugby)
            "Skating" -> workoutIcon.setImageResource(R.drawable.skating)
            "Skipping" -> workoutIcon.setImageResource(R.drawable.skipping)
            "Stairs" -> workoutIcon.setImageResource(R.drawable.stairs)
            "Squash" -> workoutIcon.setImageResource(R.drawable.squash)
            "Traditional Strength Training", "Strength Training" -> workoutIcon.setImageResource(R.drawable.traditional_strength_training)
            "Stretching" -> workoutIcon.setImageResource(R.drawable.stretching)
            "Swimming" -> workoutIcon.setImageResource(R.drawable.swimming)
            "Table Tennis" -> workoutIcon.setImageResource(R.drawable.table_tennis)
            "Tennis" -> workoutIcon.setImageResource(R.drawable.tennis)
            "Track and Field Events" -> workoutIcon.setImageResource(R.drawable.track_field_events)
            "Volleyball" -> workoutIcon.setImageResource(R.drawable.volleyball)
            "Walking" -> workoutIcon.setImageResource(R.drawable.walking)
            "Watersports" -> workoutIcon.setImageResource(R.drawable.watersports)
            "Wrestling" -> workoutIcon.setImageResource(R.drawable.wrestling)
            "Yoga" -> workoutIcon.setImageResource(R.drawable.yoga)
            else -> workoutIcon.setImageResource(R.drawable.other)
        }

        // Initial refresh
        refreshPickers()
    }

    // === TIME LISTENER (LIVE VALUES) ===
    private fun timeListener(hours: Int, minutes: Int) {
        selectedTime = "$hours hr ${minutes.toString().padStart(2, '0')} min"
        val durationMinutes = hours * 60 + minutes
        val activityId = when {
            edit == "edit" -> activityModel?.activityId
            editWorkoutRoutine == "editworkoutRoutine" -> editWorkoutRoutineItem?.id
            routine == "edit_routine" -> workoutListRoutine.lastOrNull()?.activityId
            routine == "routine" -> workoutListRoutine.lastOrNull()?.activityId
            else -> workout?._id
        }
        if (activityId != null && durationMinutes > 0) {
            calculateUserCalories(durationMinutes, normalizeIntensity(selectedIntensity), activityId)
            addLog.isEnabled = true
        } else {
            addLog.isEnabled = durationMinutes > 0
            if (durationMinutes == 0) caloriesText.text = "0"
            addLog.isEnabled = true
        }
        refreshPickers()
    }

    // === HELPER: Set icon for editWorkoutRoutineItem ===
    private fun setWorkoutIcon(name: String?) {
        when (name) {
            "American Football" -> workoutIcon.setImageResource(R.drawable.american_football)
            "Archery" -> workoutIcon.setImageResource(R.drawable.archery)
            "Athletics" -> workoutIcon.setImageResource(R.drawable.athelete_search)
            "Australian Football" -> workoutIcon.setImageResource(R.drawable.australian_football)
            "Badminton" -> workoutIcon.setImageResource(R.drawable.badminton)
            "Barre" -> workoutIcon.setImageResource(R.drawable.barre)
            "Baseball" -> workoutIcon.setImageResource(R.drawable.baseball)
            "Basketball" -> workoutIcon.setImageResource(R.drawable.basketball)
            "Boxing" -> workoutIcon.setImageResource(R.drawable.boxing)
            "Climbing" -> workoutIcon.setImageResource(R.drawable.climbing)
            "Core Training" -> workoutIcon.setImageResource(R.drawable.core_training)
            "Cycling" -> workoutIcon.setImageResource(R.drawable.cycling)
            "Cricket" -> workoutIcon.setImageResource(R.drawable.cricket)
            "Cross Training" -> workoutIcon.setImageResource(R.drawable.cross_training)
            "Dance" -> workoutIcon.setImageResource(R.drawable.dance)
            "Disc Sports" -> workoutIcon.setImageResource(R.drawable.disc_sports)
            "Elliptical" -> workoutIcon.setImageResource(R.drawable.elliptical)
            "Football" -> workoutIcon.setImageResource(R.drawable.football)
            "Functional Strength Training" -> workoutIcon.setImageResource(R.drawable.functional_strength_training)
            "Golf" -> workoutIcon.setImageResource(R.drawable.golf)
            "Gymnastics" -> workoutIcon.setImageResource(R.drawable.gymnastics)
            "Handball" -> workoutIcon.setImageResource(R.drawable.handball)
            "Hiking" -> workoutIcon.setImageResource(R.drawable.hockey)
            "Hockey" -> workoutIcon.setImageResource(R.drawable.hiit)
            "HIIT", "High Intensity Interval Training" -> workoutIcon.setImageResource(R.drawable.hiking)
            "Kickboxing" -> workoutIcon.setImageResource(R.drawable.kickboxing)
            "Martial Arts" -> workoutIcon.setImageResource(R.drawable.martial_arts)
            "Other" -> workoutIcon.setImageResource(R.drawable.other)
            "Pickleball" -> workoutIcon.setImageResource(R.drawable.pickleball)
            "Pilates" -> workoutIcon.setImageResource(R.drawable.pilates)
            "Power Yoga" -> workoutIcon.setImageResource(R.drawable.power_yoga)
            "Powerlifting" -> workoutIcon.setImageResource(R.drawable.powerlifting)
            "Pranayama" -> workoutIcon.setImageResource(R.drawable.pranayama)
            "Running" -> workoutIcon.setImageResource(R.drawable.running)
            "Rowing Machine" -> workoutIcon.setImageResource(R.drawable.rowing_machine)
            "Rugby" -> workoutIcon.setImageResource(R.drawable.rugby)
            "Skating" -> workoutIcon.setImageResource(R.drawable.skating)
            "Skipping" -> workoutIcon.setImageResource(R.drawable.skipping)
            "Stairs" -> workoutIcon.setImageResource(R.drawable.stairs)
            "Squash" -> workoutIcon.setImageResource(R.drawable.squash)
            "Traditional Strength Training", "Strength Training" -> workoutIcon.setImageResource(R.drawable.traditional_strength_training)
            "Stretching" -> workoutIcon.setImageResource(R.drawable.stretching)
            "Swimming" -> workoutIcon.setImageResource(R.drawable.swimming)
            "Table Tennis" -> workoutIcon.setImageResource(R.drawable.table_tennis)
            "Tennis" -> workoutIcon.setImageResource(R.drawable.tennis)
            "Track and Field Events" -> workoutIcon.setImageResource(R.drawable.track_field_events)
            "Volleyball" -> workoutIcon.setImageResource(R.drawable.volleyball)
            "Walking" -> workoutIcon.setImageResource(R.drawable.walking)
            "Watersports" -> workoutIcon.setImageResource(R.drawable.watersports)
            "Wrestling" -> workoutIcon.setImageResource(R.drawable.wrestling)
            "Yoga" -> workoutIcon.setImageResource(R.drawable.yoga)
            else -> workoutIcon.setImageResource(R.drawable.other)
        }
    }

    // === REFRESH PICKERS ===
    private fun refreshPickers() {
        handler.post {
            try {
                val field = NumberPicker::class.java.getDeclaredField("mInputText")
                field.isAccessible = true
                val editTextHour = field.get(view?.findViewById<NumberPicker>(R.id.hourPicker)) as EditText
                val editTextMinute = field.get(view?.findViewById<NumberPicker>(R.id.minutePicker)) as EditText
                editTextHour.setTextColor(Color.RED)
                editTextHour.textSize = 24f
                editTextHour.typeface = Typeface.DEFAULT_BOLD
                editTextMinute.setTextColor(Color.RED)
                editTextMinute.textSize = 24f
                editTextMinute.typeface = Typeface.DEFAULT_BOLD
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // === REST OF YOUR FUNCTIONS (UNCHANGED) ===
    private fun updateLastEntryCalories(durationMinutes: Int, normalizedIntensity: String) {
        if (workoutListRoutine.isEmpty()) {
            Toast.makeText(requireContext(), "Workout list is empty", Toast.LENGTH_SHORT).show()
            navigateToCreateRoutineFragment()
            return
        }

        val lastIndex = workoutListRoutine.size - 1
        val lastEntry = workoutListRoutine[lastIndex]

        if (lastEntry.activityId == null) {
            showCustomToast(requireContext(), "Activity ID missing for last workout")
            navigateToCreateRoutineFragment()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                val request = CalculateCaloriesRequest(
                    userId = userId,
                    activityId = lastEntry.activityId!!,
                    durationMin = durationMinutes,
                    intensity = selectedIntensity,
                    sessions = lastEntry.sessions,
                    date = getCurrentDate()
                )
                val response: Response<CalculateCaloriesResponse> = ApiClient.apiServiceFastApi.calculateCalories(request)
                if (response.isSuccessful) {
                    val caloriesResponse = response.body()
                    if (caloriesResponse != null) {
                        workoutListRoutine[lastIndex] = lastEntry.copy(
                            durationMin = durationMinutes,
                            intensity = normalizedIntensity,
                            message = caloriesResponse.message,
                            caloriesBurned = caloriesResponse.caloriesBurned,
                            activityFactor = caloriesResponse.activityFactor,
                            moduleIcon = moduleIcon!!
                        )
                        withContext(Dispatchers.Main) {
                            showCustomToast(requireContext(), "Workout Created Successfully")
                            navigateToCreateRoutineFragment()
                        }
                    } else {
                        workoutListRoutine[lastIndex] = lastEntry.copy(durationMin = durationMinutes, intensity = normalizedIntensity)
                        withContext(Dispatchers.Main) {
                            showCustomToast(requireContext(), "No calories data received for ${lastEntry.moduleName}")
                            navigateToCreateRoutineFragment()
                        }
                    }
                } else {
                    workoutListRoutine[lastIndex] = lastEntry.copy(durationMin = durationMinutes, intensity = normalizedIntensity)
                    withContext(Dispatchers.Main) {
                        showCustomToast(requireContext(), "Error for ${lastEntry.moduleName}: ${response.code()}")
                        navigateToCreateRoutineFragment()
                    }
                }
            } catch (e: Exception) {
                workoutListRoutine[lastIndex] = lastEntry.copy(durationMin = durationMinutes, intensity = normalizedIntensity)
                withContext(Dispatchers.Main) {
                    showCustomToast(requireContext(), "Exception: ${e.message}")
                    navigateToCreateRoutineFragment()
                }
            }
        }
    }

    private fun navigateToCreateRoutineFragment() {
        Log.d("AddWorkoutSearchFragment", "Sending workoutList to CreateRoutineFragment: $workoutListRoutine")
        if (workoutListRoutine.isNullOrEmpty()) {
            showCustomToast(requireContext(), "No workouts selected")
            return
        }
        setFragmentResult("workoutListUpdate", Bundle().apply {
            putParcelableArrayList("workoutList", workoutListRoutine)
        })
        val args = Bundle().apply {
            putParcelableArrayList("workoutList", workoutListRoutine)
            putString("routine", routine)
            putString("routineName", routineName)
            putString("routineId", routineId)
            putString("selected_date", mSelectedDate)
           putString("ModuleName", moduleName)
        }
        val createRoutineFragment = CreateRoutineFragment().apply { arguments = args }
        navigateToFragment(createRoutineFragment, "CreateRoutineFragment", args)
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun updateWorkoutEntryById(editWorkoutRoutineItemId: String, durationMinutes: Int, normalizedIntensity: String) {
        if (workoutListRoutine.isEmpty()) {
            showCustomToast(requireContext(), "Workout list is empty")
            navigateToCreateRoutineFragment()
            return
        }

        val targetIndex = workoutListRoutine.indexOfFirst { it.activityId == editWorkoutRoutineItemId }
        if (targetIndex == -1) {
            showCustomToast(requireContext(), "Workout not found")
            navigateToCreateRoutineFragment()
            return
        }

        val targetEntry = workoutListRoutine[targetIndex]
        if (targetEntry.activityId == null) {
            showCustomToast(requireContext(), "Activity ID missing")
            navigateToCreateRoutineFragment()
            return
        }

        Log.d("UpdateWorkoutEntry", "Starting update for itemId: $editWorkoutRoutineItemId at index: $targetIndex")
        Log.d("UpdateWorkoutEntry", "New values → duration: $durationMinutes min, intensity: $normalizedIntensity")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                Log.d("UpdateWorkoutEntry", "userId: $userId")

                val request = CalculateCaloriesRequest(
                    userId = userId,
                    activityId = targetEntry.activityId!!,
                    durationMin = durationMinutes,
                    intensity = normalizedIntensity,
                    sessions = targetEntry.sessions,
                    date = getCurrentDate()
                )
                Log.d("UpdateWorkoutEntry", "Request created → activityId: ${request.activityId}, duration: ${request.durationMin}, sessions: ${request.sessions}")

                Log.d("UpdateWorkoutEntry", "Calling API: calculateCalories")
                val response: Response<CalculateCaloriesResponse> = ApiClient.apiServiceFastApi.calculateCalories(request)

                Log.d("UpdateWorkoutEntry", "API response → isSuccessful = ${response.isSuccessful}, code = ${response.code()}")

                if (response.isSuccessful) {
                    val caloriesResponse = response.body()
                    if (caloriesResponse != null) {
                        Log.d("UpdateWorkoutEntry", "Success → caloriesBurned = ${caloriesResponse.caloriesBurned}, factor = ${caloriesResponse.activityFactor}")

                        workoutListRoutine[targetIndex] = targetEntry.copy(
                            durationMin = durationMinutes,
                            intensity = normalizedIntensity,
                            message = caloriesResponse.message ?: "",
                            caloriesBurned = caloriesResponse.caloriesBurned ?: 0.0,
                            activityFactor = caloriesResponse.activityFactor,
                            moduleIcon = moduleIcon ?: workoutListRoutine[targetIndex].moduleIcon
                        )

                        withContext(Dispatchers.Main) {
                            Log.d("UpdateWorkoutEntry", "Updating fragment result & navigating (success case)")
                            setFragmentResult("workoutListUpdate", Bundle().apply { putParcelableArrayList("workoutList", workoutListRoutine) })
                            showCustomToast(requireContext(), "Workout Created Successfully")
                            navigateToCreateRoutineFragment()
                        }
                    } else {
                        Log.w("UpdateWorkoutEntry", "Success but response body null")

                        workoutListRoutine[targetIndex] = targetEntry.copy(durationMin = durationMinutes, intensity = normalizedIntensity)

                        withContext(Dispatchers.Main) {
                            Log.d("UpdateWorkoutEntry", "Updating fragment result & navigating (null body)")
                            setFragmentResult("workoutListUpdate", Bundle().apply { putParcelableArrayList("workoutList", workoutListRoutine) })
                            showCustomToast(requireContext(), "No calories data")
                            navigateToCreateRoutineFragment()
                        }
                    }
                } else {
                    Log.e("UpdateWorkoutEntry", "API failed → code = ${response.code()}, message = ${response.message()}")

                    workoutListRoutine[targetIndex] = targetEntry.copy(durationMin = durationMinutes, intensity = normalizedIntensity)

                    withContext(Dispatchers.Main) {
                        Log.d("UpdateWorkoutEntry", "Updating fragment result & navigating (error response)")
                        setFragmentResult("workoutListUpdate", Bundle().apply { putParcelableArrayList("workoutList", workoutListRoutine) })
                        showCustomToast(requireContext(), "Error: ${response.code()}")
                        navigateToCreateRoutineFragment()
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateWorkoutEntry", "Exception during update", e)

                workoutListRoutine[targetIndex] = targetEntry.copy(durationMin = durationMinutes, intensity = normalizedIntensity)

                withContext(Dispatchers.Main) {
                    Log.d("UpdateWorkoutEntry", "Updating fragment result & navigating (exception case)")
                    setFragmentResult("workoutListUpdate", Bundle().apply { putParcelableArrayList("workoutList", workoutListRoutine) })
                    showCustomToast(requireContext(), "Exception: ${e.message}")
                    navigateToCreateRoutineFragment()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun createWorkout(workoutSession: WorkoutSessionRecord?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("CreateWorkout", "Starting workout creation")

                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                Log.d("CreateWorkout", "userId fetched: $userId")
                Log.d("CreateWorkout", "selected date: $mSelectedDate")

                val request = workoutSession?.let {
                    Log.d("CreateWorkout", "workoutSession found → activityId: ${it.activityId}, duration: ${it.durationMin}, intensity: ${it.intensity}")

                    CreateWorkoutRequest(
                        userId = userId,
                        activityId = it.activityId,
                        durationMin = it.durationMin,
                        intensity = it.intensity,
                        sessions = 1,
                        date = mSelectedDate
                    )
                } ?: run {
                    Log.w("CreateWorkout", "workoutSession is null → showing toast and returning")
                    withContext(Dispatchers.Main) {
                        showCustomToast(requireContext(), "Workout data missing")
                    }
                    return@launch
                }

                Log.d("CreateWorkout", "Request created → $request")
                Log.d("CreateWorkout", "Calling API: createWorkout")

                val response = ApiClient.apiServiceFastApi.createWorkout(request)

                withContext(Dispatchers.Main) {
                    Log.d("CreateWorkout", "API response received → isSuccessful = ${response.isSuccessful}, code = ${response.code()}")

                    if (response.isSuccessful) {
                        response.body()?.let {
                            Log.d("CreateWorkout", "Success → response body: $it")

                            val fragment = YourActivityFragment()
                            val args = Bundle().apply {
                                putString("selected_date", mSelectedDate)
                                putString("ModuleName", moduleName)
                            }
                            fragment.arguments = args

                            Log.d("CreateWorkout", "Replacing fragment: YourActivityFragment with selected_date = $mSelectedDate")
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.flFragment, fragment, "YourActivityFragment")
                                .addToBackStack("YourActivityFragment")
                                .commit()

                            showCustomToast(requireContext(), "Workout Saved Successfully")
                        } ?: run {
                            Log.w("CreateWorkout", "Success but response body is null")
                            showCustomToast(requireContext(), "Empty response")
                        }
                    } else {
                        Log.e("CreateWorkout", "API failed → code = ${response.code()}, message = ${response.message()}")
                        showCustomToast(requireContext(), "Error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("CreateWorkout", "Exception in createWorkout", e)
                withContext(Dispatchers.Main) {
                    showCustomToast(requireContext(), "Exception: ${e.message}")
                }
            }
        }
    }

    private fun calculateUserCalories(durationMinutes: Int, selectedIntensity: String, activityId: String, navigateToRoutine: Boolean = false) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("CalcCalories", "Starting calorie calculation → activityId: $activityId, duration: $durationMinutes min, intensity: $selectedIntensity")

                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                Log.d("CalcCalories", "userId: $userId")

                val request = CalculateCaloriesRequest(
                    userId = userId,
                    activityId = activityId,
                    durationMin = durationMinutes,
                    intensity = selectedIntensity,
                    sessions = 1,
                    date = getCurrentDate()
                )
                Log.d("CalcCalories", "Request created → $request")

                Log.d("CalcCalories", "Calling API: calculateCalories")
                val response: Response<CalculateCaloriesResponse> = ApiClient.apiServiceFastApi.calculateCalories(request)

                withContext(Dispatchers.Main) {
                    Log.d("CalcCalories", "API response received → isSuccessful = ${response.isSuccessful}, code = ${response.code()}")

                    if (response.isSuccessful) {
                        val caloriesResponse = response.body()
                        if (caloriesResponse != null) {
                            Log.d("CalcCalories", "Success → caloriesBurned = ${caloriesResponse.caloriesBurned}, message = ${caloriesResponse.message}")

                            lastWorkoutRecord = workout?.title?.let {
                                WorkoutSessionRecord(
                                    userId = request.userId,
                                    activityId = request.activityId,
                                    durationMin = request.durationMin,
                                    intensity = request.intensity,
                                    sessions = request.sessions,
                                    message = caloriesResponse.message,
                                    caloriesBurned = caloriesResponse.caloriesBurned,
                                    activityFactor = caloriesResponse.activityFactor,
                                    moduleIcon = "",
                                    moduleName = it
                                )
                            }
                            calorieBurnedNew = caloriesResponse.caloriesBurned
                            caloriesText.text = caloriesResponse.caloriesBurned.toInt().toString()

                            if (navigateToRoutine) {
                                Log.d("CalcCalories", "Navigating to create routine fragment (success case)")
                                navigateToCreateRoutineFragment()
                            }
                        } else {
                            Log.w("CalcCalories", "Success but response body is null")

                            lastWorkoutRecord = workout?.let {
                                WorkoutSessionRecord(
                                    userId = request.userId,
                                    activityId = request.activityId,
                                    durationMin = request.durationMin,
                                    intensity = request.intensity,
                                    sessions = request.sessions,
                                    moduleIcon = it.iconUrl,
                                    moduleName = it.title
                                )
                            }
                            if (navigateToRoutine) {
                                Log.d("CalcCalories", "Navigating to create routine fragment (null body case)")
                                navigateToCreateRoutineFragment()
                            }
                        }
                    } else {
                        Log.e("CalcCalories", "API failed → code = ${response.code()}, message = ${response.message()}")

                        lastWorkoutRecord = workout?.let {
                            WorkoutSessionRecord(
                                userId = request.userId,
                                activityId = request.activityId,
                                durationMin = request.durationMin,
                                intensity = request.intensity,
                                sessions = request.sessions,
                                moduleName = it.title,
                                moduleIcon = it.iconUrl
                            )
                        }
                        if (navigateToRoutine) {
                            Log.d("CalcCalories", "Navigating to create routine fragment (error response case)")
                            navigateToCreateRoutineFragment()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CalcCalories", "Exception in calorie calculation", e)

                withContext(Dispatchers.Main) {
                    Log.d("CalcCalories", "Exception caught → still navigating if needed")
                    if (navigateToRoutine) {
                        navigateToCreateRoutineFragment()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun updateCalories(calorieId: String, durationMinutes: Int, intensity: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("UpdateCalories", "Starting update for calorieId: $calorieId")
                Log.d("UpdateCalories", "durationMinutes = $durationMinutes, intensity = $intensity")

                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                Log.d("UpdateCalories", "userId fetched: $userId")

                val currentDate = getCurrentDate()
                Log.d("UpdateCalories", "currentDate: $currentDate")

                val request = UpdateCaloriesRequest(
                    userId = userId,
                    durationMin = durationMinutes,
                    intensity = intensity,
                    sessions = 1,
                    date = currentDate
                )
                Log.d("UpdateCalories", "Request body created → $request")

                Log.d("UpdateCalories", "Calling API: updateCalories(calorieId = $calorieId)")
                val response = ApiClient.apiServiceFastApi.updateCalories(calorieId, request)

                withContext(Dispatchers.Main) {
                    Log.d("UpdateCalories", "API call finished → isSuccessful = ${response.isSuccessful}, code = ${response.code()}")

                    if (response.isSuccessful) {
                        response.body()?.let {
                            Log.d("UpdateCalories", "Success → response body: $it")

                            val fragment = YourActivityFragment()
                            val args = Bundle().apply { putString("ModuleName", moduleName) }
                            fragment.arguments = args

                            Log.d("UpdateCalories", "Replacing fragment: YourActivityFragment")
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.flFragment, fragment, "YourActivityFragment")
                                .addToBackStack("YourActivityFragment")
                                .commit()

                            showCustomToast(requireContext(), "Calories Updated")
                        } ?: run {
                            Log.w("UpdateCalories", "Success but response body is null")
                            showCustomToast(requireContext(), "Empty response")
                        }
                    } else {
                        Log.e("UpdateCalories", "API failed → code = ${response.code()}, message = ${response.message()}")
                        // Optional: log error body if you want more detail
                        // val errorBody = response.errorBody()?.string()
                        // Log.e("UpdateCalories", "Error body: $errorBody")

                        showCustomToast(requireContext(), "Error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateCalories", "Exception occurred", e)
                withContext(Dispatchers.Main) {
                    showCustomToast(requireContext(), "Exception: ${e.message}")
                }
            }
        }
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String, existingArgs: Bundle? = null) {
        val args = existingArgs ?: Bundle().apply { putString("selected_date", mSelectedDate) }
        args.putString("ModuleName", moduleName)
        fragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToFragmentCreate(fragment: androidx.fragment.app.Fragment, tag: String) {
        val args = Bundle().apply { putString("selected_date", mSelectedDate) }
        fragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToFragmentData(fragment: androidx.fragment.app.Fragment, tag: String) {
        val args = Bundle().apply { putString("routineBack", "routineBack") }
        args.putString("ModuleName", moduleName)
        fragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    private fun showCustomToast(context: Context, message: String?) {
        // Cancel any old toast
        currentToast?.cancel()
        val inflater = LayoutInflater.from(context)
        val toastLayout = inflater.inflate(R.layout.custom_toast_ai_eat, null)
        val textView = toastLayout.findViewById<TextView>(R.id.toast_message)
        textView.text = message
        // ✅ Wrap layout inside FrameLayout to apply margins
        val container = FrameLayout(context)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        val marginInPx = (20 * context.resources.displayMetrics.density).toInt()
        params.setMargins(marginInPx, 0, marginInPx, 0)
        toastLayout.layoutParams = params
        container.addView(toastLayout)
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = container
        toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 100)
        currentToast = toast
        toast.show()
    }
}