package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MacroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MicroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MacroNutrientsModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MicroNutrientsModel
import com.jetsynthesys.rightlife.databinding.FragmentDishBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.view.isVisible
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.request.DishLogItem
import com.jetsynthesys.rightlife.ai_package.model.request.IngredientLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.RecipeLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.SaveDishLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.UpdateDishLogRequest
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails
import com.jetsynthesys.rightlife.ai_package.model.response.MealUpdateResponse
import com.jetsynthesys.rightlife.ai_package.model.response.Serving
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeDetailsLocalListModel
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class DishLogEditFragment : BaseFragment<FragmentDishBinding>() {

    private lateinit var macroItemRecyclerView : RecyclerView
    private lateinit var microItemRecyclerView : RecyclerView
    private lateinit var addToTheMealLayout : LinearLayoutCompat
    private lateinit var selectDateMealTypeTitleLayout : LinearLayoutCompat
    private lateinit var selectDateMealTypeLayout : LinearLayoutCompat
    private lateinit var tvSelectedDate: TextView
    private lateinit var layoutMicroTitle : ConstraintLayout
    private lateinit var layoutMacroTitle : ConstraintLayout
    private lateinit var icMacroUP : ImageView
    private lateinit var microUP : ImageView
    private lateinit var imgFood : ImageView
    private lateinit var btnLogMeal : LinearLayoutCompat
    private lateinit var editDeleteBreakfast : CardView
    private lateinit var tvMealName : TextView
    private lateinit var addToTheMealTV : TextView
    private lateinit var logMealTitle : TextView
    private lateinit var layoutMain : ConstraintLayout
    private lateinit var searchType : String
    private lateinit var recipeName : String
    private lateinit var recipeImage : String
    private lateinit var quantityEdit: EditText
    private lateinit var tvCheckOutRecipe: TextView
    private lateinit var tvChange: TextView
    private lateinit var tvQuantity: TextView
    private var quantity = 1
    private lateinit var ivMealDropdown : ImageView
    private lateinit var ivEdit : ImageView
    private lateinit var tvMeasure :TextView
    private lateinit var backButton : ImageView
    private lateinit var spinner: Spinner
    private lateinit var spinnerMealType: Spinner
    private var measureType : String = ""
    private var selectedDefaultValue : Double? = 0.0
    private var dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
    private var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
    private var mealId : String = ""
    private var mealName : String = ""
    private var moduleName : String = ""
    private var loadingOverlay : FrameLayout? = null
    private var isSpinnerInitialized = false

    private var isSpinnerMealTypeInitialized = false
    private var defaultServing: Serving? = null
    private var selectedMealDate : String = ""
    private lateinit var selectedMealType: String
    private var userSelectedServing: Serving? = null
    private var mealType : String = ""
    private var isDataChanged = false
    private var initialServingType: String = ""
    private var initialServingValue: Double = 0.0


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDishBinding
        get() = FragmentDishBinding::inflate

    private val macroNutrientsAdapter by lazy { MacroNutrientsAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onMacroNutrientsItemClick) }
    private val microNutrientsAdapter by lazy { MicroNutrientsAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onMicroNutrientsItemClick) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        macroItemRecyclerView = view.findViewById(R.id.recyclerview_macro_item)
        microItemRecyclerView = view.findViewById(R.id.recyclerview_micro_item)
        addToTheMealLayout = view.findViewById(R.id.layout_addToTheMeal)
        tvCheckOutRecipe = view.findViewById(R.id.tv_CheckOutRecipe)
        tvChange = view.findViewById(R.id.tv_change)
     //   tvQuantity = view.findViewById(R.id.tvQuantity)
        tvMeasure = view.findViewById(R.id.tvMeasure)
        addToTheMealTV = view.findViewById(R.id.tv_addToTheMeal)
        logMealTitle = view.findViewById(R.id.tv_log_meal_title)
        tvMealName = view.findViewById(R.id.tvMealName)
        imgFood = view.findViewById(R.id.imgFood)
        layoutMicroTitle = view.findViewById(R.id.layoutMicroTitle)
        layoutMacroTitle = view.findViewById(R.id.layoutMacroTitle)
        microUP = view.findViewById(R.id.microUP)
        icMacroUP = view.findViewById(R.id.icMacroUP)
        quantityEdit = view.findViewById(R.id.quantityEdit)
        ivEdit = view.findViewById(R.id.ivEdit)
        backButton = view.findViewById(R.id.backButton)
        spinner = view.findViewById(R.id.spinner)
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate)
        spinnerMealType = view.findViewById(R.id.spinnerMealType)
        ivMealDropdown = view.findViewById(R.id.ivMealDropdown)
        selectDateMealTypeTitleLayout = view.findViewById(R.id.selectDateMealTypeTitleLayout)
        selectDateMealTypeLayout = view.findViewById(R.id.selectDateMealTypeLayout)
        addToTheMealLayout.isEnabled = false
        addToTheMealLayout.alpha = 0.5f

        selectDateMealTypeTitleLayout.visibility = View.VISIBLE
        selectDateMealTypeLayout.visibility = View.VISIBLE

        moduleName = arguments?.getString("ModuleName").toString()
        mealId = arguments?.getString("mealId").toString()
        mealType = arguments?.getString("mealType").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()
      //  mealName = arguments?.getString("mealName").toString()
        val snapRecipeName = arguments?.getString("snapRecipeName").toString()

        val recipeDetailsLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapDishLocalListModel", RecipeDetailsLocalListModel::class.java)
        } else {
            arguments?.getParcelable("snapDishLocalListModel")
        }

        if (recipeDetailsLocalListModels != null) {
            recipeDetailsLocalListModel = recipeDetailsLocalListModels
            if (recipeDetailsLocalListModel?.data != null){
                if (recipeDetailsLocalListModel!!.data.size > 0){
                    dishLists.addAll(recipeDetailsLocalListModel!!.data)
                }
            }
        }

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatFullDate = DateTimeFormatter.ofPattern("d MMMM yyyy")
        val items = arrayOf("Breakfast", "Morning Snack", "Lunch", "Evening Snacks", "Dinner")
        // Create Adapter
        val adapter =
            ArrayAdapter(requireActivity(), R.layout.snap_mealtype_spinner, items)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerMealType.adapter = adapter

        if (!mealType.equals("")){
            val defaultIndex = items.indexOf(formatMealTypeName(mealType))
            if (defaultIndex != -1) {
                spinnerMealType.setSelection(defaultIndex)
                selectedMealType = items[defaultIndex]
            }
        }else {
            val defaultMeal = getDefaultMealType()
            val defaultIndex = items.indexOf(defaultMeal)
            if (defaultIndex != -1) {
                spinnerMealType.setSelection(defaultIndex)
                selectedMealType = items[defaultIndex]
            }
        }

        // Handle item selection
        spinnerMealType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (!isSpinnerMealTypeInitialized) {
                    isSpinnerMealTypeInitialized = true
                    return
                }
                val selectedItem = parent.getItemAtPosition(position).toString()
                selectedMealType = selectedItem
                addToTheMealLayout.isEnabled = true
                addToTheMealLayout.alpha = 1.0f
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                //  selectedText.text = "No selection"
            }
        }

        if (selectedMealDate.equals("") || selectedMealDate.equals("null")){
            tvSelectedDate.text = currentDateTime.format(formatFullDate)
        }else{
            val currentDateTime = selectedMealDate
            val formatFullDate = formatDisplayDateToReadable(currentDateTime)
          //  val formatFullDate = DateTimeFormatter.ofPattern("d MMMM yyyy")
            tvSelectedDate.text = formatFullDate
        }

        view.findViewById<LinearLayoutCompat>(R.id.datePickerLayout).setOnClickListener {
            // Open Date Picker
            showDatePicker()
        }

        layoutMacroTitle.setOnClickListener {
          if (macroItemRecyclerView.isVisible){
              macroItemRecyclerView.visibility = View.GONE
              icMacroUP.setImageResource(R.drawable.ic_down)
              view.findViewById<View>(R.id.view_macro).visibility = View.GONE
          }else{
              macroItemRecyclerView.visibility = View.VISIBLE
              icMacroUP.setImageResource(R.drawable.ic_up)
              view.findViewById<View>(R.id.view_macro).visibility = View.VISIBLE
          }
        }

        layoutMicroTitle.setOnClickListener {
            if (microItemRecyclerView.isVisible){
                microItemRecyclerView.visibility = View.GONE
                microUP.setImageResource(R.drawable.ic_down)
                view.findViewById<View>(R.id.view_micro).visibility = View.GONE
            }else{
                microItemRecyclerView.visibility = View.VISIBLE
                microUP.setImageResource(R.drawable.ic_up)
                view.findViewById<View>(R.id.view_micro).visibility = View.VISIBLE
            }
        }

        macroItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        macroItemRecyclerView.adapter = macroNutrientsAdapter
        microItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        microItemRecyclerView.adapter = microNutrientsAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = YourMealLogsFragment()
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        })

        backButton.setOnClickListener {
            val fragment = YourMealLogsFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }

        if (recipeDetailsLocalListModel != null){
            for (item in recipeDetailsLocalListModel!!.data) {
                if (item.recipe.contentEquals(snapRecipeName)) {
                    setDishData(item, false)
                    if (!item.source.equals("my_recipe")){
                      setupSpinner(item.available_serving, item.selected_serving)
                    }
                    onMacroNutrientsList(item, 1.0, 1.0)
                    onMicroNutrientsList(item, 1.0, 1.0)
                    break
                }
            }
        }

        quantityEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(update: Editable?) {
                if (update!!.isNotEmpty() && update.toString() != "."){
                    if (quantityEdit.text.toString().toDouble() > 0.0){
                        val targetValue : Double = quantityEdit.text.toString().toDouble()
                        addToTheMealLayout.isEnabled = true
                        addToTheMealLayout.alpha = 1.0f
                        if (recipeDetailsLocalListModel != null){
                            for (item in recipeDetailsLocalListModel!!.data) {
                                if (item.recipe.contentEquals(snapRecipeName)) {
                                    setDishData(item, true)
                                    var mealQuantity = 0.0
                                    if (selectedDefaultValue != null && selectedDefaultValue!! > 0.0){
                                        mealQuantity = selectedDefaultValue!!
                                    }else{
                                        mealQuantity = 1.0
                                    }
                                    onMacroNutrientsList(item, mealQuantity, targetValue)
                                    onMicroNutrientsList(item, mealQuantity, targetValue)
                                    break
                                }
                            }
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        addToTheMealLayout.setOnClickListener {
            if (quantityEdit.text.toString().isNotEmpty() && quantityEdit.text.toString() != "."){
                if (quantityEdit.text.toString().toDouble() > 0.0){
                    if (recipeDetailsLocalListModel != null) {
                        if (recipeDetailsLocalListModel!!.data.size > 0) {
                            for (item in recipeDetailsLocalListModel!!.data) {
                                if (item.recipe.contentEquals(snapRecipeName)) {
                                    val foodData = item
                                    var targetValue = 0.0
                                    if (quantityEdit.text.toString().toDouble() > 0.0){
                                        targetValue = quantityEdit.text.toString().toDouble()
                                    }else{
                                        targetValue = 0.0
                                    }
                                    val index = recipeDetailsLocalListModel!!.data.indexOfFirst { it.recipe == snapRecipeName }
                                    var ingredientQuantity = 0.0
                                    if (selectedDefaultValue != null && selectedDefaultValue!! > 0.0){
                                        ingredientQuantity = selectedDefaultValue!!
                                    }else{
                                        ingredientQuantity = 1.0
                                    }
                                    val selectedServing = Serving(
                                        type = measureType,
                                        value = quantityEdit.text.toString().toDouble())
                                    val ingredientData = IngredientRecipeDetails(
                                        id = foodData.id,
                                        recipe_id = foodData.recipe_id,
                                        food_code = foodData.food_code,
                                        food_name = foodData.food_name,
                                        recipe = foodData.recipe,
                                        meal_type = foodData.meal_type,
                                        cuisine = foodData.cuisine,
                                        regional_split = foodData.regional_split,
                                        category = foodData.category,
                                        food_category = foodData.food_category,
                                        flag = foodData.flag,
                                        serving_size_for_calorific_breakdown = foodData.serving_size_for_calorific_breakdown,
                                        standard_serving_size = tvMeasure.text.toString(),
                                        calories_kcal = calculateValue(foodData.calories_kcal, ingredientQuantity, targetValue),
                                        carbs_g = calculateValue(foodData.carbs_g, ingredientQuantity, targetValue),
                                        fiber_g = calculateValue(foodData.fiber_g, ingredientQuantity, targetValue),
                                        sugars_g = calculateValue(foodData.sugars_g, ingredientQuantity, targetValue),
                                        vit_b6_mg = calculateValue(foodData.vit_b6_mg, ingredientQuantity, targetValue),
                                        vit_b12_mcg = calculateValue(foodData.vit_b12_mcg, ingredientQuantity, targetValue),
                                        protein_g = calculateValue(foodData.protein_g, ingredientQuantity, targetValue),
                                        fat_g = calculateValue(foodData.fat_g, ingredientQuantity, targetValue),
                                        vit_a_mcg = calculateValue(foodData.vit_a_mcg, ingredientQuantity, targetValue),
                                        vit_c_mg = calculateValue(foodData.vit_c_mg, ingredientQuantity, targetValue),
                                        vit_d_mcg = calculateValue(foodData.vit_d_mcg, ingredientQuantity, targetValue),
                                        vit_e_mg = calculateValue(foodData.vit_e_mg, ingredientQuantity, targetValue),
                                        folate_b9_mcg = calculateValue(foodData.folate_b9_mcg, ingredientQuantity, targetValue),
                                        vit_k_mcg = calculateValue(foodData.vit_k_mcg, ingredientQuantity, targetValue),
                                        thiamin_b1_mg = calculateValue(foodData.thiamin_b1_mg, ingredientQuantity, targetValue),
                                        riboflavin_b2_mg = calculateValue(foodData.riboflavin_b2_mg, ingredientQuantity, targetValue),
                                        niacin_b3_mg = calculateValue(foodData.niacin_b3_mg, ingredientQuantity, targetValue),
                                        iron_mg = calculateValue(foodData.iron_mg, ingredientQuantity, targetValue),
                                        calcium_mg = calculateValue(foodData.calcium_mg, ingredientQuantity, targetValue),
                                        magnesium_mg = calculateValue(foodData.magnesium_mg, ingredientQuantity, targetValue),
                                        zinc_mg = calculateValue(foodData.zinc_mg, ingredientQuantity, targetValue),
                                        potassium_mg = calculateValue(foodData.potassium_mg, ingredientQuantity, targetValue),
                                        sodium_mg = calculateValue(foodData.sodium_mg, ingredientQuantity, targetValue),
                                        phosphorus_mg = calculateValue(foodData.phosphorus_mg, ingredientQuantity, targetValue),
                                        omega3_g = calculateValue(foodData.omega3_g, ingredientQuantity, targetValue),
                                        ingredients = foodData.ingredients,
                                        preparation_notes = foodData.preparation_notes,
                                        active_cooking_time_min = foodData.active_cooking_time_min,
                                        allergy_groups_restricted_from_consuming = foodData.allergy_groups_restricted_from_consuming,
                                        tags = foodData.tags,
                                        typical_1person_serving = foodData.typical_1person_serving,
                                        household_measure_1_serving = foodData.household_measure_1_serving,
                                        photo_url = foodData.photo_url,
                                        selected_serving = selectedServing,
                                        default_serving = foodData.default_serving,
                                        available_serving = foodData.available_serving,
                                        source = foodData.source,
                                        quantity =  quantityEdit.text.toString().toDouble(),
                                        servings = foodData.servings
                                    )
                                    if (index != -1) {
                                        dishLists[index] = ingredientData
                                    }
                                    dishLists.get(index)
                                    recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
                                    updateLogDish(mealId, dishLists)
                                    break
                                }
                            }
                        }
                    }
                }else{
                    Toast.makeText(activity, "Please input quantity greater than 0", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(activity, "Please input quantity greater than 0", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val date = "$dayOfMonth ${getMonthName(month + 1)} $year"
                tvSelectedDate.text = date
                addToTheMealLayout.isEnabled = true
                addToTheMealLayout.alpha = 1.0f
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        val today = System.currentTimeMillis()
        // 🚫 Disable past dates
        //datePicker.datePicker.minDate = today
        // 🚫 Disable future dates
        datePicker.datePicker.maxDate = today
        datePicker.show()
    }

    fun formatDisplayDateToReadable(date: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
        val localDate = LocalDate.parse(date, inputFormatter)
        return localDate.format(outputFormatter)
    }

    fun formatApiDateToReadable(date: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val localDate = LocalDate.parse(date, inputFormatter)
        return localDate.format(outputFormatter)
    }

    fun getDefaultMealType(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentMinutes = hour * 60 + minute
        return when {
            currentMinutes in (5 * 60)..(9 * 60 + 59) -> "Breakfast"
            currentMinutes in (10 * 60)..(11 * 60 + 59) -> "Morning Snack"
            currentMinutes in (12 * 60)..(14 * 60 + 59) -> "Lunch"
            currentMinutes in (15 * 60)..(18 * 60 + 59) -> "Evening Snacks"
            else -> "Dinner" // 7:00 PM – 4:59 AM
        }
    }

    private fun formatMealTypeName(meal: String): String {
        return when (meal.lowercase()) {
            "breakfast" -> "Breakfast"
            "morning_snack" -> "Morning Snack"
            "lunch" -> "Lunch"
            "evening_snack" -> "Evening Snacks"
            "dinner" -> "Dinner"
            else -> meal.replaceFirstChar { it.uppercase() } // fallback
        }
    }

    private fun calculateValue(givenValue: Double?, defaultQuantity: Double, targetQuantity: Double): Double {
        return if (givenValue != null && defaultQuantity > 0.0) {
            (givenValue / defaultQuantity) * targetQuantity
        } else {
            0.0
        }
    }

    private fun Double?.scaledIfPositive(scale: Double): Double {
        return if (this != null && this > 0.0) this * scale else 0.0
    }

    private fun setDishData(snapRecipeData: IngredientRecipeDetails, isEdit: Boolean) {
        addToTheMealTV.text = "Update Dish"
        logMealTitle.text = "Dish"
        val capitalized = snapRecipeData.recipe.toString().replaceFirstChar { it.uppercase() }
        tvMealName.text = capitalized
        if (snapRecipeData.source.equals("my_recipe")){
            tvMeasure.visibility = View.VISIBLE
            spinner.visibility = View.GONE
            ivMealDropdown.visibility = View.GONE
            if (snapRecipeData.selected_serving != null){
                tvMeasure.text = snapRecipeData.selected_serving.type?: "serving"
            }else{
                tvMeasure.text = "serving"
            }
        }else {
            tvMeasure.visibility = View.GONE
            spinner.visibility = View.VISIBLE
            ivMealDropdown.visibility = View.VISIBLE
        }
        if (!isEdit){
            if (snapRecipeData.source.equals("my_recipe")){
                if (snapRecipeData.selected_serving?.value != null ){
                    quantityEdit.setText(snapRecipeData.selected_serving.value.toString())
                }
            }
        }
        var imageUrl : String? = ""
        imageUrl = if (snapRecipeData.photo_url.contains("drive.google.com")) {
            getDriveImageUrl(snapRecipeData.photo_url)
        }else{
            snapRecipeData.photo_url
        }
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_view_meal_place)
            .error(R.drawable.ic_view_meal_place)
            .into(imgFood)
    }

    private fun setupSpinner(servingsList: List<Serving>, default: Serving?) {
        val adapter =
            ArrayAdapter(requireActivity(), R.layout.snap_mealtype_spinner,  servingsList.map {it.type })
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
        // Store default serving
        defaultServing = default
        // Pre-select default serving in spinner
        val defaultIndex = servingsList.indexOfFirst {
            it.type == default?.type
        }
        val safeIndex = if (defaultIndex >= 0) defaultIndex else 0
        spinner.setSelection(safeIndex)
        val defaultSelectedServing = servingsList[safeIndex]
        measureType = defaultSelectedServing.type.toString()
        selectedDefaultValue = default?.value ?: defaultSelectedServing.value
        quantityEdit.setText(selectedDefaultValue.toString())
        // Listener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true
                    return
                }
                val selectedServing = servingsList[position]
                userSelectedServing = selectedServing  // ✅ track user choice
                val newQuantity = selectedServing.value.toString()
                if (quantityEdit.text.toString() != newQuantity) {
                    measureType = selectedServing.type.toString()
                    selectedDefaultValue = selectedServing.value
                    quantityEdit.setText(newQuantity)
                    addToTheMealLayout.isEnabled = true
                    addToTheMealLayout.alpha = 1.0f
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun onMacroNutrientsList(mealDetails: IngredientRecipeDetails, defaultValue: Double, targetValue: Double) {

        val calories = calculateValue(mealDetails.calories_kcal, defaultValue, targetValue)
        val protein = calculateValue(mealDetails.protein_g, defaultValue, targetValue)
        val carbs = calculateValue(mealDetails.carbs_g, defaultValue, targetValue)
        val fats = calculateValue(mealDetails.fat_g, defaultValue, targetValue)
        val calories_kcal : String = calories.toInt().toString()?: "NA"
        val protein_g : String = protein.toInt().toString()?: "NA"
        val carb_g : String = carbs.toInt().toString()?: "NA"
        val fat_g : String = fats.toInt().toString()?: "NA"

        val mealLogs = listOf(
            MacroNutrientsModel(calories_kcal, "kcal", "Calorie", R.drawable.ic_cal),
            MacroNutrientsModel(protein_g, "g", "Protein", R.drawable.ic_protein),
            MacroNutrientsModel(carb_g, "g", "Carbs", R.drawable.ic_cabs),
            MacroNutrientsModel(fat_g, "g", "Fats", R.drawable.ic_fats),
        )

        val valueLists : ArrayList<MacroNutrientsModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MacroNutrientsModel>)
        val mealLogDateData: MacroNutrientsModel? = null
        macroNutrientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMicroNutrientsList(mealDetails: IngredientRecipeDetails, defaultValue: Double, targetValue: Double) {

//        val cholesterol = if (mealDetails.cholesterol != null){
//            calculateValue( mealDetails.cholesterol, defaultValue, targetValue).toString()
//        }else{
//            "0.0"
//        }

        val vitamin_A = if (mealDetails.vit_a_mcg != null){
            calculateValue( mealDetails.vit_a_mcg, defaultValue, targetValue).toString()
        }else{
            "0.0"
        }

        val vitamin_C = if (mealDetails.vit_c_mg != null){
            calculateValue( mealDetails.vit_c_mg, defaultValue, targetValue).toString()
        }else{
            "0.0"
        }

        val vitamin_k = if (mealDetails.vit_k_mcg != null){
            calculateValue( mealDetails.vit_k_mcg, defaultValue, targetValue).toString()
        }else{
            "0.0"
        }

        val vitaminD = if (mealDetails.vit_d_mcg != null){
            calculateValue( mealDetails.vit_d_mcg, defaultValue, targetValue).toString()
        }else{
            "0.0"
        }

        val folate = if (mealDetails.folate_b9_mcg != null){
            calculateValue( mealDetails.folate_b9_mcg, defaultValue, targetValue).toString()
        }else{
            "0.0"
        }

        val iron_mg = if (mealDetails.iron_mg != null){
            calculateValue( mealDetails.iron_mg, defaultValue, targetValue).toString()
        }else{
            "0.0"
        }

        val calcium = if (mealDetails.calcium_mg != null){
            calculateValue( mealDetails.calcium_mg, defaultValue, targetValue).toString()
        }else{
            "0.0"
        }

        val magnesium = if (mealDetails.magnesium_mg != null){
            calculateValue( mealDetails.magnesium_mg, defaultValue, targetValue).toString()
        }else{
            "0.0"
        }

        val potassium_mg = if (mealDetails.potassium_mg != null){
            calculateValue( mealDetails.potassium_mg, defaultValue, targetValue).toString()
        }else{
            "0.0"
        }

//        val fiber_mg = if (mealDetails.fiber != null){
//            calculateValue( mealDetails.fiber, defaultValue, targetValue).toString()
//        }else{
//            "0"
//        }

        val zinc = if (mealDetails.zinc_mg != null){
            calculateValue( mealDetails.zinc_mg, defaultValue, targetValue).toString()
        }else{
            "0.0"
        }

        val sodium = if (mealDetails.sodium_mg != null){
            calculateValue( mealDetails.sodium_mg, defaultValue, targetValue).toString()
        }else{
            "0.0"
        }

//        val sugar_mg = if (mealDetails.sugar != null){
//            calculateValue( mealDetails.sugar, defaultValue, targetValue).toString()
//        }else{
//            "0.0"
//        }

        val vitB6 = if (mealDetails.vit_b6_mg != null){
            calculateValue( mealDetails.vit_b6_mg, defaultValue, targetValue).toString()
        }else{
            "0.0"
        }

        val vitB12 = if (mealDetails.vit_b12_mcg != null){
            calculateValue( mealDetails.vit_b12_mcg, defaultValue, targetValue).toString()
        }else{
            "0.0"
        }

        val phosphorus = if (mealDetails.phosphorus_mg != null){
            calculateValue( mealDetails.phosphorus_mg, defaultValue, targetValue).toString()
        }else{
            "0.0"
        }

        val mealLogs = listOf(
            //   MicroNutrientsModel(cholesterol, "mg", "Cholesterol", R.drawable.ic_fats),
            MicroNutrientsModel(vitamin_A, "mcg", "Vitamin A", R.drawable.ic_fats),
            MicroNutrientsModel(vitamin_C, "mg", "Vitamin C", R.drawable.ic_fats),
            MicroNutrientsModel(vitamin_k, "mcg", "Vitamin K", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminD, "mcg", "Vitamin D", R.drawable.ic_fats),
            MicroNutrientsModel(vitB6, "mg", "Vitamin B6", R.drawable.ic_fats),
            MicroNutrientsModel(vitB12, "mcg", "Vitamin B12", R.drawable.ic_fats),
            MicroNutrientsModel(folate, "mcg", "Folate", R.drawable.ic_fats),
            MicroNutrientsModel(iron_mg, "mg", "Iron", R.drawable.ic_fats),
            MicroNutrientsModel(calcium, "mg", "Calcium", R.drawable.ic_fats),
            MicroNutrientsModel(magnesium, "mg", "Magnesium", R.drawable.ic_fats),
            MicroNutrientsModel(potassium_mg, "mg", "Potassium", R.drawable.ic_fats),
            //       MicroNutrientsModel(fiber_mg, "mg", "Fiber", R.drawable.ic_fats),
            MicroNutrientsModel(zinc, "mg", "Zinc", R.drawable.ic_fats),
            MicroNutrientsModel(sodium, "mg", "Sodium", R.drawable.ic_fats),
            //        MicroNutrientsModel(sugar_mg, "g", "Sugar", R.drawable.ic_fats)
            MicroNutrientsModel(phosphorus, "mg", "Phosphorus", R.drawable.ic_fats)
        )
        val valueLists : ArrayList<MicroNutrientsModel> = ArrayList()
        for (item in mealLogs){
            if (item.nutrientsValue != "0.0"){
                valueLists.add(item)
            }
        }
        val mealLogDateData: MicroNutrientsModel? = null
        microNutrientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMacroNutrientsItemClick(macroNutrientsModel: MacroNutrientsModel, position: Int, isRefresh: Boolean) {

    }

    private fun onMicroNutrientsItemClick(microNutrientsModel: MicroNutrientsModel, position: Int, isRefresh: Boolean) {

    }

    private fun getMonthName(month: Int): String {
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(Date(0, month, 0))
    }

    fun getDriveImageUrl(originalUrl: String): String? {
        val regex = Regex("(?<=/d/)(.*?)(?=/|$)")
        val matchResult = regex.find(originalUrl)
        val fileId = matchResult?.value
        return if (!fileId.isNullOrEmpty()) {
            "https://drive.google.com/uc?export=view&id=$fileId"
        } else {
            null
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

    private fun updateLogDish(mealId: String, snapRecipeList : ArrayList<IngredientRecipeDetails>) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val recipes: ArrayList<RecipeLogRequest> = ArrayList()
        val ingredients: ArrayList<IngredientLogRequest> = ArrayList()
        val mealLogList : ArrayList<DishLogItem> = ArrayList()
        snapRecipeList?.forEach { selectedDish ->
//            val mealLogData = DishLogItem(
//                receipe_id = snapRecipe.id,
//                meal_quantity = snapRecipe.quantity,
//                 unit = snapRecipe.standard_serving_size,
//                measure = "Bowl"
//            )
//            mealLogList.add(mealLogData)
            if (selectedDish.source.equals("recipe")){
                val mealRecipeData = RecipeLogRequest(
                    recipe_id = selectedDish.id,
                    selected_serving_type = selectedDish.selected_serving?.type,
                    selected_serving_value = selectedDish.selected_serving?.value
                )
                recipes.add(mealRecipeData)
            }else if (selectedDish.source.equals("my_recipe")){
                val mealRecipeData = RecipeLogRequest(
                    recipe_id = selectedDish.id,
                    selected_serving_type = tvMeasure.text.toString(),
                    selected_serving_value = quantityEdit.text.toString().toDouble()
                )
                recipes.add(mealRecipeData)
            }
            if (selectedDish.source.equals("ingredient")){
                val mealIngredientData = IngredientLogRequest(
                    ingredient_id = selectedDish.id,
                    meal_quantity = selectedDish.quantity,
                    standard_serving_size = selectedDish.selected_serving?.type
                )
                ingredients.add(mealIngredientData)
            }
        }
        val apiDate = tvSelectedDate.text.toString()
        val formatApiDate = formatApiDateToReadable(apiDate)
        val updateMealRequest = SaveDishLogRequest(
            date = formatApiDate,
            meal_type = formatMealType(selectedMealType),
            recipes = recipes,
            ingredients = ingredients
        )
        val call = ApiClient.apiServiceFastApiV2.updateLogDish(userId, mealId, updateMealRequest)
        call.enqueue(object : Callback<MealUpdateResponse> {
            override fun onResponse(call: Call<MealUpdateResponse>, response: Response<MealUpdateResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealData = response.body()?.message
                    Toast.makeText(context, mealData, Toast.LENGTH_SHORT).show()
  //                  Toast.makeText(activity, "Changes Save", Toast.LENGTH_SHORT).show()
                    val fragment = YourMealLogsFragment()
                    val args = Bundle()
                    args.putString("ModuleName", moduleName)
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }
            override fun onFailure(call: Call<MealUpdateResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    private fun formatMealType(input: String): String {
        return when (input.lowercase()) {
            "breakfast" -> "breakfast"
            "morning snack" -> "morning_snack"
            "lunch" -> "lunch"
            "evening snacks" -> "evening_snack"
            "dinner" -> "dinner"
            else -> input.lowercase().replace(" ", "_")
        }
    }
}