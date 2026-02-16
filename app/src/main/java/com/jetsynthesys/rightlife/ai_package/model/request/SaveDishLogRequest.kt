package com.jetsynthesys.rightlife.ai_package.model.request

data class SaveDishLogRequest(
    val date: String,
    val meal_type: String?,
    val recipes: List<RecipeLogRequest>,
    val ingredients: List<IngredientLogRequest>
)

data class RecipeLogRequest(
    val recipe_id: String?,
    val selected_serving_type: String?,
    val selected_serving_value: Double?
)

data class IngredientLogRequest(
    val ingredient_id: String?,
    val meal_quantity: Double?,
    val standard_serving_size: String?
)

data class DishLog(
    val receipe_id: String?,
    val meal_quantity: Double?,
    val unit: String?,
    val measure: String?
)
