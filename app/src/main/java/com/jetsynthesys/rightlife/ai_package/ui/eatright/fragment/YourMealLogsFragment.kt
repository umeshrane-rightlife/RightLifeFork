package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.MealLists
import com.jetsynthesys.rightlife.ai_package.model.MealLogData
import com.jetsynthesys.rightlife.ai_package.model.response.FullDaySummary
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails
import com.jetsynthesys.rightlife.ai_package.model.response.LoggedMealHistory
import com.jetsynthesys.rightlife.ai_package.model.response.MaxMacros
import com.jetsynthesys.rightlife.ai_package.model.response.MealDetailsLog
import com.jetsynthesys.rightlife.ai_package.model.response.MealLogDataResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MealLogsHistoryResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MealNutritionSummary
import com.jetsynthesys.rightlife.ai_package.model.response.MergedLogsMealItem
import com.jetsynthesys.rightlife.ai_package.model.response.RegularRecipeEntry
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMeal
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MealLogWeeklyDayAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourBreakfastMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourDinnerMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourEveningSnacksMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourLunchMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourMorningSnackMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.HomeTabMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MealLogWeeklyDayModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeDetailsLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.utils.AppPreference
import com.jetsynthesys.rightlife.databinding.FragmentYourMealLogsBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.round

