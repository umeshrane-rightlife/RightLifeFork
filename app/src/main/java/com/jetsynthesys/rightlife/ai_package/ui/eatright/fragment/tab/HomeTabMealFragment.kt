package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import java.time.LocalDate
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.flexbox.FlexboxLayout
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.jetsynthesys.rightlife.databinding.FragmentHomeTabMealBinding
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.jetsynthesys.rightlife.ai_package.PermissionManager
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.request.DishLog
import com.jetsynthesys.rightlife.ai_package.model.request.IngredientLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.MealLogItem
import com.jetsynthesys.rightlife.ai_package.model.request.RecipeLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.SaveDishLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.SaveSnapMealLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.SnapMealLogRequest
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails
import com.jetsynthesys.rightlife.ai_package.model.response.MealUpdateResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealDetailsResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealLogResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.MealSaveQuitBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.OnImageSelectedListener
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.SearchDishToLogFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.SnapMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.frequentlylogged.FrequentlyAddDishBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MealLogItems
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SelectedMealLogList
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeDetailsLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SnapMealRequestLocalListModel
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class HomeTabMealFragment : BaseFragment<FragmentHomeTabMealBinding>(), MealSaveQuitBottomSheet.OnMealSaveQuitListener {

    private val sharedViewModel: SharedMealViewModel by activityViewModels()

    private lateinit var tabLayout : TabLayout
    private lateinit var backIc : ImageView
    private lateinit var searchLayout : LinearLayoutCompat
    private lateinit var searchType : String
    private lateinit var mealType : String
    private lateinit var frequentlyAddDishBottomSheetLayout : ConstraintLayout
    private lateinit var flexboxLayout: FlexboxLayout
    private val ingredientsList = ArrayList<String>()
    private lateinit var layoutTitle : LinearLayout
    private lateinit var btnLogMeal: LinearLayoutCompat
    private lateinit var checkCircle : ImageView
    private lateinit var imageScan : ImageView
    private lateinit var imageGallery : ImageView
    private lateinit var loggedSuccess : TextView
    private var dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
    private  var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
    private var mealLogRequests : SelectedMealLogList? = null
    private var mealLogRequestsList : ArrayList<SelectedMealLogList> = ArrayList()
    private var selectedMealLogList : ArrayList<MealLogItems> = ArrayList()
    private var snapMealLogRequests : SelectedMealLogList? = null
    private var selectedSnapMealLogList : ArrayList<MealLogItems> = ArrayList()
    private var isSnaps : Boolean = false
    private var snapMealRequestLocalListModel : SnapMealRequestLocalListModel? = null
    private var snapMealLogRequestList : ArrayList<SnapMealLogRequest> = ArrayList()
    private var snapMealRequestCount : Int = 0
    private var loadingOverlay : FrameLayout? = null
    private lateinit var tvIngredientsCount : TextView
    private var tabType : String = ""
    private var moduleName : String = ""
    private var selectedMealDate : String = ""
    var imageSelectedListener: OnImageSelectedListener? = null
    private lateinit var imagePathsecond : Uri
    private var mealQuantity : String = ""
    private var currentToast: Toast? = null
    private lateinit var permissionManager: PermissionManager
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            permissionManager.handlePermissionResult(result)
        }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeTabMealBinding
        get() = FragmentHomeTabMealBinding::inflate

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        imageScan = view.findViewById(R.id.image_calender)
        imageGallery = view.findViewById(R.id.image)
        tabLayout = view.findViewById(R.id.tabLayout)
        backIc = view.findViewById(R.id.backIc)
        searchLayout = view.findViewById(R.id.searchLayout)
        frequentlyAddDishBottomSheetLayout = view.findViewById(R.id.frequentlyAddDishBottomSheetLayout)
        flexboxLayout = view.findViewById(R.id.flexboxLayout)
        val btnAdd: LinearLayoutCompat = view.findViewById(R.id.layout_btnAdd)
        btnLogMeal = view.findViewById(R.id.layout_btnLogMeal)
        layoutTitle = view.findViewById(R.id.layout_title)
        checkCircle = view.findViewById(R.id.check_circle_icon)
        loggedSuccess = view.findViewById(R.id.tv_logged_success)
        tvIngredientsCount = view.findViewById(R.id.tvIngredientsCount)

        moduleName = arguments?.getString("ModuleName").toString()
        searchType = arguments?.getString("searchType").toString()
        mealType = arguments?.getString("mealType").toString()
        tabType = arguments?.getString("tabType").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()
        mealQuantity = arguments?.getString("mealQuantity").toString()
        val dishLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapDishLocalListModel", RecipeDetailsLocalListModel::class.java)
        } else {
            arguments?.getParcelable("snapDishLocalListModel")
        }

        val selectedMealLogListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("selectedMealLogList", SelectedMealLogList::class.java)
        } else {
            arguments?.getParcelable("selectedMealLogList")
        }

        val selectedSnapMealLogListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("selectedSnapMealLogList", SelectedMealLogList::class.java)
        } else {
            arguments?.getParcelable("selectedSnapMealLogList")
        }

        val snapMealRequestLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapMealRequestLocalListModel", SnapMealRequestLocalListModel::class.java)
        } else {
            arguments?.getParcelable("snapMealRequestLocalListModel")
        }

        if (snapMealRequestLocalListModels != null){
            snapMealRequestLocalListModel = snapMealRequestLocalListModels
            snapMealLogRequestList.addAll(snapMealRequestLocalListModel!!.data)
        }

        if (selectedMealLogListModels != null){
            mealLogRequests = selectedMealLogListModels
            selectedMealLogList.addAll(mealLogRequests!!.meal_log)
        }

        if (selectedSnapMealLogListModels != null){
            isSnaps = true
            snapMealLogRequests = selectedSnapMealLogListModels
            selectedSnapMealLogList.addAll(snapMealLogRequests!!.meal_log)
        }

        if (dishLocalListModels != null){
            recipeDetailsLocalListModel = dishLocalListModels
            dishLists.addAll(recipeDetailsLocalListModel!!.data)
        }

        if (mealQuantity != "null" && !mealQuantity.equals("")){
            mealQuantity = mealQuantity
        }else{
            mealQuantity = "1.0"
        }

        val tabTitles = arrayOf("Frequently Logged", "My Meal", "My Recipe")

        for (title in tabTitles) {
            val tab = tabLayout.newTab()
            val customView =
                LayoutInflater.from(context).inflate(R.layout.custom_tab, null) as TextView
            customView.text = title
            tab.customView = customView
            tabLayout.addTab(tab)
        }

        val deleteType = arguments?.getString("deleteType").toString()?: ""

        if (deleteType.contentEquals("MyMeal")){
            // Set default fragment
            //if (savedInstanceState == null) {
                replaceFragment(MyMealFragment())
                updateTabColors()
          //  }
        }else{
            // Set default fragment
            if (tabType != null && tabType != "null"){
                if (tabType.contentEquals("MyMeal")){
                    replaceFragment(MyMealFragment())
                    tabLayout.getTabAt(1)?.select()
                    updateTabColors()
                }else if (tabType.contentEquals("MyRecipe")){
                    replaceFragment(MyRecipeFragment())
                    tabLayout.getTabAt(2)?.select()
                    updateTabColors()
                }
            }else{
                if (savedInstanceState == null) {
                    replaceFragment(FrequentlyLoggedFragment())
                    updateTabColors()
                }
            }
        }

        imageScan.setOnClickListener {
//            permissionManager = PermissionManager(
//                activity = requireActivity(), // or just `this` in Activity
//                launcher = permissionLauncher,
//                onPermissionGranted = {
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        val mealSearchFragment = SnapMealFragment()
                        val args = Bundle()
                        args.putString("selectedMealDate", selectedMealDate)
                        args.putString("homeTab", "homeTab")
                        args.putString("ModuleName", moduleName)
                        args.putString("mealType", mealType)
                        mealSearchFragment.arguments = args
                        replace(R.id.flFragment, mealSearchFragment, "SnapMealFragmentTag")
                        addToBackStack(null)
                        commit()
                    }
