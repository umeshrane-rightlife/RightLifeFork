package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.frequentlylogged

import android.R.color.transparent
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.jetsynthesys.rightlife.R
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.request.DishLog
import com.jetsynthesys.rightlife.ai_package.model.request.IngredientLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.RecipeLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.SaveDishLogRequest
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails
import com.jetsynthesys.rightlife.ai_package.model.response.MealUpdateResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeDetailsLocalListModel
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FrequentlyAddDishBottomSheet : BottomSheetDialogFragment() {

    private lateinit var flexboxLayout: FlexboxLayout
    private val ingredientsList = ArrayList<String>()
    private  var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
    private var dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
    private lateinit var mealType : String
    private lateinit var layoutTitle : LinearLayout
    private lateinit var btnLogMeal: LinearLayoutCompat
    private lateinit var checkCircle : ImageView
    private lateinit var loggedSuccess : TextView
    private var loadingOverlay : FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_frequently_add_meal_bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialog = BottomSheetDialog(requireContext(), R.style.LoggedBottomSheetDialogTheme)
        dialog.setContentView(R.layout.fragment_frequently_logged)
        dialog.window?.setBackgroundDrawableResource(transparent)
        flexboxLayout = view.findViewById(R.id.flexboxLayout)
        val btnAdd: LinearLayoutCompat = view.findViewById(R.id.layout_btnAdd)
         btnLogMeal = view.findViewById(R.id.layout_btnLogMeal)
         layoutTitle = view.findViewById(R.id.layout_title)
         checkCircle = view.findViewById<ImageView>(R.id.check_circle_icon)
         loggedSuccess = view.findViewById<TextView>(R.id.tv_logged_success)

        mealType = arguments?.getString("mealType").toString()
        val dishLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapDishLocalListModel", RecipeDetailsLocalListModel::class.java)
        } else {
            arguments?.getParcelable("snapDishLocalListModel")
        }
        if (dishLocalListModels != null){
            recipeDetailsLocalListModel = dishLocalListModels
            dishLists.addAll(recipeDetailsLocalListModel!!.data)
        }
        flexboxLayout.visibility = View.VISIBLE
        layoutTitle.visibility = View.VISIBLE
        btnLogMeal.visibility = View.VISIBLE
        // Display default ingredients

        if (dishLists.size > 0){
            for (dishItem in dishLists) {
                ingredientsList.add(dishItem.recipe)
            }
            if (ingredientsList.size > 0){
                updateIngredientChips()
            }
        }

        // Add button clicked (For demonstration, adding a dummy ingredient)
//        btnAdd.setOnClickListener {
//            val newIngredient = "New Item ${ingredientsList.size + 1}"
//            ingredientsList.add(newIngredient)
//            updateIngredientChips()
//        }

        // Log Meal button click
        btnLogMeal.setOnClickListener {
            if (mealType.isNotEmpty()){
                if (dishLists.size > 0){
                    createDishLog(dishLists)
                }
            }
        }
    }

    // Function to update Flexbox with chips
    private fun updateIngredientChips() {
        flexboxLayout.removeAllViews() // Clear existing chips
        for (ingredient in ingredientsList) {
            val chipView = LayoutInflater.from(context).inflate(R.layout.chip_ingredient, flexboxLayout, false)
            val tvIngredient: TextView = chipView.findViewById(R.id.tvIngredient)
            val btnRemove: ImageView = chipView.findViewById(R.id.btnRemove)
            btnRemove.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white), PorterDuff.Mode.SRC_IN)
            tvIngredient.text = ingredient
            btnRemove.setOnClickListener {
                ingredientsList.remove(ingredient)
                updateIngredientChips()
            }
            flexboxLayout.addView(chipView)
        }
    }

    private fun createDishLog(snapRecipeList : ArrayList<IngredientRecipeDetails>) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)

        val mealLogList : ArrayList<DishLog> = ArrayList()
        val recipes: ArrayList<RecipeLogRequest> = ArrayList()
        val ingredients: ArrayList<IngredientLogRequest> = ArrayList()
        val mealNamesString = snapRecipeList.map { it.recipe ?: "" }.joinToString(", ")

        snapRecipeList?.forEach { snapRecipe ->
            if (snapRecipe.source.equals("recipe")){
                val mealRecipeData = RecipeLogRequest(
                    recipe_id = snapRecipe.id,
                    selected_serving_type = snapRecipe.selected_serving?.type,
                    selected_serving_value = snapRecipe.selected_serving?.value
                )
                recipes.add(mealRecipeData)
            }
            if (snapRecipe.source.equals("ingredient")){
                val mealIngredientData = IngredientLogRequest(
                    ingredient_id = snapRecipe.id,
                    meal_quantity = snapRecipe.quantity,
                    standard_serving_size = snapRecipe.selected_serving?.type
                )
                ingredients.add(mealIngredientData)
            }
//            val mealLogData = DishLog(
//                receipe_id = snapRecipe.id,
//                meal_quantity = 1.0,
//                unit = "g",
//                measure = "Bowl"
//            )
//            mealLogList.add(mealLogData)
        }
        val mealLogRequest = SaveDishLogRequest(
            date = formattedDate,
            meal_type = mealType ?: "dd",
            recipes = recipes,
            ingredients = ingredients
        )
        val call = ApiClient.apiServiceFastApiV2.createSaveMealsToLog(userId, formattedDate, mealLogRequest)
        call.enqueue(object : Callback<MealUpdateResponse> {
            override fun onResponse(call: Call<MealUpdateResponse>, response: Response<MealUpdateResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealData = response.body()?.message
                //    Toast.makeText(activity, mealData, Toast.LENGTH_SHORT).show()
                    flexboxLayout.visibility = View.GONE
                    layoutTitle.visibility = View.GONE
                    btnLogMeal.visibility = View.GONE
                    checkCircle.visibility = View.VISIBLE
                    loggedSuccess.visibility = View.VISIBLE
                    loggedSuccess.text = mealData
//                    val fragment = HomeTabMealFragment()
//                    val args = Bundle()
//                    fragment.arguments = args
//                    requireActivity().supportFragmentManager.beginTransaction().apply {
//                        replace(R.id.flFragment, fragment, "landing")
//                        addToBackStack("landing")
//                        commit()
//                    }
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
            override fun onFailure(call: Call<MealUpdateResponse>, t: Throwable) {
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

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    companion object {
        const val TAG = "LoggedBottomSheet"
        @JvmStatic
        fun newInstance() = FrequentlyAddDishBottomSheet().apply {
            arguments = Bundle().apply {
            }
        }
    }
}