class YourMealLogsFragment : BaseFragment<FragmentYourMealLogsBinding>(), DeleteLogDishBottomSheet.OnLogDishDeletedListener,
    SelectMealTypeBottomSheet.OnMealTypeListener {

    private lateinit var layoutToolbar :ConstraintLayout
    private lateinit var mealLogWeeklyDayRecyclerView : RecyclerView
    private lateinit var breakfastMealRecyclerView : RecyclerView
    private lateinit var morningSnackMealsRecyclerView : RecyclerView
    private lateinit var lunchMealRecyclerView : RecyclerView
    private lateinit var eveningSnacksMealRecyclerView : RecyclerView
    private lateinit var dinnerMealRecyclerView : RecyclerView
    private lateinit var imageCalender : ImageView
    private lateinit var breakfastDotMenu : ImageView
    private lateinit var lunchDotMenu : ImageView
    private lateinit var dinnerDotMenu : ImageView
    private lateinit var backButton : ImageView
    private lateinit var btnLogMeal : LinearLayoutCompat
    private lateinit var editDeleteBreakfast : CardView
    private lateinit var editDeleteLunch : CardView
    private lateinit var editDeleteDinner : CardView
    private lateinit var layoutMain : ConstraintLayout
    private lateinit var selectMealTypeBottomSheet: SelectMealTypeBottomSheet
    private lateinit var layoutDelete : LinearLayoutCompat
    private lateinit var viewBFMealInsightLayout : LinearLayoutCompat
    private lateinit var viewMSMealInsightLayout : LinearLayoutCompat
    private lateinit var viewLunchMealInsightLayout : LinearLayoutCompat
    private lateinit var viewESMealInsightLayout : LinearLayoutCompat
    private lateinit var viewDinnerMealInsightLayout : LinearLayoutCompat
    private lateinit var addFoodLayout : LinearLayoutCompat
    private lateinit var appPreference: AppPreference
    private lateinit var calValue : TextView
    private lateinit var carbsValue : TextView
    private lateinit var proteinsValue : TextView
    private lateinit var fatsValue : TextView
    private lateinit var caloriesProgressBar : ProgressBar
    private lateinit var carbsProgressBar : ProgressBar
    private lateinit var proteinsProgressBar : ProgressBar
    private lateinit var fatsProgressBar : ProgressBar
    private lateinit var transparentOverlay : View
    private lateinit var addBreakfastLayout : LinearLayoutCompat
    private lateinit var addMorningSnackLayout : LinearLayoutCompat
    private lateinit var addLunchLayout : LinearLayoutCompat
    private lateinit var addEveningSnacksLayout : LinearLayoutCompat
    private lateinit var addDinnerLayout : LinearLayoutCompat
    private lateinit var selectedWeeklyDayTv : TextView
    private lateinit var nextWeekBtn : ImageView
    private lateinit var prevWeekBtn : ImageView
    private lateinit var breakfastListLayout : ConstraintLayout
    private lateinit var morningSnackListLayout : ConstraintLayout
    private lateinit var lunchListLayout : ConstraintLayout
    private lateinit var eveningSnacksListLayout : ConstraintLayout
    private lateinit var dinnerListLayout : ConstraintLayout
    private lateinit var dailyCalorieGraphLayout : ConstraintLayout
    private lateinit var calValueTv : TextView
    private lateinit var calValueMorningSnackTv : TextView
    private lateinit var calValueLunchTv : TextView
    private lateinit var calValueEveningSnacksTv : TextView
    private lateinit var calValueDinnerTv : TextView
    private lateinit var noMealLogsLayout : LinearLayoutCompat
    private lateinit var logMealTv : TextView
    private lateinit var maxCalUnit : TextView
    private lateinit var maxCarbUnit : TextView
    private lateinit var maxProteinUnit : TextView
    private lateinit var maxFatsUnit : TextView
    private lateinit var progressBarLayout : ConstraintLayout
    private lateinit var layoutWeightLoss : LinearLayoutCompat
    private lateinit var imageViewBelowOverlay : ImageView
    private lateinit var kcalStart : TextView
    private lateinit var kcalEnd : TextView
    private var selectedMealDate : String = ""
    private var moduleName : String = ""

    private var currentWeekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)
    private var mealLogsHistoryResponse : MealLogsHistoryResponse? = null
    private var  mealLogHistory :  ArrayList<LoggedMealHistory> = ArrayList()

    private var mealLogWeeklyDayList : List<MealLogWeeklyDayModel> = ArrayList()
    private var mealPlanData : ArrayList<MealLogData> = ArrayList()
    private var mealList : ArrayList<MealLists> = ArrayList()
    private val breakfastCombinedList : ArrayList<MergedLogsMealItem> = ArrayList()
    private val morningSnackCombinedList : ArrayList<MergedLogsMealItem> = ArrayList()
    private val lunchCombinedList : ArrayList<MergedLogsMealItem> = ArrayList()
    private val eveningSnacksCombinedList : ArrayList<MergedLogsMealItem> = ArrayList()
    private val dinnerCombinedList : ArrayList<MergedLogsMealItem> = ArrayList()

    private val breakfastMealNutritionSummary = ArrayList<MealNutritionSummary>()
    private val morningSnackMealNutritionSummary = ArrayList<MealNutritionSummary>()
    private val lunchMealNutritionSummary = ArrayList<MealNutritionSummary>()
    private val eveningSnacksMealNutritionSummary = ArrayList<MealNutritionSummary>()
    private val dinnerMealNutritionSummary = ArrayList<MealNutritionSummary>()
    private var breakFastMealDetailsLog : MealDetailsLog? = null
    private var morningSnackMealDetailsLog : MealDetailsLog? = null
    private var lunchMealDetailsLog : MealDetailsLog? = null
    private var eveningSnackMealDetailsLog : MealDetailsLog? = null
    private var dinnerMealDetailsLog : MealDetailsLog? = null
    private var selectedDate : String = ""
    private var loadingOverlay : FrameLayout? = null
    private var lastDayOfCurrentWeek : String = ""
    private var dialogToolTip : Dialog? = null
    private var currentWeekStartItem: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentYourMealLogsBinding
        get() = FragmentYourMealLogsBinding::inflate

    private val mealLogWeeklyDayAdapter by lazy { MealLogWeeklyDayAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onMealLogDateItem) }
    private val breakfastMealLogsAdapter by lazy { YourBreakfastMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null, false, ::onBreakFastRegularRecipeDeleteItem,
        :: onBreakFastRegularRecipeEditItem, :: onBreakFastSnapMealDeleteItem, :: onBreakFastSnapMealEditItem, false, false) }
    private val morningSnackMealLogsAdapter by lazy { YourMorningSnackMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null,false, :: onMSRegularRecipeDeleteItem, :: onMSRegularRecipeEditItem,
        :: onMSSnapMealDeleteItem, :: onMSSnapMealEditItem, false, false) }
    private val lunchMealLogsAdapter by lazy { YourLunchMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null,false, :: onLunchRegularRecipeDeleteItem, :: onLunchRegularRecipeEditItem,
        :: onLunchSnapMealDeleteItem, :: onLunchSnapMealEditItem, false, false) }
    private val eveningSnacksMealLogsAdapter by lazy { YourEveningSnacksMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null,false, :: onESRegularRecipeDeleteItem, :: onESRegularRecipeEditItem,
        :: onESSnapMealDeleteItem, :: onESSnapMealEditItem, false, false) }
    private val dinnerMealLogsAdapter by lazy { YourDinnerMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null,false, :: onDinnerRegularRecipeDeleteItem, :: onDinnerRegularRecipeEditItem,
        :: onDinnerSnapMealDeleteItem, :: onDinnerSnapMealEditItem, false, false) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        appPreference = AppPreference(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appPreference = AppPreference(requireContext())
        moduleName = arguments?.getString("ModuleName").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))
        mealLogWeeklyDayRecyclerView = view.findViewById(R.id.recyclerview_calender)
        breakfastMealRecyclerView = view.findViewById(R.id.recyclerview_breakfast_meals_item)
        morningSnackMealsRecyclerView = view.findViewById(R.id.recyclerviewMorningSnackMealsItem)
        lunchMealRecyclerView = view.findViewById(R.id.recyclerview_lunch_meals_item)
        eveningSnacksMealRecyclerView = view.findViewById(R.id.recyclerview_eveningSnacks_meals_item)
        dinnerMealRecyclerView = view.findViewById(R.id.recyclerview_dinner_meals_item)
        imageCalender = view.findViewById(R.id.image_calender)
        btnLogMeal = view.findViewById(R.id.layout_btn_log_meal)
        breakfastDotMenu = view.findViewById(R.id.image_dot_menu)
        lunchDotMenu = view.findViewById(R.id.image_lunch_dot_menu)
        dinnerDotMenu = view.findViewById(R.id.image_dinner_dot_menu)
        editDeleteBreakfast = view.findViewById(R.id.btn_edit_delete)
        editDeleteLunch = view.findViewById(R.id.btn_edit_delete_lunch)
        editDeleteDinner = view.findViewById(R.id.btn_edit_delete_dinner)
        layoutMain = view.findViewById(R.id.layout_main)
        layoutDelete = view.findViewById(R.id.layout_delete)
        viewBFMealInsightLayout = view.findViewById(R.id.viewBFMealInsightLayout)
        viewMSMealInsightLayout = view.findViewById(R.id.viewMSMealInsightLayout)
        viewLunchMealInsightLayout = view.findViewById(R.id.viewLunchMealInsightLayout)
        viewESMealInsightLayout = view.findViewById(R.id.viewESMealInsightLayout)
        viewDinnerMealInsightLayout = view.findViewById(R.id.viewDinnerMealInsightLayout)
        calValue = view.findViewById(R.id.tv_meal_cal_value)
        proteinsValue = view.findViewById(R.id.tvProteinValue)
        carbsValue = view.findViewById(R.id.tvCarbsValue)
        fatsValue = view.findViewById(R.id.tvFatsValue)
        caloriesProgressBar = view.findViewById(R.id.cal_progressBar)
        carbsProgressBar = view.findViewById(R.id.carbs_progressBar)
        proteinsProgressBar = view.findViewById(R.id.protien_progressBar)
        fatsProgressBar = view.findViewById(R.id.fats_progressBar)
        backButton = view.findViewById(R.id.backButton)
        addFoodLayout = view.findViewById(R.id.layout_add_food)
        addBreakfastLayout = view.findViewById(R.id.addBreakfastLayout)
        addMorningSnackLayout = view.findViewById(R.id.addMorningSnackLayout)
        addLunchLayout = view.findViewById(R.id.addLunchLayout)
        addEveningSnacksLayout = view.findViewById(R.id.addEveningSnacksLayout)
        addDinnerLayout = view.findViewById(R.id.addDinnerLayout)
        layoutToolbar = view.findViewById(R.id.layoutToolbar)
        transparentOverlay = view.findViewById(R.id.transparentOverlay)
        selectedWeeklyDayTv = view.findViewById(R.id.selectedWeeklyDayTv)
        nextWeekBtn = view.findViewById(R.id.nextWeekBtn)
        prevWeekBtn = view.findViewById(R.id.prevWeekBtn)
        breakfastListLayout = view.findViewById(R.id.layout_breakfast_list)
        morningSnackListLayout = view.findViewById(R.id.layoutMorningSnackList)
        lunchListLayout = view.findViewById(R.id.layout_lunch_list)
        eveningSnacksListLayout = view.findViewById(R.id.layout_eveningSnacks_list)
        dinnerListLayout = view.findViewById(R.id.layout_dinner_list)
        dailyCalorieGraphLayout = view.findViewById(R.id.dailyCalorieGraphLayout)
        calValueTv = view.findViewById(R.id.tv_cal_value)
        calValueMorningSnackTv = view.findViewById(R.id.tvCalValueMorningSnack)
        calValueLunchTv = view.findViewById(R.id.tv_lunch_cal_value)
        calValueEveningSnacksTv = view.findViewById(R.id.tv_cal_valueeveningSnacks)
        calValueDinnerTv = view.findViewById(R.id.tv_dinner_cal_value)
        noMealLogsLayout = view.findViewById(R.id.noMealLogsLayout)
        logMealTv = view.findViewById(R.id.logMealTv)
        maxCalUnit = view.findViewById(R.id.maxCalUnit)
        maxCarbUnit = view.findViewById(R.id.maxCarbUnit)
        maxProteinUnit = view.findViewById(R.id.maxProteinUnit)
        maxFatsUnit = view.findViewById(R.id.maxFatsUnit)
        progressBarLayout = view.findViewById(R.id.progressBarLayout)
        imageViewBelowOverlay = view.findViewById(R.id.imageViewBelowOverlay)
        layoutWeightLoss = view.findViewById(R.id.layoutWeightLoss)
        kcalStart = view.findViewById(R.id.kcalStart)
        kcalEnd = view.findViewById(R.id.kcalEnd)

        mealLogWeeklyDayRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,
            false)
        mealLogWeeklyDayRecyclerView.adapter = mealLogWeeklyDayAdapter

        breakfastMealRecyclerView.layoutManager = LinearLayoutManager(context)
        breakfastMealRecyclerView.adapter = breakfastMealLogsAdapter

        morningSnackMealsRecyclerView.layoutManager = LinearLayoutManager(context)
        morningSnackMealsRecyclerView.adapter = morningSnackMealLogsAdapter

        lunchMealRecyclerView.layoutManager = LinearLayoutManager(context)
        lunchMealRecyclerView.adapter = lunchMealLogsAdapter

        eveningSnacksMealRecyclerView.layoutManager = LinearLayoutManager(context)
        eveningSnacksMealRecyclerView.adapter = eveningSnacksMealLogsAdapter

        dinnerMealRecyclerView.layoutManager = LinearLayoutManager(context)
        dinnerMealRecyclerView.adapter = dinnerMealLogsAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (moduleName.contentEquals("EatRightLanding")){
                    val fragment = HomeBottomTabFragment()
                    val args = Bundle()
                    args.putString("ModuleName", "EatRight")
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }else if (moduleName.contentEquals("EatRightLandingWithoutPopup")){
                    val fragment = HomeBottomTabFragment()
                    val args = Bundle()
                    args.putString("ModuleName", "EatRight")
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }else if (moduleName.contentEquals("HomeDashboard")){
                    activity?.finish()
                }else{
                    val fragment = HomeBottomTabFragment()
                    val args = Bundle()
                    args.putString("ModuleName", "MoveRight")
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }
            }
        })

     //   val currentDateTime = LocalDateTime.now()
        val nowInstant = Instant.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = nowInstant.getMealLogDate(4)//currentDateTime.format(formatter)
        val formatFullDate = DateTimeFormatter.ofPattern("E, d MMM yyyy")
        val formattedDisplayDate  = nowInstant.getMealLogDisplayDate(4)

        if (selectedMealDate.equals("null") || selectedMealDate.equals("")){
            selectedMealDate = formattedDate
        }
        currentWeekStart = getStartOfWeek(LocalDate.parse(selectedMealDate, formatter))
        selectedWeeklyDayTv.text = formattedDisplayDate
        if (moduleName.contentEquals("HomeDashboard")){
            selectMealTypeBottomSheet = SelectMealTypeBottomSheet()
            selectMealTypeBottomSheet.isCancelable = true
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("selectedMealDate", selectedMealDate)
            selectMealTypeBottomSheet.arguments = args
            parentFragment.let { selectMealTypeBottomSheet.show(childFragmentManager, "SelectMealTypeBottomSheet") }
        }else if (moduleName.contentEquals("EatRightLanding")){
            selectMealTypeBottomSheet = SelectMealTypeBottomSheet()
            selectMealTypeBottomSheet.isCancelable = true
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("selectedMealDate", selectedMealDate)
            selectMealTypeBottomSheet.arguments = args
            parentFragment.let { selectMealTypeBottomSheet.show(childFragmentManager, "SelectMealTypeBottomSheet") }
        }else if (moduleName.contentEquals("MoveRightLanding")) {
            selectMealTypeBottomSheet = SelectMealTypeBottomSheet()
            selectMealTypeBottomSheet.isCancelable = true
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("selectedMealDate", selectedMealDate)
            selectMealTypeBottomSheet.arguments = args
            parentFragment.let { selectMealTypeBottomSheet.show(childFragmentManager, "SelectMealTypeBottomSheet") }
        }
        getMealsLogHistory(selectedMealDate)

        mealLogWeeklyDayList = getWeekFrom(currentWeekStart)
        lastDayOfCurrentWeek = mealLogWeeklyDayList.get(mealLogWeeklyDayList.size - 1).fullDate.toString()

        onMealLogWeeklyDayList(mealLogWeeklyDayList, mealLogHistory)
       prevWeekBtn.setOnClickListener {
            currentWeekStart = currentWeekStart.minusWeeks(1)
           mealLogWeeklyDayList = getWeekFrom(currentWeekStart)
           lastDayOfCurrentWeek = mealLogWeeklyDayList.get(mealLogWeeklyDayList.size - 1).fullDate.toString()

           selectedWeeklyDayTv.text = currentWeekStart.format(formatFullDate)
           selectedMealDate = currentWeekStart.toString()

           getMealsLogHistory(currentWeekStart.toString())

           onMealLogWeeklyDayList(mealLogWeeklyDayList, mealLogHistory)

           nextWeekBtn.setImageResource(R.drawable.ic_right_arrow_circle)
        }

       nextWeekBtn.setOnClickListener {
           val current = LocalDate.parse(formattedDate, formatter)
           val updated = LocalDate.parse(lastDayOfCurrentWeek, formatter)

//           if (current > updated) {
//               println("date1 is before date2")
//           } else if (date1 > date2) {
//               println("date1 is after date2")
//           } else {
//               println("Both dates are equal")
//           }
          // val currentDate : Int =  currentDateTime.dayOfMonth
           if (current > updated){
               currentWeekStart = currentWeekStart.plusWeeks(1)
               mealLogWeeklyDayList = getWeekFrom(currentWeekStart)
               lastDayOfCurrentWeek = mealLogWeeklyDayList.get(mealLogWeeklyDayList.size - 1).fullDate.toString()
               selectedWeeklyDayTv.text = currentWeekStart.format(formatFullDate)
               if (currentWeekStart == currentWeekStartItem){
                   selectedMealDate = formattedDate
               }else{
                   selectedMealDate = currentWeekStart.toString()
               }
               onMealLogWeeklyDayList(mealLogWeeklyDayList, mealLogHistory)
               getMealsLogHistory(currentWeekStart.toString())

               nextWeekBtn.setImageResource(R.drawable.ic_right_arrow_circle)
           }else{
               nextWeekBtn.setImageResource(R.drawable.right_arrow_grey)
               Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
           }
        }
        //getMealsLogList(formattedDate)

        //Set Tooltip
        val isShow = SharedPreferenceManager.getInstance(requireActivity()).isMealCalenderTooltipShowed("MealCalenderTooltip")
        if (!isShow){
          //  showTooltipDialog(layoutToolbar)
        }

        imageCalender.setOnClickListener {
          //  showTooltipDialog( layoutToolbar,"You can access Calender \n view from here.")
            val fragment = MealLogCalenderFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

        backButton.setOnClickListener {

            if (moduleName.contentEquals("EatRightLanding")){
                val fragment = HomeBottomTabFragment()
                val args = Bundle()
                args.putString("ModuleName", "EatRight")
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }else if (moduleName.contentEquals("EatRightLandingWithoutPopup")){
                val fragment = HomeBottomTabFragment()
                val args = Bundle()
                args.putString("ModuleName", "EatRight")
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }else if (moduleName.contentEquals("HomeDashboard")){
                activity?.finish()
            }else{
                val fragment = HomeBottomTabFragment()
                val args = Bundle()
                args.putString("ModuleName", "MoveRight")
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        }

        btnLogMeal.setOnClickListener {
            val current = LocalDate.parse(formattedDate, formatter)
            val updated = LocalDate.parse(selectedMealDate, formatter)
            if (current >= updated){
                selectMealTypeBottomSheet = SelectMealTypeBottomSheet()
                selectMealTypeBottomSheet.isCancelable = true
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("selectedMealDate", selectedMealDate)
                selectMealTypeBottomSheet.arguments = args
                parentFragment.let { selectMealTypeBottomSheet.show(childFragmentManager, "SelectMealTypeBottomSheet") }
            }else{
                Toast.makeText(context, "Not allowed to log meals for future dates", Toast.LENGTH_SHORT).show()
            }
        }

        breakfastDotMenu.setOnClickListener {
            if (editDeleteBreakfast.visibility == View.GONE){
                //layoutMain.setBackgroundColor(Color.parseColor("#0A1214"))
                editDeleteBreakfast.visibility = View.VISIBLE
            }else{
               // layoutMain.setBackgroundColor(Color.parseColor("#F0FFFA"))
                editDeleteBreakfast.visibility = View.GONE
            }
        }

        addBreakfastLayout.setOnClickListener {
            val fragment = HomeTabMealFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("mealType", "breakfast")
            args.putString("selectedMealDate", selectedMealDate)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

        addMorningSnackLayout.setOnClickListener {
            val fragment = HomeTabMealFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("mealType", "morning_snack")
            args.putString("selectedMealDate", selectedMealDate)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

        addLunchLayout.setOnClickListener {
            val fragment = HomeTabMealFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("mealType", "lunch")
            args.putString("selectedMealDate", selectedMealDate)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

        addEveningSnacksLayout.setOnClickListener {
            val fragment = HomeTabMealFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("mealType", "evening_snack")
            args.putString("selectedMealDate", selectedMealDate)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

        addDinnerLayout.setOnClickListener {
            val fragment = HomeTabMealFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("mealType", "dinner")
            args.putString("selectedMealDate", selectedMealDate)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

        lunchDotMenu.setOnClickListener {
            if (editDeleteLunch.visibility == View.GONE){
              //  layoutMain.setBackgroundColor(Color.parseColor("#0A1214"))
                editDeleteLunch.visibility = View.VISIBLE
            }else{
              //  layoutMain.setBackgroundColor(Color.parseColor("#F0FFFA"))
                editDeleteLunch.visibility = View.GONE
            }
        }

        dinnerDotMenu.setOnClickListener {
            if (editDeleteDinner.visibility == View.GONE){
              //  layoutMain.setBackgroundColor(Color.parseColor("#0A1214"))
                editDeleteDinner.visibility = View.VISIBLE
            }else{
              //  layoutMain.setBackgroundColor(Color.parseColor("#F0FFFA"))
                editDeleteDinner.visibility = View.GONE
            }
        }

        viewBFMealInsightLayout.setOnClickListener {
            if (breakFastMealDetailsLog != null){
                val fragment = ViewMealInsightsFragment()
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("selectedMealDate", selectedMealDate)
                args.putParcelable("mealDetailsLog", breakFastMealDetailsLog)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "viewMeal")
                    addToBackStack("viewMeal")
                    commit()
                }
            }
        }

        viewMSMealInsightLayout.setOnClickListener {
            if (morningSnackMealDetailsLog != null){
                val fragment = ViewMealInsightsFragment()
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("selectedMealDate", selectedMealDate)
                args.putParcelable("mealDetailsLog", morningSnackMealDetailsLog)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "viewMeal")
                    addToBackStack("viewMeal")
                    commit()
                }
            }
        }

        viewLunchMealInsightLayout.setOnClickListener {
            if (lunchMealDetailsLog != null){
                val fragment = ViewMealInsightsFragment()
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("selectedMealDate", selectedMealDate)
                args.putParcelable("mealDetailsLog", lunchMealDetailsLog)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "viewMeal")
                    addToBackStack("viewMeal")
                    commit()
                }
            }
        }

        viewESMealInsightLayout.setOnClickListener {
            if (eveningSnackMealDetailsLog != null){
                val fragment = ViewMealInsightsFragment()
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("selectedMealDate", selectedMealDate)
                args.putParcelable("mealDetailsLog", eveningSnackMealDetailsLog)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "viewMeal")
                    addToBackStack("viewMeal")
                    commit()
                }
            }
        }

        viewDinnerMealInsightLayout.setOnClickListener {
            if (dinnerMealDetailsLog != null){
                val fragment = ViewMealInsightsFragment()
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("selectedMealDate", selectedMealDate)
                args.putParcelable("mealDetailsLog", dinnerMealDetailsLog)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "viewMeal")
                    addToBackStack("viewMeal")
                    commit()
                }
            }
        }
    }

    private fun onMealLogWeeklyDayList(weekList: List<MealLogWeeklyDayModel>, mealLogHistory: ArrayList<LoggedMealHistory>) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val localDate = LocalDate.parse(selectedMealDate, formatter)
        val weekLists : ArrayList<MealLogWeeklyDayModel> = ArrayList()
        weekLists.clear()
        if (mealLogHistory.size > 0 && weekList.isNotEmpty()){
            mealLogHistory.forEach { mealLog ->
                for (item in weekList){
                    val todayDateTime = LocalDateTime.now()
                    val todayDate = todayDateTime.format(formatter)
                    if (item.fullDate.toString() == todayDate){
                        nextWeekBtn.setImageResource(R.drawable.right_arrow_grey)
                    }
                    if (item.fullDate.toString() == mealLog.date){
                        if (mealLog.isAvailable == true){
                           item.is_available = true
                        }else{
                            item.is_available = mealLog.isAvailable
                        }
                    }
                }
            }
        }

        if (weekList.isNotEmpty()){
            weekLists.addAll(weekList as Collection<MealLogWeeklyDayModel>)
            var mealLogDateData: MealLogWeeklyDayModel? = null
            var isClick = false
            var index = -1
            for (currentDay in weekLists){
                if (currentDay.fullDate == localDate){
                    mealLogDateData = currentDay
                    isClick = true
                     index = weekLists.indexOfFirst { it.fullDate == currentDay.fullDate }
                    break
                }
            }

            requireActivity()?.runOnUiThread {
                mealLogWeeklyDayAdapter.addAll(weekLists, index, mealLogDateData, isClick)
            }

            getMealsLogList(selectedMealDate)
        }
    }

    fun Instant.getMealLogDate(rolloverHour: Int = 4): String {
        val zone = ZoneId.systemDefault()
        val effectiveDate = this.effectiveDateForApi(rolloverHour, zone)
        return effectiveDate
            .atZone(zone)
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
    private fun Instant.effectiveDateForApi(
        rolloverHour: Int,
        zone: ZoneId
    ): Instant {
        val now = Instant.now()
        val thisDate = this.atZone(zone).toLocalDate()
        val today = now.atZone(zone).toLocalDate()
        // ✅ Only apply rollover if THIS date is today
        if (thisDate != today) {
            return thisDate.atStartOfDay(zone).toInstant()
        }
        val currentHour = now.atZone(zone).hour
        return if (currentHour < rolloverHour) {
            // before rollover → yesterday
            today
                .minusDays(1)
                .atStartOfDay(zone)
                .toInstant()
        } else {
            // after rollover → today
            today
                .atStartOfDay(zone)
                .toInstant()
        }
    }

    fun Instant.getMealLogDisplayDate(rolloverHour: Int = 4): String {
        val zone = ZoneId.systemDefault()
        val effective = this.effectiveDateForApi(rolloverHour, zone)
        return effective
            .atZone(zone)
            .format(DateTimeFormatter.ofPattern("E, d MMM yyyy"))
    }

    private fun onMealLogDateItem(mealLogWeeklyDayModel: MealLogWeeklyDayModel, position: Int, isRefresh: Boolean) {

        val formatter = DateTimeFormatter.ofPattern("E, d MMM yyyy")
        selectedWeeklyDayTv.text = mealLogWeeklyDayModel.fullDate.format(formatter)
        selectedMealDate = mealLogWeeklyDayModel.fullDate.toString()

        val weekLists : ArrayList<MealLogWeeklyDayModel> = ArrayList()
        weekLists.addAll(mealLogWeeklyDayList as Collection<MealLogWeeklyDayModel>)
        mealLogWeeklyDayAdapter.addAll(weekLists, position, mealLogWeeklyDayModel, isRefresh)

        val seleteddate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = mealLogWeeklyDayModel.fullDate.format(seleteddate)
        getMealsLogList(formattedDate)
    }

    private fun setGraphValue(dailyRecipe: FullDaySummary?, maxData: MaxMacros?){
        activity?.runOnUiThread {
            var maxCalorie = 0
            var maxCarbs = 0
            var maxProtein = 0
            var maxFats = 0
            if (maxData != null){
                maxCalorie = maxData.calories.toInt()
                maxCarbs = maxData.carbs.toInt()
                maxProtein = maxData.protein.toInt()
                maxFats = maxData.fat.toInt()
            }

            val currentCal = dailyRecipe?.calories_kcal?.let { round(it).toInt() } ?: 0

// Check if current calories is greater than max
            if (currentCal > maxCalorie) {
                calValue.setTextColor(Color.parseColor("#B50200"))
            } else {
                calValue.setTextColor(Color.BLACK)
            }

            calValue.text = currentCal.toString()
            val currentCarbs = dailyRecipe?.carbs_g?.let { round(it)?.toInt() } ?: 0

// Check if current carbs is greater than max
            if (currentCarbs > maxCarbs) {
                carbsValue.setTextColor(Color.parseColor("#B50200"))
            } else {
                carbsValue.setTextColor(Color.BLACK)
            }

            carbsValue.text = currentCarbs.toString()
            val currentProtein = dailyRecipe?.protein_g?.let { round(it)?.toInt() } ?: 0

// Check if current protein is greater than max
            if (currentProtein > maxProtein) {
                proteinsValue.setTextColor(Color.parseColor("#B50200"))
            } else {
                proteinsValue.setTextColor(Color.BLACK)
            }

            proteinsValue.text = currentProtein.toString()
            val currentFats = dailyRecipe?.fat_g?.let { round(it)?.toInt() } ?: 0

// Check if current fats is greater than max
            if (currentFats > maxFats) {
                fatsValue.setTextColor(Color.parseColor("#B50200"))
            } else {
                fatsValue.setTextColor(Color.BLACK)
            }

            fatsValue.text = currentFats.toString()
            maxCalUnit.text = " / " + maxCalorie.toString() + " kCal"
            maxCarbUnit.text = " / " + maxCarbs.toString() + " g"
            maxProteinUnit.text = " / " + maxProtein.toString() + " g"
            maxFatsUnit.text = " / " + maxFats.toString() + " g"

            caloriesProgressBar.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    caloriesProgressBar.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val progressBarWidth = caloriesProgressBar.width.toFloat()
                    val overlayPosition = 0.7f * progressBarWidth
                    val progress = dailyRecipe?.calories_kcal?.toInt()
                    caloriesProgressBar.progress = progress!!
                    val progressPercentage = caloriesProgressBar.max
                    caloriesProgressBar.max = maxCalorie
                    val circlePosition = (progress!!.toFloat() / progressPercentage) * progressBarWidth
//                val circleRadius = circleIndicator.width / 2f
//                circleIndicator.x = circlePosition - circleRadius
//                circleIndicator.y = progressBar.y + (progressBar.height - circleIndicator.height) / 2f
                    val overlayRadius = transparentOverlay.width / 2f
                    transparentOverlay.x = overlayPosition - overlayRadius
                    transparentOverlay.y = caloriesProgressBar.y + (caloriesProgressBar.height - transparentOverlay.height) / 2f

                    val overlayImage = layoutWeightLoss.width / 2f
                    layoutWeightLoss.x = overlayPosition - 40

                    val kcalsStart = kcalStart.width / 2f
                    kcalStart.x = overlayPosition - 90

                    val kcalsEnd = kcalEnd.width / 2f
                    kcalEnd.x = overlayPosition + 40
                }
            })
            // Set progress programmatically
            carbsProgressBar.max = maxCarbs  // Set maximum value
            carbsProgressBar.progress = dailyRecipe?.carbs_g!!.toInt()

            // Set progress programmatically
            proteinsProgressBar.max = maxProtein  // Set maximum value
            proteinsProgressBar.progress = dailyRecipe?.protein_g!!.toInt()

            // Set progress programmatically
            fatsProgressBar.max = maxFats  // Set maximum value
            fatsProgressBar.progress = dailyRecipe?.fat_g!!.toInt()
        }
    }

    private fun setDayLogsList(){

        val regularRecipeData : RegularRecipeEntry? = null
        val snapMealData : SnapMeal? = null
        activity?.runOnUiThread {
            if (breakfastCombinedList.size > 0) {
                breakfastListLayout.visibility = View.VISIBLE
                if (breakfastMealNutritionSummary.size > 0) {
                    calValueTv.text = breakfastMealNutritionSummary.get(0).calories_kcal.toInt().toString()
                }
                breakfastMealLogsAdapter.updateList(breakfastCombinedList, -1, regularRecipeData, snapMealData, false)
            } else {
                breakfastListLayout.visibility = View.GONE
            }
        }

        activity?.runOnUiThread {
            if (morningSnackCombinedList.size > 0) {
                morningSnackListLayout.visibility = View.VISIBLE
                morningSnackMealLogsAdapter.addAll(morningSnackCombinedList, -1, regularRecipeData, snapMealData, false)
                if (morningSnackMealNutritionSummary.size > 0) {
                    calValueMorningSnackTv.text =
                        morningSnackMealNutritionSummary.get(0).calories_kcal.toInt().toString()
                }
            } else {
                morningSnackListLayout.visibility = View.GONE
            }
        }
        activity?.runOnUiThread {
            if (lunchCombinedList.size > 0) {
                lunchListLayout.visibility = View.VISIBLE
                lunchMealLogsAdapter.addAll(lunchCombinedList, -1, regularRecipeData, snapMealData, false)
                if (lunchMealNutritionSummary.size > 0) {
                    calValueLunchTv.text =
                        lunchMealNutritionSummary.get(0).calories_kcal.toInt().toString()
                }
            } else {
                lunchListLayout.visibility = View.GONE
            }
        }
        activity?.runOnUiThread {
            if (eveningSnacksCombinedList.size > 0) {
                eveningSnacksListLayout.visibility = View.VISIBLE
                eveningSnacksMealLogsAdapter.addAll(eveningSnacksCombinedList, -1, regularRecipeData, snapMealData, false)
                if (eveningSnacksMealNutritionSummary.size > 0) {
                    calValueEveningSnacksTv.text =
                        eveningSnacksMealNutritionSummary.get(0).calories_kcal.toInt().toString()
                }
            } else {
                eveningSnacksListLayout.visibility = View.GONE
            }
        }
        activity?.runOnUiThread {
            if  (dinnerCombinedList.size > 0){
                dinnerListLayout.visibility = View.VISIBLE
                dinnerMealLogsAdapter.addAll(dinnerCombinedList, -1, regularRecipeData, snapMealData, false)
                if (dinnerMealNutritionSummary.size > 0){
                    calValueDinnerTv.text = dinnerMealNutritionSummary.get(0).calories_kcal.toInt().toString()
                }
            }else{
                dinnerListLayout.visibility = View.GONE
            }
        }
    }

    private fun onBreakFastRegularRecipeDeleteItem(mealItem: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
        deleteLogDishDialog(mealItem, "RegularRecipe")
    }

    private fun onBreakFastRegularRecipeEditItem(foodData: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
             var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
             val dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
            val fragment = DishLogEditFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("mealId", foodData.meal_id)
            args.putString("recipeId", foodData.recipe.id)
            args.putString("mealType", "breakfast")
            args.putString("snapRecipeName", foodData.recipe.recipe)
            args.putString("selectedMealDate", selectedMealDate)
            dishLists.add(foodData.recipe)
            recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            replace(R.id.flFragment, fragment, "Steps")
            addToBackStack(null)
            commit()
        }
    }

    private fun onBreakFastSnapMealDeleteItem(mealItem: SnapMeal, position: Int, isRefresh: Boolean) {
        deleteSnapLogDishDialog(mealItem, "SnapMeal")
    }

    private fun onBreakFastSnapMealEditItem(snapMealDetail: SnapMeal, position: Int, isRefresh: Boolean) {
        if (snapMealDetail != null){
            var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
            val dishList = snapMealDetail.dish
            val dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
            dishLists.addAll(dishList!!)
            recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
            val fragment = MealScanResultFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("mealId", snapMealDetail._id)
            args.putString("mealType", "Breakfast")
            args.putString("snapMealLog", "snapMealLog")
            args.putString("snapImageUrl", snapMealDetail.image_url)
            args.putString("mealName", snapMealDetail.meal_name)
            args.putString("selectedMealDate", selectedMealDate)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }
    }

    private fun onMSRegularRecipeDeleteItem(mealItem: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
        deleteLogDishDialog(mealItem, "RegularRecipe")
    }

    private fun onMSRegularRecipeEditItem(foodData: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
            val dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
            val fragment = DishLogEditFragment()
            val args = Bundle()
            val mealNames = foodData.recipe.recipe.takeIf { r -> !r.isNullOrBlank() } ?: foodData.recipe.food_name
            args.putString("ModuleName", moduleName)
            args.putString("mealId", foodData.meal_id)
            args.putString("recipeId", foodData.recipe.id)
            args.putString("mealType", "morning_snack")
            args.putString("snapRecipeName", mealNames)
            args.putString("selectedMealDate", selectedMealDate)
            dishLists.add(foodData.recipe)
            recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            replace(R.id.flFragment, fragment, "Steps")
            addToBackStack(null)
            commit()
        }
    }

    private fun onMSSnapMealDeleteItem(mealItem: SnapMeal, position: Int, isRefresh: Boolean) {
        deleteSnapLogDishDialog(mealItem, "SnapMeal")
    }

    private fun onMSSnapMealEditItem(snapMealDetail: SnapMeal, position: Int, isRefresh: Boolean) {
        if (snapMealDetail != null){
            var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
            val dishList = snapMealDetail.dish
            val dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
            dishLists.addAll(dishList!!)
            recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
            val fragment = MealScanResultFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("mealId", snapMealDetail._id)
            args.putString("mealType", "Morning Snack")
            args.putString("snapMealLog", "snapMealLog")
            args.putString("snapImageUrl", snapMealDetail.image_url)
            args.putString("mealName", snapMealDetail.meal_name)
            args.putString("selectedMealDate", selectedMealDate)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }
    }

    private fun onLunchRegularRecipeDeleteItem(mealItem: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
        deleteLogDishDialog(mealItem, "RegularRecipe")
    }

    private fun onLunchRegularRecipeEditItem(foodData: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
            val dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
            val fragment = DishLogEditFragment()
            val args = Bundle()
            val mealNames = foodData.recipe.recipe.takeIf { r -> !r.isNullOrBlank() } ?: foodData.recipe.food_name
            args.putString("ModuleName", moduleName)
            args.putString("mealId", foodData.meal_id)
            args.putString("recipeId", foodData.recipe.id)
            args.putString("mealType", "lunch")
            args.putString("snapRecipeName", mealNames)
            args.putString("selectedMealDate", selectedMealDate)
            dishLists.add(foodData.recipe)
            recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            replace(R.id.flFragment, fragment, "Steps")
            addToBackStack(null)
            commit()
        }

    }

    private fun onLunchSnapMealDeleteItem(mealItem: SnapMeal, position: Int, isRefresh: Boolean) {
        deleteSnapLogDishDialog(mealItem, "SnapMeal")
    }

    private fun onLunchSnapMealEditItem(snapMealDetail: SnapMeal, position: Int, isRefresh: Boolean) {
        if (snapMealDetail != null){
            var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
            val dishList = snapMealDetail.dish
            val dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
             dishLists.addAll(dishList!!)
            recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
            val fragment = MealScanResultFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("mealId", snapMealDetail._id)
            args.putString("mealType", "Lunch")
            args.putString("snapMealLog", "snapMealLog")
            args.putString("snapImageUrl", snapMealDetail.image_url)
            args.putString("mealName", snapMealDetail.meal_name)
            args.putString("selectedMealDate", selectedMealDate)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }
    }

    private fun onESRegularRecipeDeleteItem(mealItem: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
        deleteLogDishDialog(mealItem, "RegularRecipe")
    }

    private fun onESRegularRecipeEditItem(foodData: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
            val dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
            val fragment = DishLogEditFragment()
            val args = Bundle()
            val mealNames = foodData.recipe.recipe.takeIf { r -> !r.isNullOrBlank() } ?: foodData.recipe.food_name
            args.putString("ModuleName", moduleName)
            args.putString("mealId", foodData.meal_id)
            args.putString("recipeId", foodData.recipe.id)
            args.putString("mealType", "evening_snack")
            args.putString("snapRecipeName", mealNames)
            args.putString("selectedMealDate", selectedMealDate)
            dishLists.add(foodData.recipe)
            recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            replace(R.id.flFragment, fragment, "Steps")
            addToBackStack(null)
            commit()
        }
    }

    private fun onESSnapMealDeleteItem(mealItem: SnapMeal, position: Int, isRefresh: Boolean) {
        deleteSnapLogDishDialog(mealItem, "SnapMeal")
    }

    private fun onESSnapMealEditItem(snapMealDetail: SnapMeal, position: Int, isRefresh: Boolean) {
        if (snapMealDetail != null){
            var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
            val dishList = snapMealDetail.dish
            val dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
            dishLists.addAll(dishList!!)
            recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
            val fragment = MealScanResultFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("mealId", snapMealDetail._id)
            args.putString("mealType", "Evening Snacks")
            args.putString("snapMealLog", "snapMealLog")
            args.putString("snapImageUrl", snapMealDetail.image_url)
            args.putString("mealName", snapMealDetail.meal_name)
            args.putString("selectedMealDate", selectedMealDate)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }
    }

    private fun onDinnerRegularRecipeDeleteItem(mealItem: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
        deleteLogDishDialog(mealItem, "RegularRecipe")
    }

    private fun onDinnerRegularRecipeEditItem(foodData: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
            val dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
            val fragment = DishLogEditFragment()
            val args = Bundle()
            val mealNames = foodData.recipe.recipe.takeIf { r -> !r.isNullOrBlank() } ?: foodData.recipe.food_name
            args.putString("ModuleName", moduleName)
            args.putString("mealId", foodData.meal_id)
            args.putString("recipeId", foodData.recipe.id)
            args.putString("mealType", "dinner")
            args.putString("snapRecipeName", mealNames)
            args.putString("selectedMealDate", selectedMealDate)
            dishLists.add(foodData.recipe)
            recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            replace(R.id.flFragment, fragment, "Steps")
            addToBackStack(null)
            commit()
        }
    }

    private fun onDinnerSnapMealDeleteItem(mealItem: SnapMeal, position: Int, isRefresh: Boolean) {
        deleteSnapLogDishDialog(mealItem, "SnapMeal")
    }

    private fun onDinnerSnapMealEditItem(snapMealDetail: SnapMeal, position: Int, isRefresh: Boolean) {
        if (snapMealDetail != null){
            var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
            val dishList = snapMealDetail.dish
            val dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
             dishLists.addAll(dishList!!)
            recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
            val fragment = MealScanResultFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("mealId", snapMealDetail._id)
            args.putString("mealType", "Dinner")
            args.putString("snapMealLog", "snapMealLog")
            args.putString("snapImageUrl", snapMealDetail.image_url)
            args.putString("mealName", snapMealDetail.meal_name)
            args.putString("selectedMealDate", selectedMealDate)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }
    }

    private fun deleteLogDishDialog(mealItem: RegularRecipeEntry, deleteType: String) {
       val deleteBottomSheetFragment = DeleteLogDishBottomSheet()
        deleteBottomSheetFragment.isCancelable = true
        val args = Bundle()
        args.putString("mealId", mealItem.meal_id)
        args.putString("recipeId", mealItem.recipe.id)
        args.putString("deleteType", deleteType)
        deleteBottomSheetFragment.arguments = args
        parentFragment.let { deleteBottomSheetFragment.show(childFragmentManager, "DeleteLogDishBottomSheet") }
    }

    private fun deleteSnapLogDishDialog(mealItem: SnapMeal, deleteType: String) {
        val deleteBottomSheetFragment = DeleteLogDishBottomSheet()
        deleteBottomSheetFragment.isCancelable = true
        val args = Bundle()
        args.putString("mealId", mealItem._id)
       // args.putString("recipeId", mealItem.)
        args.putString("deleteType", deleteType)
        deleteBottomSheetFragment.arguments = args
        parentFragment.let { deleteBottomSheetFragment.show(childFragmentManager, "DeleteLogDishBottomSheet") }
    }

    private fun showTooltipDialog(anchorView: View) {
        dialogToolTip = Dialog(requireContext())
        dialogToolTip?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogToolTip?.setContentView(R.layout.tooltip_layout)
        val tvTooltip = dialogToolTip?.findViewById<TextView>(R.id.tvTooltipText)
        tvTooltip?.text = "You can access Calender \n view from here."
        // Set transparent background for rounded tooltip
        dialogToolTip?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Get screen dimensions
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        // Get anchor view position
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        // Tooltip width (adjust as needed)
        val tooltipWidth = 250
        // Set dialog position
        val params = dialogToolTip?.window?.attributes
        // Align tooltip to the **right** of the button
        params?.x = (location[0] + anchorView.width) + tooltipWidth
        // Position tooltip **above** the button
        params?.y = location[1] - anchorView.height + 15  // Add some spacing
        dialogToolTip?.window?.attributes = params
        dialogToolTip?.window?.setGravity(Gravity.TOP)
        // Show the dialog
        dialogToolTip?.show()
        // Auto dismiss after 3 seconds
//        Handler(Looper.getMainLooper()).postDelayed({
//            dialog.dismiss()
            SharedPreferenceManager.getInstance(context).saveMealCalenderTooltip("MealCalenderTooltip",true)
//        }, 3000)
    }

    private fun getMealsLogList(formattedDate: String) {
        if (isAdded && view != null) {
            context?.let { showLoader(requireView()) }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
            val call = ApiClient.apiServiceFastApiV2.getMealsLogByDate(userId, formattedDate)
            call.enqueue(object : Callback<MealLogDataResponse> {
                override fun onResponse(call: Call<MealLogDataResponse>, response: Response<MealLogDataResponse>) {
                    if (!isAdded || view == null) return
                    val ctx = context ?: return
                    dismissLoader(requireView())
                    if (response.isSuccessful && response.body()?.data != null) {
                        if (response.body()?.data != null){
                            selectedDate = response.body()?.data!!.date
                            val breakfastRecipes = response.body()?.data!!.meal_detail["breakfast"]?.regular_receipes
                            val morningSnackRecipes = response.body()?.data!!.meal_detail["morning_snack"]?.regular_receipes
                            val lunchSnapRecipes = response.body()?.data!!.meal_detail["lunch"]?.regular_receipes
                            val eveningSnacksRecipes = response.body()?.data!!.meal_detail["evening_snack"]?.regular_receipes
                            val dinnerRecipes = response.body()?.data!!.meal_detail["dinner"]?.regular_receipes

                            val breakfastSnapMeals = response.body()?.data!!.meal_detail["breakfast"]?.snap_meals
                            val morningSnackSnapMeals = response.body()?.data!!.meal_detail["morning_snack"]?.snap_meals
                            val lunchSnapSnapMeals = response.body()?.data!!.meal_detail["lunch"]?.snap_meals
                            val eveningSnacksSnapMeals = response.body()?.data!!.meal_detail["evening_snack"]?.snap_meals
                            val dinnerSnapMeals = response.body()?.data!!.meal_detail["dinner"]?.snap_meals

                            val breakfastMealNutrition = response.body()?.data!!.meal_detail["breakfast"]?.meal_nutrition_summary
                            val morningSnackMealNutrition = response.body()?.data!!.meal_detail["morning_snack"]?.meal_nutrition_summary
                            val lunchMealNutrition = response.body()?.data!!.meal_detail["lunch"]?.meal_nutrition_summary
                            val eveningSnacksMealNutrition = response.body()?.data!!.meal_detail["evening_snack"]?.meal_nutrition_summary
                            val dinnerMealNutrition = response.body()?.data!!.meal_detail["dinner"]?.meal_nutrition_summary
                            val breakFastViewItem = response.body()?.data!!.meal_detail["breakfast"]
                            val morningSnackViewItem = response.body()?.data!!.meal_detail["morning_snack"]
                            val lunchViewItem = response.body()?.data!!.meal_detail["lunch"]
                            val eveningSnackViewItem = response.body()?.data!!.meal_detail["evening_snack"]
                            val dinnerViewItem = response.body()?.data!!.meal_detail["dinner"]
                            if (breakFastViewItem != null){
                                breakFastMealDetailsLog = breakFastViewItem
                            }
                            if (morningSnackViewItem != null){
                                morningSnackMealDetailsLog = morningSnackViewItem
                            }
                            if (lunchViewItem != null){
                                lunchMealDetailsLog = lunchViewItem
                            }
                            if (eveningSnackViewItem != null){
                                eveningSnackMealDetailsLog = eveningSnackViewItem
                            }
                            if (dinnerViewItem != null){
                                dinnerMealDetailsLog = dinnerViewItem
                            }

                            breakfastCombinedList.clear()
                            morningSnackCombinedList.clear()
                            lunchCombinedList.clear()
                            eveningSnacksCombinedList.clear()
                            dinnerCombinedList.clear()
                            breakfastMealNutritionSummary.clear()
                            morningSnackMealNutritionSummary.clear()
                            lunchMealNutritionSummary.clear()
                            eveningSnacksMealNutritionSummary.clear()
                            dinnerMealNutritionSummary.clear()

                            if (breakfastMealNutrition != null) {
                                if (breakfastMealNutrition.size > 0) {
                                    breakfastMealNutritionSummary.addAll(breakfastMealNutrition)
                                }
                            }

                            if (morningSnackMealNutrition != null) {
                                if (morningSnackMealNutrition.size > 0) {
                                    morningSnackMealNutritionSummary.addAll(morningSnackMealNutrition)
                                }
                            }

                            if (lunchMealNutrition != null) {
                                if (lunchMealNutrition.size > 0) {
                                    lunchMealNutritionSummary.addAll(lunchMealNutrition)
                                }
                            }

                            if (eveningSnacksMealNutrition != null) {
                                if (eveningSnacksMealNutrition.size > 0) {
                                    eveningSnacksMealNutritionSummary.addAll(eveningSnacksMealNutrition)
                                }
                            }

                            if (dinnerMealNutrition != null) {
                                if (dinnerMealNutrition.size > 0) {
                                    dinnerMealNutritionSummary.addAll(dinnerMealNutrition)
                                }
                            }

                            if (breakfastRecipes != null){
                                if (breakfastRecipes.size > 0){
                                    breakfastCombinedList.addAll(breakfastRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                                }
                            }
                            if (breakfastSnapMeals != null){
                                if (breakfastSnapMeals.size > 0){
                                    breakfastCombinedList.addAll(breakfastSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                                }
                            }
                            if (morningSnackRecipes != null){
                                if (morningSnackRecipes.size > 0){
                                    morningSnackCombinedList.addAll(morningSnackRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                                }
                            }
                            if (morningSnackSnapMeals != null){
                                if (morningSnackSnapMeals.size > 0){
                                    morningSnackCombinedList.addAll(morningSnackSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                                }
                            }
                            if (lunchSnapRecipes != null){
                                if (lunchSnapRecipes.size > 0){
                                    lunchCombinedList.addAll(lunchSnapRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                                }
                            }
                            if (lunchSnapSnapMeals != null){
                                if (lunchSnapSnapMeals.size > 0){
                                    lunchCombinedList.addAll(lunchSnapSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                                }
                            }
                            if (eveningSnacksRecipes != null){
                                if (eveningSnacksRecipes.size > 0){
                                    eveningSnacksCombinedList.addAll(eveningSnacksRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                                }
                            }
                            if (eveningSnacksSnapMeals != null){
                                if (eveningSnacksSnapMeals.size > 0){
                                    eveningSnacksCombinedList.addAll(eveningSnacksSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                                }
                            }
                            if (dinnerRecipes != null) {
                                if (dinnerRecipes.size > 0){
                                    dinnerCombinedList.addAll(dinnerRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                                }
                            }
                            if (dinnerSnapMeals != null){
                                if (dinnerSnapMeals.size > 0){
                                    dinnerCombinedList.addAll(dinnerSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                                }
                            }
                            val fullDaySummary = response.body()?.data!!.full_day_summary
                            val maxData = response.body()?.data?.max_macros

                            requireActivity()?.runOnUiThread {
                                if (fullDaySummary.calories_kcal != null){
                                    noMealLogsLayout.visibility = View.GONE
                                    dailyCalorieGraphLayout.visibility = View.VISIBLE
                                    setGraphValue(fullDaySummary,maxData )
                                    if (response.body()?.data!!.meal_detail.isNotEmpty()){
                                        logMealTv.text = "Log New Meal"
                                        setDayLogsList()
                                    }else{
                                        noMealLogsLayout.visibility = View.VISIBLE
                                        dailyCalorieGraphLayout.visibility = View.GONE
                                        breakfastListLayout.visibility = View.GONE
                                        morningSnackListLayout.visibility = View.GONE
                                        lunchListLayout.visibility = View.GONE
                                        eveningSnacksListLayout.visibility = View.GONE
                                        dinnerListLayout.visibility = View.GONE
                                    }
                                }else{
                                    noMealLogsLayout.visibility = View.VISIBLE
                                    dailyCalorieGraphLayout.visibility = View.GONE
                                    breakfastListLayout.visibility = View.GONE
                                    morningSnackListLayout.visibility = View.GONE
                                    lunchListLayout.visibility = View.GONE
                                    eveningSnacksListLayout.visibility = View.GONE
                                    dinnerListLayout.visibility = View.GONE
                                }
                            }
                        }
                    } else {
                        Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                          //  val ctx = context ?: return
                            Toast.makeText(ctx, "No meal logs found for this user", Toast.LENGTH_SHORT).show()
                            if (isAdded  && view != null){
                                dismissLoader(requireView())
                                dailyCalorieGraphLayout.visibility = View.GONE
                                breakfastListLayout.visibility = View.GONE
                                morningSnackListLayout.visibility = View.GONE
                                lunchListLayout.visibility = View.GONE
                                eveningSnacksListLayout.visibility = View.GONE
                                dinnerListLayout.visibility = View.GONE
                                noMealLogsLayout.visibility = View.VISIBLE
                                logMealTv.text = "Log Your Meal"
                            }
                    }
                }
                override fun onFailure(call: Call<MealLogDataResponse>, t: Throwable) {
                    Log.e("Error", "API call failed: ${t.message}")
                    if (!isAdded || view == null) return
                    val ctx = context ?: return
                    Toast.makeText(ctx, "Failure", Toast.LENGTH_SHORT).show()
                    dismissLoader(requireView())
                }
            })
        }
    }

    private fun getMealsLogHistory(formattedDate: String) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val call = ApiClient.apiServiceFastApiV2.getMealsLogHistory(userId, formattedDate)
        call.enqueue(object : Callback<MealLogsHistoryResponse> {
            override fun onResponse(call: Call<MealLogsHistoryResponse>, response: Response<MealLogsHistoryResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    if (response.body() != null){
                        mealLogHistory.clear()
                        mealLogsHistoryResponse = response.body()
                        requireActivity()?.runOnUiThread {
                            if (mealLogsHistoryResponse?.loggedMealList!!.size > 0){
                                mealLogHistory.addAll(mealLogsHistoryResponse!!.loggedMealList!!)
                                onMealLogWeeklyDayList(mealLogWeeklyDayList, mealLogHistory)
                            }
                        }
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }
            override fun onFailure(call: Call<MealLogsHistoryResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }

    private fun getStartOfWeek(date: LocalDate): LocalDate {
        return date.with(java.time.DayOfWeek.MONDAY)
    }

    private fun getWeekFrom(startDate: LocalDate): List<MealLogWeeklyDayModel> {
        return (0..6).map { i ->
            val date = startDate.plusDays(i.toLong())
            MealLogWeeklyDayModel(
                dayName = date.dayOfWeek.name.take(1), // M, T, W...
                dayNumber = date.dayOfMonth.toString(),
                fullDate = date,
            )
        }
    }

    override fun onLogDishDeleted(mealData: String) {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val formatFullDate = DateTimeFormatter.ofPattern("E, d MMM yyyy")
//        selectedWeeklyDayTv.text = currentDateTime.format(formatFullDate)
        //selectedMealDate = formattedDate
        getMealsLogHistory(selectedDate)
        getMealsLogList(selectedDate)
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    override fun onMealTypeSelected(mealType: String) {
       // dialogToolTip?.dismiss()
    }
}