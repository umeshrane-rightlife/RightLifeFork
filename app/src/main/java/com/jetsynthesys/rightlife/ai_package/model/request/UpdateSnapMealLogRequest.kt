package com.jetsynthesys.rightlife.ai_package.model.request

import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails

data class UpdateSnapMealLogRequest(
    val meal_name: String,
    val date: String,
    val meal_type: String?,
    val meal_log: List<IngredientRecipeDetails>
)

data class SnapMealLogItems(
    val _id: String,
    val name: String,
    val servings: Int,
    val b12_mcg: Double?,
    val b1_mg: Double?,
    val b2_mg: Double?,
    val b3_mg: Double?,
    val b6_mg: Double?,
    val calcium_mg: Double?,
    val calories_kcal: Double?,
    val carb_g: Double?,
    val cholesterol_mg: Double?,
    val copper_mg: Double?,
    val fat_g: Double?,
    val folate_mcg: Double?,
    val fiber_g: Double?,
    val iron_mg: Double?,
    val is_beverage: Double?,
    val magnesium_mg: Double?,
    val mass_g: Double?,
    val monounsaturated_g: Double?,
    val omega_3_fatty_acids_g: Double?,
    val omega_6_fatty_acids_g: Double?,
    val percent_fruit: Double?,
    val percent_legume_or_nuts: Double?,
    val percent_vegetable: Double?,
    val phosphorus_mg: Double?,
    val polyunsaturated_g: Double?,
    val potassium_mg: Double?,
    val protein_g: Double?,
    val saturated_fats_g: Double?,
    val selenium_mcg: Double?,
    val sodium_mg: Double?,
    val source_urls: List<String>?,
    val sugar_g: Double?,
    val vitamin_a_mcg: Double?,
    val vitamin_c_mg: Double?,
    val vitamin_d_iu: Double?,
    val vitamin_e_mg: Double?,
    val vitamin_k_mcg: Double?,
    val zinc_mg: Double?,
    val mealQuantity: Double?
)