//                },
//                onPermissionDenied = {
//                    // ❌ Show user-facing message or disable features
//                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
//                }
//            )
//            permissionManager.checkAndRequestPermissions()
        }
        imageGallery.setOnClickListener {
            context?.let { it1 ->
                AnalyticsLogger.logEvent(
                    it1, AnalyticsEvent.ER_MealLog_UploadImage
                )
            }
            openGallery()
        }
        // Handle tab clicks manually
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> replaceFrequentlyFragment(FrequentlyLoggedFragment())
                    1 -> replaceFragment(MyMealFragment())
                  //  2 -> replaceFragment(MealPlanFragment())
                    2 -> replaceFragment(MyRecipeFragment())
                }
                updateTabColors()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (ingredientsList.size > 0){
                        mealSaveQuitDialog()
                    }else{
                        val fragment = YourMealLogsFragment()
                        val args = Bundle()
                       // args.putString("ModuleName", moduleName)
                        args.putString("ModuleName", "EatRightLandingWithoutPopup")
                        args.putString("selectedMealDate", selectedMealDate)
                        fragment.arguments = args
                        requireActivity().supportFragmentManager.beginTransaction().apply {
                            replace(R.id.flFragment, fragment, "landing")
                            addToBackStack("landing")
                            commit()
                        }
                    }
                }
            })

        backIc.setOnClickListener {
            if (ingredientsList.size > 0){
                mealSaveQuitDialog()
            }else{
                val fragment = YourMealLogsFragment()
                val args = Bundle()
              //  args.putString("ModuleName", moduleName)
                args.putString("ModuleName", "EatRightLandingWithoutPopup")
                args.putString("selectedMealDate", selectedMealDate)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        }

        searchLayout.setOnClickListener {
            if (snapMealLogRequestList.size > 0){
                snapMealRequestLocalListModel = SnapMealRequestLocalListModel(snapMealLogRequestList)
            }
            val fragment = SearchDishToLogFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("searchType", "HomeTabMeal")
            args.putString("mealType", mealType)
            args.putString("selectedMealDate", selectedMealDate)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            args.putParcelable("selectedMealLogList", mealLogRequests)
            args.putParcelable("selectedSnapMealLogList", snapMealLogRequests)
            args.putParcelable("snapMealRequestLocalListModel", snapMealRequestLocalListModel)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }

        if (searchType.contentEquals("DishToLog")){
            if (recipeDetailsLocalListModel != null){
                //loggedAddDish(snapDishLocalListModel)
                frequentlyAddDishBottomSheetLayout.visibility = View.VISIBLE
                flexboxLayout.visibility = View.VISIBLE
                layoutTitle.visibility = View.VISIBLE
                btnLogMeal.visibility = View.VISIBLE
                // Display default ingredients
                if (dishLists.size > 0){
                    for (dishItem in dishLists) {
                        ingredientsList.add(dishItem.recipe)
                    }
                }
                if (mealLogRequests != null) {
                    if (selectedMealLogList.size > 0) {
                        for (dishItem in selectedMealLogList) {
                            ingredientsList.add(dishItem.recipe_name!!)
                        }
                    }
                }

                if (snapMealLogRequests != null){
                    if (selectedSnapMealLogList.size > 0){
                        for (mealItem in selectedSnapMealLogList){
                            ingredientsList.add(mealItem.recipe_name!!)
                        }
                    }
                }

                if (ingredientsList.size > 0){
                    updateIngredientChips()
                }
            }
        }

        btnLogMeal.setOnClickListener {
            context?.let { it1 ->
                AnalyticsLogger.logEvent(
                    it1, AnalyticsEvent.ER_MEALLOG_LOGYOURMEAL,
                    mapOf(
                        AnalyticsParam.LIST_OF_DISHES to ingredientsList,
                        AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                    )
                )
            }
            if (recipeDetailsLocalListModel != null){
                if (mealType.isNotEmpty() && !mealType.equals("null")){
                    if (dishLists.size > 0){
                        createDishLog()
                    }
                }
            }else{
                if (mealLogRequests != null){
                    if (mealType.isNotEmpty() && !mealType.equals("null")){
                        if (selectedMealLogList.size > 0){
                            createDishLog()
                        }
                    }
                }
            }

            if (isSnaps){
                if (snapMealLogRequests != null){
                    if (mealType.isNotEmpty() && !mealType.equals("null")){
                        if (selectedSnapMealLogList.size > 0){
                           // createSnapMealLog()
                            if (snapMealLogRequestList.size > 0){
                                snapMealLogRequestList?.forEach { snapDish ->
                                    createSnapMealLog(snapDish)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

//    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        uri?.let {
//            imageSelectedListener?.onImageSelected(it)
//            imagePathsecond = it
//            requireActivity().supportFragmentManager.beginTransaction().apply {
//                val mealSearchFragment = SnapMealFragment()
//                val args = Bundle()
//                args.putString("homeTab", "homeTab")
//                args.putString("ModuleName", moduleName)
//                args.putString("mealType", mealType)
//                args.putString("selectedMealDate", selectedMealDate)
//                args.putString("gallery","gallery")
//                args.putString("ImagePathsecound", imagePathsecond.toString())
//                mealSearchFragment.arguments = args
//                replace(R.id.flFragment, mealSearchFragment, "SnapMealFragmentTag")
//                addToBackStack(null)
//                commit()
//            }
//            Toast.makeText(requireContext(), "Image loaded from gallery!", Toast.LENGTH_SHORT).show()
//        } ?: Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
//    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            imageSelectedListener?.onImageSelected(it)
            imagePathsecond = it
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = SnapMealFragment()
                val args = Bundle()
                args.putString("homeTab", "homeTab")
                args.putString("ModuleName", moduleName)
                args.putString("mealType", mealType)
                args.putString("selectedMealDate", selectedMealDate)
                args.putString("gallery","gallery")
                args.putString("ImagePathsecound", imagePathsecond.toString())
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "SnapMealFragmentTag")
                addToBackStack(null)
                commit()
            }
            Toast.makeText(requireContext(), "Image loaded from gallery!", Toast.LENGTH_SHORT).show()
            Toast.makeText(requireContext(), "Image loaded from gallery!", Toast.LENGTH_SHORT).show()
        }
    }

    private val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.CAMERA
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }.toTypedArray()

    // Function to replace fragments
    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction().apply {
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("mealType", mealType)
                args.putString("selectedMealDate", selectedMealDate)
                fragment.arguments = args
                replace(R.id.fragmentContainer, fragment, "homeTab")
                commit()
            }
    }

    private fun replaceFrequentlyFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction().apply {
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("mealType", mealType)
            args.putString("selectedMealDate", selectedMealDate)
            args.putString("searchType", "HomeTabMeal")
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            args.putParcelable("selectedMealLogList", mealLogRequests)
            args.putParcelable("selectedSnapMealLogList", snapMealLogRequests)
            args.putParcelable("snapMealRequestLocalListModel", snapMealRequestLocalListModel)
            fragment.arguments = args
            replace(R.id.fragmentContainer, fragment, "homeTab")
            commit()
        }
    }

    // Function to update tab selection colors
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateTabColors() {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            val customView = tab?.customView
            val tabText = customView?.findViewById<TextView>(R.id.tabText)

            if (tab?.isSelected == true) {
                tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                val typeface = resources.getFont(R.font.dmsans_bold)
                tabText?.typeface = typeface
            } else {
                val typeface = resources.getFont(R.font.dmsans_regular)
                tabText?.typeface = typeface
                tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_unselected_text))
            }
        }
    }

    fun setSelectedFrequentlyLog(mealLogRequest: MealLogItems?, isSnap: Boolean, mealLogRequest1: SelectedMealLogList?,
                                 snapMealLogRequest : SnapMealLogRequest?) {
        if (isSnap){
            isSnaps = isSnap
            if (mealLogRequest != null){
                frequentlyAddDishBottomSheetLayout.visibility = View.VISIBLE
                flexboxLayout.visibility = View.VISIBLE
                layoutTitle.visibility = View.VISIBLE
                btnLogMeal.visibility = View.VISIBLE
                if (mealLogRequest.isMealLogSelect){
                    ingredientsList.add(mealLogRequest.recipe_name.toString())
                    selectedSnapMealLogList.add(mealLogRequest)
                }else{
                    if (selectedSnapMealLogList.size > 0 && ingredientsList.size > 0){
                        val iterator = selectedSnapMealLogList.iterator()
                        while (iterator.hasNext()) {
                            val item = iterator.next()
                            if (item.meal_id == mealLogRequest.meal_id) {
                                iterator.remove() // safe removal
                                ingredientsList.remove(item.recipe_name.toString())
                            }
                        }
                    }
                }
                if (ingredientsList.size > 0){
                    updateIngredientChips()
                }else{
                    frequentlyAddDishBottomSheetLayout.visibility = View.GONE
                    recipeDetailsLocalListModel = null
                    mealLogRequests = null
                    snapMealLogRequests = null
                    snapMealRequestLocalListModel = null
                }
            }
            if (selectedSnapMealLogList.size > 0){
                val mealName = selectedSnapMealLogList.get(selectedSnapMealLogList.size-1).recipe_name
                val selectedSnapMealList = SelectedMealLogList(
                    meal_name = mealName,
                    meal_type = mealType,
                    meal_log = selectedSnapMealLogList
                )
                snapMealLogRequests = selectedSnapMealList
            }else{
                snapMealLogRequests = null
            }
            if (snapMealLogRequest != null){
                if (snapMealLogRequest.isSnapMealLogSelect){
                    snapMealLogRequestList.add(snapMealLogRequest)
                }else{
                    if (snapMealLogRequestList.size > 0){
                        val iterator = snapMealLogRequestList.iterator()
                        while (iterator.hasNext()) {
                            val item = iterator.next()
                            if (item.date == snapMealLogRequest.date) {
                                iterator.remove()
                            }
                        }
                    }
                }
            }
        }else{
            if (mealLogRequest != null){
                frequentlyAddDishBottomSheetLayout.visibility = View.VISIBLE
                flexboxLayout.visibility = View.VISIBLE
                layoutTitle.visibility = View.VISIBLE
                btnLogMeal.visibility = View.VISIBLE
                if (mealLogRequest.isMealLogSelect){
                    ingredientsList.add(mealLogRequest.recipe_name.toString())
                    selectedMealLogList.add(mealLogRequest)
                }else{
                    if (selectedMealLogList.size > 0 && ingredientsList.size > 0){
                        val iterator = selectedMealLogList.iterator()
                        while (iterator.hasNext()) {
                            val item = iterator.next()
                            if (item.meal_id == mealLogRequest.meal_id) {
                                iterator.remove() // safe removal from selectedMealLogList
                                ingredientsList.remove(item.recipe_name.toString())
                            }
                        }
                    }
                }
                if (ingredientsList.size > 0){
                    updateIngredientChips()
                }else{
                    frequentlyAddDishBottomSheetLayout.visibility = View.GONE
                    recipeDetailsLocalListModel = null
                    mealLogRequests = null
                    snapMealLogRequests = null
                    snapMealRequestLocalListModel = null
                }
            }

            if (mealLogRequest1 != null){
                frequentlyAddDishBottomSheetLayout.visibility = View.VISIBLE
                flexboxLayout.visibility = View.VISIBLE
                layoutTitle.visibility = View.VISIBLE
                btnLogMeal.visibility = View.VISIBLE
                if (mealLogRequest1.isMealLog){
                    ingredientsList.add(mealLogRequest1.meal_name.toString())
                }else{
                    if (ingredientsList.size > 0){
                        val iterator = ingredientsList.iterator()
                        while (iterator.hasNext()) {
                            val item = iterator.next()
                            if (item == mealLogRequest1.meal_name) {
                                iterator.remove()
                            }
                        }
                    }
                }
                if (ingredientsList.size > 0){
                    updateIngredientChips()
                }else{
                    frequentlyAddDishBottomSheetLayout.visibility = View.GONE
                    recipeDetailsLocalListModel = null
                    mealLogRequests = null
                    snapMealLogRequests = null
                    snapMealRequestLocalListModel = null
                }

                if (mealLogRequest1.isMealLog){
                    val selectedMealLog : ArrayList<MealLogItems> = ArrayList()
                    val mealLogList = mealLogRequest1.meal_log
                    mealLogList.forEach { selectedDish ->
                        val mealLogData = MealLogItems(
                            meal_id = selectedDish.meal_id,
                            recipe_name = selectedDish.recipe_name,
                            meal_quantity = selectedDish.meal_quantity,
                            source = selectedDish.source,
                            measure = selectedDish.measure
                        )
                        selectedMealLog.add(mealLogData)
                    }
                    selectedMealLogList.addAll(selectedMealLog)
                    mealLogRequestsList.add(mealLogRequest1)
                }else{
                    if (selectedMealLogList.size > 0){
                        val iterator = selectedMealLogList.iterator()
                        while (iterator.hasNext()) {
                            val item = iterator.next()
                            if (mealLogRequest1.meal_log.any { it.meal_id == item.meal_id }) {
                                iterator.remove()
                            }
                        }
                    }
                }
            }
            if (selectedMealLogList.size > 0 && ingredientsList.size > 0){
                val mealName = ingredientsList.get(ingredientsList.size-1)
                val selectedMealList = SelectedMealLogList(
                    meal_name = mealName,
                    meal_type = mealType,
                    meal_log = selectedMealLogList
                )
                mealLogRequests = selectedMealList
            }else{
                mealLogRequests = null
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
            val layoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 8, 8)
            }
            chipView.layoutParams = layoutParams
            btnRemove.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white), PorterDuff.Mode.SRC_IN)
            tvIngredient.text = ingredient
            btnRemove.setOnClickListener {
                val index = ingredientsList.indexOfFirst { it.startsWith(ingredient) }
                ingredientsList.remove(ingredient)
                updateIngredientChips()
                if (snapMealLogRequestList.isNotEmpty()) {
                    val iterator = snapMealLogRequestList.iterator()
                    while (iterator.hasNext()) {
                        val snapDish = iterator.next()
                        if (snapDish.meal_name.equals(ingredient, ignoreCase = true)) {
                            iterator.remove()
                            snapMealLogUpdateMethod(snapMealLogRequestList)
                            break // if only one item should be removed
                        }
                    }
                }
                if (dishLists.isNotEmpty()){
                    val iterator = dishLists.iterator()
                    while (iterator.hasNext()) {
                        val dishItem = iterator.next()
                        if (dishItem.recipe.equals(ingredient, ignoreCase = true)) {
                            iterator.remove()
                            // someUpdateMethod(ingredient)
                            break // if only one item should be removed
                        }
                    }
                }
                if (selectedMealLogList.isNotEmpty()){
                    val iterator = selectedMealLogList.iterator()
                    while (iterator.hasNext()) {
                        val mealLog = iterator.next()
                        if (mealLog.recipe_name.equals(ingredient, ignoreCase = true)) {
                            iterator.remove()
                            recipeLogAndFrequentlyLogUpdateMethod(selectedMealLogList)
                            break // if only one item should be removed
                        }
                    }
                }

                if (mealLogRequestsList.isNotEmpty() && selectedMealLogList.isNotEmpty()) {
                    // Find the meal by ingredient
                    val mealToRemove = mealLogRequestsList.find { it.meal_name.equals(ingredient, ignoreCase = true) }
                    if (mealToRemove != null) {
                        // Collect mealIds from this meal
                        val mealIds = mealToRemove.meal_log.map { it.meal_id }
                        // Safely remove all selected meals that match
                        selectedMealLogList.removeAll { selected ->
                            mealIds.contains(selected.meal_id)
                        }
                        // Safely remove the meal itself
                        mealLogRequestsList.remove(mealToRemove)
                        mealLogUpdateMethod(mealLogRequestsList)
                    }
                }
            }
            flexboxLayout.addView(chipView)
        }
        tvIngredientsCount.text = ""+ ingredientsList.size + " Dishes/ Ingredients Added"
        if (ingredientsList.isEmpty()){
            frequentlyAddDishBottomSheetLayout.visibility = View.GONE
            recipeDetailsLocalListModel = null
            mealLogRequests = null
            snapMealLogRequests = null
            snapMealRequestLocalListModel = null
        }
    }

    private fun recipeLogAndFrequentlyLogUpdateMethod(newData: List<MealLogItems>) {
        sharedViewModel.recipeLogAndFrequentlyLogUpdateMealData(newData)
    }

    private fun mealLogUpdateMethod(newData: List<SelectedMealLogList>) {
        sharedViewModel.mealLogUpdateMealData(newData)
    }

    private fun snapMealLogUpdateMethod(newData:  List<SnapMealLogRequest>) {
        sharedViewModel.snapMealLogUpdateMealData(newData)
    }

    private fun createDishLog() {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = selectedMealDate  //?: currentDateTime.format(formatter)

        val dishLogList : ArrayList<DishLog> = ArrayList()
        val recipes: ArrayList<RecipeLogRequest> = ArrayList()
        val ingredients: ArrayList<IngredientLogRequest> = ArrayList()
        val mealNamesString = dishLists.map { it.recipe ?: "" }.joinToString(", ")

        if (recipeDetailsLocalListModel != null){
            if (dishLists.size > 0) {
                dishLists?.forEach { snapRecipe ->
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
//                    val mealLogData = DishLog(
//                        receipe_id = snapRecipe.id,
//                        meal_quantity = mealQuantity.toDouble(),
//                        unit = "g",
//                        measure = "Bowl"
//                    )
//                    dishLogList.add(mealLogData)
                }
            }
        }

        if (mealLogRequests != null){
            if (selectedMealLogList.size > 0){
                selectedMealLogList?.forEach { selectedDish ->
                    if (selectedDish.source.equals("recipe")){
                        val mealRecipeData = RecipeLogRequest(
                            recipe_id = selectedDish.meal_id,
                            selected_serving_type = selectedDish.measure,
                            selected_serving_value = selectedDish.meal_quantity
                        )
                        recipes.add(mealRecipeData)
                    }
                    if (selectedDish.source.equals("ingredient")){
                        val mealIngredientData = IngredientLogRequest(
                            ingredient_id = selectedDish.meal_id,
                            meal_quantity = selectedDish.meal_quantity,
                            standard_serving_size = selectedDish.measure
                        )
                        ingredients.add(mealIngredientData)
                    }
//                    val mealLogData = DishLog(
//                        receipe_id = selectedDish.meal_id,
//                        meal_quantity = mealQuantity.toDouble(),
//                        unit = "g",
//                        measure = "Bowl"
//                    )
//                    dishLogList.add(mealLogData)
                }
            }
        }
        val dishLogRequest = SaveDishLogRequest(
            date = selectedMealDate,
            meal_type = mealType ?: "dd",
            recipes = recipes,
            ingredients = ingredients
        )
        val call = ApiClient.apiServiceFastApiV2.createSaveMealsToLog(userId, formattedDate, dishLogRequest)
        call.enqueue(object : Callback<MealUpdateResponse> {
            override fun onResponse(call: Call<MealUpdateResponse>, response: Response<MealUpdateResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealData = response.body()?.message
                    showCustomToast(requireContext(), mealData)
                   // Toast.makeText(activity, mealData, Toast.LENGTH_SHORT).show()
                    flexboxLayout.visibility = View.GONE
                    layoutTitle.visibility = View.GONE
                    btnLogMeal.visibility = View.GONE
                    checkCircle.visibility = View.VISIBLE
                    loggedSuccess.visibility = View.VISIBLE
                    loggedSuccess.text = mealData
                    frequentlyAddDishBottomSheetLayout.visibility = View.GONE
                    val fragment = YourMealLogsFragment()
                    val args = Bundle()
                    args.putString("ModuleName", "EatRightLandingWithoutPopup")
                    args.putString("selectedMealDate", selectedMealDate)
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
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

    private fun createSnapMealLog(snapRecipeList: SnapMealLogRequest) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }

        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val inputDateStr = selectedMealDate
        val localDate = LocalDate.parse(inputDateStr)
        val localDateTime = localDate.atStartOfDay()
       // val localDateTime = localDate.atTime(4, 0) // 4:00 AM
        val utcZonedDateTime = localDateTime.atZone(ZoneOffset.UTC)
        val utcInstant = utcZonedDateTime.toInstant()
        val utcDateString = utcInstant.toString()  // "2025-08-04T00:00:00Z"
        val currentDateUtc: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        if (snapRecipeList.dish.isNotEmpty()) {
            val snapMealLogRequest = SnapMealLogRequest(
                user_id = userId,
                meal_type = mealType,
                meal_name = snapRecipeList.meal_name,
                is_save = false,
                is_snapped = true,
                date = utcDateString,
                dish = snapRecipeList.dish,
                image_url = ""
            )
            val gson = Gson()
            val jsonString = gson.toJson(snapMealLogRequest) // snapMealLogRequest is your model instance
            Log.d("JSON Output", jsonString)
            val call = ApiClient.apiServiceFastApiV2.createSnapMealLog(snapMealLogRequest)
            call.enqueue(object : Callback<SnapMealLogResponse> {
                override fun onResponse(
                    call: Call<SnapMealLogResponse>,
                    response: Response<SnapMealLogResponse>
                ) {
                    if (response.isSuccessful) {
                        if (isAdded  && view != null){
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                        snapMealRequestCount++
                        val mealData = response.body()?.message
                        showCustomToast(requireContext(), mealData)
                       // Toast.makeText(activity, mealData, Toast.LENGTH_SHORT).show()
                        if (snapMealLogRequestList.size == snapMealRequestCount){
                            flexboxLayout.visibility = View.GONE
                            layoutTitle.visibility = View.GONE
                            btnLogMeal.visibility = View.GONE
                            checkCircle.visibility = View.VISIBLE
                            loggedSuccess.visibility = View.VISIBLE
                            loggedSuccess.text = mealData
                            frequentlyAddDishBottomSheetLayout.visibility = View.GONE
                            val fragment = YourMealLogsFragment()
                            val args = Bundle()
                            args.putString("ModuleName", moduleName)
                            args.putString("selectedMealDate", selectedMealDate)
                            fragment.arguments = args
                            requireActivity().supportFragmentManager.beginTransaction().apply {
                                replace(R.id.flFragment, fragment, "landing")
                                addToBackStack("landing")
                                commit()
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
                override fun onFailure(call: Call<SnapMealLogResponse>, t: Throwable) {
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
    }

//    fun LocalDateTime.dayWith4AmBoundary(): LocalDate {
//        return if (this.toLocalTime().isBefore(LocalTime.of(4, 0))) {
//            this.toLocalDate().minusDays(1)
//        } else {
//            this.toLocalDate()
//        }
//    }
//
//    val dateTime = LocalDateTime.of(2026, 1, 30, 1, 30) // 1:30 AM
//    val logicalDay = dateTime.dayWith4AmBoundary()

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

    private fun mealSaveQuitDialog() {
        val mealSaveQuitBottomSheet = MealSaveQuitBottomSheet()
        mealSaveQuitBottomSheet.isCancelable = true
        parentFragment.let { mealSaveQuitBottomSheet.show(childFragmentManager, "MealSaveQuitBottomSheet") }
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    private fun loggedAddDish(recipeDetailsLocalListModel: RecipeDetailsLocalListModel?) {
        val frequentlyAddDishBottomSheet = FrequentlyAddDishBottomSheet()
        frequentlyAddDishBottomSheet.isCancelable = true
        val args = Bundle()
        args.putString("mealType", mealType)
        args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
        args.putBoolean("test",false)
        frequentlyAddDishBottomSheet.arguments = args
        activity?.supportFragmentManager?.let { frequentlyAddDishBottomSheet.show(it, "FrequentlyAddDishBottomSheet") }
    }

    private fun createSnapMealLog() {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val snapMealLogList : ArrayList<MealLogItem> = ArrayList()
        if (snapMealLogRequests != null){
            if (selectedSnapMealLogList.size > 0){
                selectedSnapMealLogList?.forEach { selectedDish ->
                    val mealLogData = MealLogItem(
                        meal_id = selectedDish.meal_id,
                        meal_quantity = 1,
                        unit = "g",
                        measure = "Bowl"
                    )
                    snapMealLogList.add(mealLogData)
                }
            }
        }
        val snapMealLogRequest = SaveSnapMealLogRequest(
            meal_type = mealType,
            meal_name = "Meal1",
            meal_log = snapMealLogList
        )
        val call = ApiClient.apiServiceFastApi.createSaveSnapMealsToLog(userId, formattedDate, snapMealLogRequest)
        call.enqueue(object : Callback<MealUpdateResponse> {
            override fun onResponse(call: Call<MealUpdateResponse>, response: Response<MealUpdateResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealData = response.body()?.message
                    Toast.makeText(activity, mealData, Toast.LENGTH_SHORT).show()
                    flexboxLayout.visibility = View.GONE
                    layoutTitle.visibility = View.GONE
                    btnLogMeal.visibility = View.GONE
                    checkCircle.visibility = View.VISIBLE
                    loggedSuccess.visibility = View.VISIBLE
                    loggedSuccess.text = mealData
                    frequentlyAddDishBottomSheetLayout.visibility = View.GONE
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

    private fun getMealDetails(selectedMealLogListModels: SelectedMealLogList?) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val call = ApiClient.apiServiceFastApi.fetchMealDetails(userId, "")
        call.enqueue(object : Callback<SnapMealDetailsResponse> {
            override fun onResponse(call: Call<SnapMealDetailsResponse>, response: Response<SnapMealDetailsResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealDetails = response.body()?.data
                    if (mealDetails != null){
                        println(mealDetails)
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
            override fun onFailure(call: Call<SnapMealDetailsResponse>, t: Throwable) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        sharedViewModel.recipeLogAndFrequentlyLogUpdateMealData(emptyList())
        sharedViewModel.mealLogUpdateMealData(emptyList())
        sharedViewModel.snapMealLogUpdateMealData(emptyList())
    }

    override fun onMealSaveQuit(mealData: String) {
        val fragment = YourMealLogsFragment()
        val args = Bundle()
        args.putString("ModuleName", moduleName)
        args.putString("selectedMealDate", selectedMealDate)
        fragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, "landing")
            addToBackStack("landing")
            commit()
        }
    }
}