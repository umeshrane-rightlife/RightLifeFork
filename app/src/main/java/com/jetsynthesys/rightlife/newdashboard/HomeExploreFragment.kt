package com.jetsynthesys.rightlife.newdashboard

import PromotionWeeklyResponse
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseFragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.apimodel.rledit.RightLifeEditResponse
import com.jetsynthesys.rightlife.apimodel.servicepane.HomeService
import com.jetsynthesys.rightlife.apimodel.servicepane.ServicePaneResponse
import com.jetsynthesys.rightlife.apimodel.submodule.SubModuleData
import com.jetsynthesys.rightlife.apimodel.submodule.SubModuleResponse
import com.jetsynthesys.rightlife.apimodel.welnessresponse.ContentWellness
import com.jetsynthesys.rightlife.apimodel.welnessresponse.WellnessApiResponse
import com.jetsynthesys.rightlife.databinding.FragmentHomeExploreBinding
import com.jetsynthesys.rightlife.newdashboard.model.ContentDetails
import com.jetsynthesys.rightlife.newdashboard.model.ContentResponse
import com.jetsynthesys.rightlife.runWhenAttached
import com.jetsynthesys.rightlife.ui.ActivityUtils
import com.jetsynthesys.rightlife.ui.ActivityUtils.startFaceScanActivity
import com.jetsynthesys.rightlife.ui.Articles.ArticlesDetailActivity
import com.jetsynthesys.rightlife.ui.CardItem
import com.jetsynthesys.rightlife.ui.CircularCardAdapter
import com.jetsynthesys.rightlife.ui.NewCategoryListActivity
import com.jetsynthesys.rightlife.ui.ServicePaneAdapter
import com.jetsynthesys.rightlife.ui.contentdetailvideo.ContentDetailsActivity
import com.jetsynthesys.rightlife.ui.contentdetailvideo.SeriesListActivity
import com.jetsynthesys.rightlife.ui.mindaudit.MindAuditFromActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.FeatureFlags
import com.jetsynthesys.rightlife.ui.utility.NetworkUtils
import com.jetsynthesys.rightlife.ui.voicescan.VoiceScanActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import kotlin.math.abs


class HomeExploreFragment : BaseFragment() {
    private var _binding: FragmentHomeExploreBinding? = null
    private val binding get() = _binding!!

    private var sliderHandler: Handler? = null // Handler for scheduling auto-slide
    private var sliderRunnable: Runnable? = null
    private val cardItems: ArrayList<CardItem> = ArrayList()
    private var adapter: CircularCardAdapter? = null

    var rightLifeEditResponse: RightLifeEditResponse? = null
    var wellnessApiResponse: WellnessApiResponse? = null
    var ThinkRSubModuleResponse: SubModuleResponse? = null
    var MoveRSubModuleResponse: SubModuleResponse? = null
    var EatRSubModuleResponse: SubModuleResponse? = null
    var SleepRSubModuleResponse: SubModuleResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //(requireActivity() as? HomeNewActivity)?.showHeader(true)

        // Initialize Handler and Runnable for Auto-Sliding
        sliderHandler = Handler(Looper.getMainLooper())
        sliderRunnable = object : Runnable {
            override fun run() {
                val nextItem: Int = binding.viewPager.currentItem + 1 // Move to the next item
                binding.viewPager.setCurrentItem(nextItem, true)
                sliderHandler?.removeCallbacks(sliderRunnable!!)
                sliderHandler?.postDelayed(this, 5000) // Change slide every 3 seconds
            }
        }

        val swipeRefreshLayout = activity?.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        swipeRefreshLayout?.setOnRefreshListener {
            // Call your refresh logic
            sliderHandler?.postDelayed(sliderRunnable!!, 3000)
            getPromotionListWeekly()
            //getPromotionList()
            getRightLifeEdit()
            getWellnessPlaylist()
            swipeRefreshLayout.isRefreshing = false // Stop the spinner
        }

        callAPIS()

        sliderHandler?.removeCallbacks(sliderRunnable!!)

        // Start Auto-Sliding
        sliderHandler?.postDelayed(sliderRunnable!!, 5000)

        /*binding.refreshLayout.setOnRefreshListener {
            callAPIS()
            binding.refreshLayout.isRefreshing = false
        }

        binding.scrollView.setOnScrollChangeListener { view: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            binding.refreshLayout.setEnabled(
                scrollY <= 5
            )
        }*/

        setClickListeners()
    }

    private fun setClickListeners() {
        // 1. Simple Navigation
        binding.imgJumpBackInNext.setOnClickListener {
            startActivity(Intent(requireContext(), JumpInBackActivity::class.java))
        }

        // 2. Edit Details (Mapped)
        listOf(binding.relativeRledit1, binding.relativeRledit2, binding.relativeRledit3)
            .forEachIndexed { index, view ->
                view.setSafeClickListener { callRlEditDetailActivity(index) }
            }

        // 3. Wellness Details (Mapped)
        listOf(
            binding.relativeWellness1,
            binding.relativeWellness2,
            binding.relativeWellness3,
            binding.relativeWellness4
        )
            .forEachIndexed { index, view ->
                view.setSafeClickListener { callWellnessDetailActivity(index) }
            }

        // 5. Explore Buttons
        binding.btnSrExplore.setSafeClickListener {
            SleepRSubModuleResponse?.let {
                callExploreModuleActivity(
                    it
                )
            }
        }
        binding.btnTrExplore.setSafeClickListener {
            ThinkRSubModuleResponse?.let {
                callExploreModuleActivity(
                    it
                )
            }
        }
        binding.btnErExplore.setSafeClickListener {
            EatRSubModuleResponse?.let {
                callExploreModuleActivity(
                    it
                )
            }
        }
        binding.btnMrExplore.setSafeClickListener {
            MoveRSubModuleResponse?.let {
                callExploreModuleActivity(
                    it
                )
            }
        }
    }

    private fun setupCategoryClicks(views: List<View>, responseData: List<SubModuleData>?) {
        views.forEachIndexed { index, view ->
            view.setSafeClickListener {
                responseData?.getOrNull(index)?.let { item ->
                    val intent =
                        Intent(requireContext(), NewCategoryListActivity::class.java).apply {
                            putExtra("Categorytype", item.categoryId)
                            putExtra("moduleId", item.moduleId)
                        }
                    startActivity(intent)
                }
            }
        }
    }

    // Inside your Fragment class
    private fun View.setSafeClickListener(action: () -> Unit) {
        this.setOnClickListener {
            // 'context' is available directly inside a Fragment
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                action()
            } else {
                // Ensure this method exists in your Fragment or BaseFragment
                showInternetError()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Resume auto-slide when activity is visible
        sliderHandler?.postDelayed(sliderRunnable!!, 3000)
        //getPromotionList()
        getPromotionListWeekly()
        getRightLifeEdit()
        getWellnessPlaylist()
        lifecycleScope.launch {
            delay(2000)
            getJumpBackInData()
            (requireActivity() as? HomeNewActivity)?.showChallengeCard()
        }
    }

    private fun callAPIS() {
        getPromotionList2() // ModuleService pane
        getRightLifeEdit()

        //getAffirmations()

        getWellnessPlaylist()

        getModuleContent()

        getSubModuleContent("THINK_RIGHT")
        getSubModuleContent("MOVE_RIGHT")
        getSubModuleContent("EAT_RIGHT")
        getSubModuleContent("SLEEP_RIGHT")

        getJumpBackInData()
    }

    private fun getPromotionListWeekly() {
        val call = apiService.getPromotionListWeekly(
            sharedPreferenceManager.accessToken,
            "HOME_PAGE",
            "TOP",
            sharedPreferenceManager.userId
        )
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())
                    val promotionResponse = gson.fromJson(
                        jsonResponse,
                        PromotionWeeklyResponse::class.java
                    )
                    if (promotionResponse.success) {
                        runWhenAttached { handlePromotionResponseWeekly(promotionResponse) }
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            "Failed: " + promotionResponse.statusCode,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })
    }

    private fun getPromotionList2() {
        val call = apiService.getPromotionList2(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    val responseObj = gson.fromJson(
                        jsonResponse,
                        ServicePaneResponse::class.java
                    )
                    runWhenAttached { handleServicePaneResponse(responseObj) }
                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })
    }

    private fun getRightLifeEdit() {
        val call = apiService.getRightlifeEdit(sharedPreferenceManager.accessToken, "HOME")
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    gson.fromJson(
                        jsonResponse,
                        RightLifeEditResponse::class.java
                    )
                    rightLifeEditResponse = gson.fromJson(
                        jsonResponse,
                        RightLifeEditResponse::class.java
                    )
                    runWhenAttached { setupRLEditContent(rightLifeEditResponse) }
                } else {
                    val statusCode = response.code()
                    try {
                        val errorMessage = response.errorBody()!!.string()
                        Log.e(
                            "Error",
                            "HTTP error code: $statusCode, message: $errorMessage"
                        )
                        Log.e("Error", "Error message: $errorMessage")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })
    }

    private fun getWellnessPlaylist() {
        val call =
            apiService.getWelnessPlaylist(sharedPreferenceManager.accessToken, "SERIES", "WELLNESS")
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    wellnessApiResponse = gson.fromJson(
                        jsonResponse,
                        WellnessApiResponse::class.java
                    )
                    binding.tvHeaderWellnessplay.text =
                        wellnessApiResponse?.data?.sectionTitle ?: "Your Wellness Playlist"
                    binding.tvDescWellness.text = wellnessApiResponse?.data?.sectionSubtitle ?: ""
                    wellnessApiResponse?.data?.contentList?.let {
                        runWhenAttached {
                            setupWellnessContent(
                                it
                            )
                        }
                    }
                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })
    }

    private fun getModuleContent() {
        val call = apiService.getmodule(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    gson.toJson(response.body())

                    // LiveEventResponse ResponseObj = gson.fromJson(jsonResponse,LiveEventResponse.class);
                    //Log.d("API Response body", "Success:AuthorName " + ResponseObj.getData().get(0).getAuthorName());
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })
    }

    private fun getSubModuleContent(moduleid: String) {
        val call =
            apiService.getsubmodule(sharedPreferenceManager.accessToken, moduleid, "CATEGORY")
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val affirmationsResponse = response.body()
                    val gson = Gson()

                    runWhenAttached {
                        if (moduleid.equals("THINK_RIGHT", ignoreCase = true)) {
                            ThinkRSubModuleResponse = gson.fromJson(
                                affirmationsResponse.toString(),
                                SubModuleResponse::class.java
                            )
                            handleThinkRightResponse()
                        } else if (moduleid.equals("MOVE_RIGHT", ignoreCase = true)) {
                            MoveRSubModuleResponse = gson.fromJson(
                                affirmationsResponse.toString(),
                                SubModuleResponse::class.java
                            )
                            handleMoveRightResponse()
                        } else if (moduleid.equals("EAT_RIGHT", ignoreCase = true)) {
                            EatRSubModuleResponse = gson.fromJson(
                                affirmationsResponse.toString(),
                                SubModuleResponse::class.java
                            )
                            handleEatRightResponse()
                        } else if (moduleid.equals("SLEEP_RIGHT", ignoreCase = true)) {
                            SleepRSubModuleResponse = gson.fromJson(
                                affirmationsResponse.toString(),
                                SubModuleResponse::class.java
                            )
                            handleSleepRightResponse()
                        }
                        (requireActivity() as? HomeNewActivity)?.isCategoryModuleLoaded = true
                    }
                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })
    }

    private fun bindCategoryData(textView: TextView, imageView: ImageView, item: SubModuleData?) {
        if (item == null) return

        textView.text = item.name

        Glide.with(requireActivity())
            .load(ApiClient.CDN_URL_QA + item.imageUrl)
            .placeholder(R.drawable.rl_placeholder)
            .error(R.drawable.rl_placeholder)
            .into(imageView)
    }

    private fun setupSubModuleUI(
        textViews: List<TextView>,
        imageViews: List<ImageView>,
        data: List<SubModuleData>?
    ) {
        if (data.isNullOrEmpty()) return

        // Iterate through the available views and bind data if it exists at that index
        textViews.forEachIndexed { index, textView ->
            val imageView = imageViews.getOrNull(index)
            val item = data.getOrNull(index)

            if (imageView != null && item != null) {
                bindCategoryData(textView, imageView, item)
            }
        }
    }

    private fun handleThinkRightResponse() {
        setupSubModuleUI(
            textViews = listOf(
                binding.tvThinkRightCategory1,
                binding.tvThinkRightCategory2,
                binding.tvThinkRightCategory3,
                binding.tvThinkRightCategory4
            ),
            imageViews = listOf(
                binding.imageThinkRightCategory1,
                binding.imageThinkRightCategory2,
                binding.imageThinkRightCategory3,
                binding.imageThinkRightCategory4
            ),
            data = ThinkRSubModuleResponse?.data
        )
        setupCategoryClicks(
            views = listOf(
                binding.llThinkrightCategory1,
                binding.llThinkrightCategory2,
                binding.llThinkrightCategory3,
                binding.llThinkrightCategory4
            ),
            responseData = ThinkRSubModuleResponse?.data
        )
    }

    private fun handleMoveRightResponse() {
        // Note: I preserved your specific index mapping (1 -> 3, 2 -> 2) by ordering the list
        setupSubModuleUI(
            textViews = listOf(
                binding.tvMoveRightCategory1,
                binding.tvMoveRightCategory3,
                binding.tvMoveRightCategory2
            ),
            imageViews = listOf(
                binding.imageMoveRightCategory1,
                binding.imageMoveRightCategory3,
                binding.imageMoveRightCategory2
            ),
            data = MoveRSubModuleResponse?.data
        )

        setupCategoryClicks(
            views = listOf(
                binding.llMoverightCategory1,
                binding.llMoverightCategory3,
                binding.llMoverightCategor2
            ), // Note: kept your specific order 0, 1, 2
            responseData = MoveRSubModuleResponse?.data
        )
    }

    private fun handleEatRightResponse() {
        setupSubModuleUI(
            textViews = listOf(
                binding.tvEatRightCategory1,
                binding.tvEatRightCategory2,
                binding.tvEatRightCategory3,
                binding.tvEatRightCategory4
            ),
            imageViews = listOf(
                binding.imageEatRightCategory1,
                binding.imageEatRightCategory2,
                binding.imageEatRightCategory3,
                binding.imageEatRightCategory4
            ),
            data = EatRSubModuleResponse?.data
        )

        setupCategoryClicks(
            views = listOf(
                binding.llEatrightCategory1,
                binding.llEatrightCategory2,
                binding.llEatrightCategory3,
                binding.llEatrightCategory4
            ),
            responseData = EatRSubModuleResponse?.data
        )
    }

    private fun handleSleepRightResponse() {
        setupSubModuleUI(
            textViews = listOf(
                binding.tvSleepRightCategory1,
                binding.tvSleepRightCategory2,
                binding.tvSleepRightCategory3
            ),
            imageViews = listOf(
                binding.imageSleepRightCategory1,
                binding.imageSleepRightCategory2,
                binding.imageSleepRightCategory3
            ),
            data = SleepRSubModuleResponse?.data
        )

        setupCategoryClicks(
            views = listOf(
                binding.llSleeprightCategory1,
                binding.llSleeprightCategory2,
                binding.llSleeprightCategory3
            ),
            responseData = SleepRSubModuleResponse?.data
        )
    }

    private fun handlePromotionResponseWeekly(promotionResponse: PromotionWeeklyResponse) {
        cardItems.clear()
        promotionResponse.data.promotionList.indices.forEach { i ->
            val promo = promotionResponse.data.promotionList[i]
            val cardItem = CardItem(
                promo._id,
                promo.name,
                R.drawable.facialconcept,
                promo.desktopImage,
                promo.content,
                promo.buttonName,
                promo.category,
                promo.views.toString(),
                promo.seriesId,
                promo.seriesType,
                promo.selectedContentType,
                promo.titleImage,
                promo.buttonImage,
                promo.contentId // ✅ NEW
            )
            cardItems.add(i, cardItem)
        }

        if (cardItems.isNotEmpty()) {
            binding.viewPager.visibility = View.VISIBLE
            adapter = CircularCardAdapter(requireActivity(), cardItems) { item: CardItem ->

                if (!NetworkUtils.isInternetAvailable(requireContext())) {
                    showInternetError()
                    return@CircularCardAdapter
                }
                handlePromotionBannerTap(item)
            }

            binding.viewPager.adapter = adapter
            adapter?.notifyDataSetChanged()


            // Set up the initial position for circular effect
            val initialPosition = Int.MAX_VALUE / 2
            binding.viewPager.setCurrentItem(
                initialPosition - initialPosition % cardItems.size,
                false
            )

            binding.viewPager.apply {
                clipToPadding = false
                clipChildren = false
                offscreenPageLimit = 1
                setPadding(60, 0, 60, 0)
            }

            // Set offscreen page limit and page margin
            binding.viewPager.offscreenPageLimit = 1 // Load adjacent pages
            binding.viewPager.clipToPadding = false
            binding.viewPager.clipChildren = false
            binding.viewPager.setPageTransformer { page, position ->
                val MIN_SCALE = 0.9f     // center = 1.0, side cards smaller
                val MIN_ALPHA = 0.7f
                val translationFactor = 4f  // controls overlap/spacing

                // Keep center card on top
                page.z = 1f - abs(position)

                // Scale cards
                val scale = MIN_SCALE + (1 - abs(position)) * (1 - MIN_SCALE)
                page.scaleX = scale
                page.scaleY = scale

                // Fade cards slightly
                val alpha = MIN_ALPHA + (1 - abs(position)) * (1 - MIN_ALPHA)
                page.alpha = alpha

                // Adjust horizontal position for peeking
                page.translationX = -position * page.width / translationFactor
            }

        } else {
            binding.viewPager.visibility = View.GONE
        }
    }

    private fun handlePromotionBannerTap(item: CardItem) {
        val seriesType = item.seriesType?.trim()?.lowercase().orEmpty()
        val category = item.category?.trim()?.lowercase().orEmpty()
        val homeActivity = requireActivity() as? HomeNewActivity

        when {
            // 1. Handle DAILY Logic
            seriesType == "daily" -> {
                val seriesId = item.seriesId?.trim().orEmpty()
                if (seriesId.isNotEmpty()) {
                    startActivity(Intent(requireContext(), SeriesListActivity::class.java).apply {
                        putExtra("contentId", seriesId)
                    })
                } else {
                    openContentByType(item.selectedContentType, item.contentId?.trim().orEmpty())
                }
            }

            // 2. Handle LIVE Logic
            seriesType == "live" || category == "live" -> {
                Toast.makeText(requireContext(), "Live Content", Toast.LENGTH_SHORT).show()
            }

            // 3. Handle COMMERCIAL / Specific Categories
            else -> {
                when (normalizePromoCategory(category)) {
                    "face_scan" -> homeActivity?.callFaceScanClick()
                    "mind_audit" -> {
                        if (homeActivity?.checkTrailEndedAndShowDialog() == true) {
                            ActivityUtils.startMindAuditActivity(requireContext())
                        }
                    }

                    "meal_snap" -> homeActivity?.callSnapMealClick()
                    "voice_scan" -> startActivity(
                        Intent(
                            requireContext(),
                            VoiceScanActivity::class.java
                        )
                    )
                }
            }
        }
    }

    private fun openContentByType(contentType: String?, contentId: String) {
        if (contentId.isBlank()) return

        when {
            contentType.equals("TEXT", ignoreCase = true) -> {
                startActivity(Intent(requireContext(), ArticlesDetailActivity::class.java).apply {
                    putExtra("contentId", contentId)
                })
                AnalyticsLogger.logEvent(
                    requireActivity(),
                    AnalyticsEvent.Home_PromBan_Art_Tap
                )
            }

            contentType.equals("VIDEO", ignoreCase = true) || contentType.equals(
                "AUDIO",
                ignoreCase = true
            ) -> {
                startActivity(Intent(requireContext(), ContentDetailsActivity::class.java).apply {
                    putExtra("contentId", contentId)
                })

                if (contentType.equals("VIDEO", ignoreCase = true))
                    AnalyticsLogger.logEvent(
                        requireActivity(),
                        AnalyticsEvent.Home_PromBan_Vid_Tap
                    )
                else
                    AnalyticsLogger.logEvent(
                        requireActivity(),
                        AnalyticsEvent.Home_PromBan_Audio_Tap
                    )
            }

            contentType.equals("SERIES", ignoreCase = true) -> {
                startActivity(Intent(requireContext(), SeriesListActivity::class.java).apply {
                    putExtra("contentId", contentId)
                })
            }

            else -> {
                // fallback
                startActivity(Intent(requireContext(), ContentDetailsActivity::class.java).apply {
                    putExtra("contentId", contentId)
                })
            }
        }
    }

    private fun normalizePromoCategory(raw: String): String {
        return when (val s = raw.trim().lowercase().replace(" ", "_").replace("-", "_")) {
            "facial_scan", "face_scan", "health_cam", "healthcam", "facialscan", "facescan" -> "face_scan"
            "mind_audit", "health_audit", "mindaudit", "healthaudit" -> "mind_audit"
            "meal_snap", "snap_meal", "mealsnap", "snapmeal" -> "meal_snap"
            "voice_scan", "voicescan" -> "voice_scan"
            else -> s
        }
    }


    private fun handleServicePaneResponse(responseObj: ServicePaneResponse) {
        val adapter = ServicePaneAdapter(
            requireActivity(), responseObj.data.homeServices
        ) { homeService: HomeService ->
            when (homeService.title) {
                "Voice Scan" -> {
                    val intentVoice =
                        Intent(requireContext(), MindAuditFromActivity::class.java)
                    startActivity(intentVoice)
                }

                "Mind Audit" -> {
                    if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
                        if (NetworkUtils.isInternetAvailable(requireContext())) {
                            freeTrialDialogActivity()
                        } else {
                            showInternetError()
                        }
                    } else {
                        /*  ActivityUtils.startEatRightReportsActivity(
                              requireContext(),
                              "SnapMealTypeEat",
                              ""
                          )*/

                        (requireActivity() as? HomeNewActivity)?.callMindAuditClick()
                    }

                }

                "Meal Snap" -> {
                    if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
                        if (NetworkUtils.isInternetAvailable(requireContext())) {
                            freeTrialDialogActivity(FeatureFlags.MEAL_SCAN)
                        } else {
                            showInternetError()
                        }
                    } else {
                        /*  ActivityUtils.startEatRightReportsActivity(
                              requireContext(),
                              "SnapMealTypeEat",
                              ""
                          )*/
                        (requireActivity() as? HomeNewActivity)?.callSnapMealClick()
                    }
                    //ActivityUtils.startMindAuditActivity(requireContext())
                }

                "Health Cam" -> {
                    if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
                        if (NetworkUtils.isInternetAvailable(requireContext())) {
                            freeTrialDialogActivity(FeatureFlags.FACE_SCAN)
                        } else {
                            showInternetError()
                        }
                    } else {
                        //ActivityUtils.startFaceScanActivity(requireContext())
                        (requireActivity() as? HomeNewActivity)?.callFaceScanClick()
                    }
                }

                "Face Scan" -> {
                    if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
                        if (NetworkUtils.isInternetAvailable(requireContext())) {
                            freeTrialDialogActivity(FeatureFlags.FACE_SCAN)
                        } else {
                            showInternetError()
                        }
                    } else {
                        //ActivityUtils.startFaceScanActivity(requireContext())
                        (requireActivity() as? HomeNewActivity)?.callFaceScanClick()
                    }
                }

                else -> {
                    startFaceScanActivity(requireContext())
                }
            }
        }
        var spanCount = responseObj.data.homeServices.size
        spanCount = if ((spanCount > 3)) 2 else spanCount

        binding.rvServicePane.layoutManager =
            GridLayoutManager(requireContext(), spanCount)
        binding.rvServicePane.adapter = adapter
    }

    private fun setupRLEditContent(response: RightLifeEditResponse?) {
        // 1. Safe Unwrapping: Return early if data is invalid
        val data = response?.data
        val topList = data?.topList

        if (topList.isNullOrEmpty()) {
            binding.rlRightlifeEdit.visibility = View.GONE
            return
        }

        // 2. Use 'with' to avoid repeating 'binding.'
        with(binding) {
            rlRightlifeEdit.visibility = View.VISIBLE
            tvHeaderHealth.text = data.sectionTitle ?: "The RightLife Edit"
            tvDescriptionHealth.text =
                data.sectionSubtitle ?: "Discover curated content to help you thrive."

            // --- BIND ITEM 0 (HERO ITEM) ---
            val item0 = topList.getOrNull(0)
            if (item0 != null) {
                tvRledtContTitle1.text = item0.title

                val artist = item0.artist?.firstOrNull()
                nameeditor.text = "${artist?.firstName.orEmpty()} ${artist?.lastName.orEmpty()}"

                count.text = item0.viewCount.toString()

                // Specific Hero Logic for Play/Read Icon
                val typeIcon = if (item0.contentType.equals("VIDEO", ignoreCase = true)) {
                    R.drawable.ic_playrledit
                } else {
                    R.drawable.read
                }
                imgContenttypeRledit.setImageResource(typeIcon)

                // Load Hero Image
                if (item0.thumbnail != null) {
                    Glide.with(root.context)
                        .load(ApiClient.CDN_URL_QA + item0.thumbnail.url)
                        .placeholder(R.drawable.rl_placeholder)
                        .error(R.drawable.rl_placeholder)
                        .into(imgRledit)
                }
                setcontenttypeIcon(itemTextRledit, imgIconviewRledit, item0.contentType)
            }

            // --- LOCAL HELPER FOR ITEMS 1 & 2 ---
            fun bindSmallItem(
                index: Int,
                container: View,
                titleTv: TextView,
                nameTv: TextView,
                countTv: TextView,
                imgView: ImageView,
                typeTv: TextView,
                typeIconView: ImageView
            ) {
                val item = topList.getOrNull(index)

                if (item == null) {
                    container.visibility = View.GONE
                    return
                }

                container.visibility = View.VISIBLE
                titleTv.text = item.title

                val artist = item.artist?.firstOrNull()
                nameTv.text = "${artist?.firstName.orEmpty()} ${artist?.lastName.orEmpty()}"

                countTv.text = item.viewCount.toString()

                if (item.thumbnail != null) {
                    Glide.with(root.context)
                        .load(ApiClient.CDN_URL_QA + item.thumbnail.url)
                        .placeholder(R.drawable.rl_placeholder)
                        .error(R.drawable.rl_placeholder)
                        .transform(CenterCrop(), RoundedCorners(25))
                        .into(imgView)
                }
                setcontenttypeIcon(typeTv, typeIconView, item.contentType)
            }

            // --- BIND ITEM 1 ---
            bindSmallItem(
                1, relativeRledit2, tvRledtContTitle2, nameeditor1, count1,
                imgRledit1, itemTextRledit1, imgIconviewRledit1
            )

            // --- BIND ITEM 2 ---
            bindSmallItem(
                2, relativeRledit3, tvRledtContTitle3, nameeditor2, count2,
                imgRledit2, itemTextRledit2, imgIconviewRledit2
            )
        }
    }

    private fun setcontenttypeIcon(
        contentTypeRledit1: TextView,
        imgContenttypeRledit: ImageView,
        contentType: String?
    ) {
        when {
            "VIDEO".equals(contentType, ignoreCase = true) -> {
                imgContenttypeRledit.setImageResource(R.drawable.video_jump_back_in)
                contentTypeRledit1.text = "Video"
            }

            "AUDIO".equals(contentType, ignoreCase = true) -> {
                imgContenttypeRledit.setImageResource(R.drawable.audio_jump_back_in)
                contentTypeRledit1.text = "Audio"
            }

            "TEXT".equals(contentType, ignoreCase = true) -> {
                imgContenttypeRledit.setImageResource(R.drawable.text_jump_back_in)
                contentTypeRledit1.text = "Text"
            }

            else -> {
                imgContenttypeRledit.setImageResource(R.drawable.series_jump_back_in)
                contentTypeRledit1.text = "Series"
            }
        }
    }


    private fun setupWellnessContent(contentList: List<ContentWellness>?) {
        // 1. Guard Clause: Hide main layout if list is null or empty
        if (contentList.isNullOrEmpty()) {
            binding.rlWellnessMain.visibility = View.GONE
            return
        }

        with(binding) {
            rlWellnessMain.visibility = View.VISIBLE

            // 2. Local Helper Function to remove repeated code
            // This handles: Checking if item exists -> Binding it OR Hiding the view
            fun bindSlot(
                index: Int,
                container: View,
                headerTv: TextView,
                descTv: TextView,
                mainImg: ImageView,
                countTv: TextView,
                iconImg: ImageView,
                tagView: ImageView // Using View generic type as exact type of 'imgtagTv' is unsure
            ) {
                val item = contentList.getOrNull(index)
                if (item != null) {
                    container.visibility = View.VISIBLE
                    // Call your existing binding logic
                    bindContentToView(item, headerTv, descTv, mainImg, countTv, iconImg, tagView)
                } else {
                    container.visibility = View.GONE
                }
            }

            // 3. Bind the 4 slots cleanly
            bindSlot(0, relativeWellness1, tv1Header, tv1, img1, tv1Viewcount, img5, imgtagTv1)
            bindSlot(1, relativeWellness2, tv2Header, tv2, img2, tv2Viewcount, img6, imgtagTv2)
            bindSlot(2, relativeWellness3, tv3Header, tv3, img3, tv3Viewcount, img7, imgtagTv3)
            bindSlot(3, relativeWellness4, tv4Header, tv4, img4, tv4Viewcount, img8, imgtagTv4)
        }
    }

    //Bind Wellnes content
    private fun bindContentToView(
        content: ContentWellness,
        header: TextView,
        category: TextView,
        thumbnail: ImageView,
        viewcount: TextView,
        imgcontenttype: ImageView, // This was unused before!
        imgtag: ImageView
    ) {
        // 1. Set Text Data
        header.text = content.title
        viewcount.text = content.viewCount.toString() // Better than "" + ...
        category.text = content.categoryName

        // 2. Set Module Color
        setModuleColor(imgtag, content.moduleId)

        // 3. Set Content Type Icon (Play vs Read)
        // Assuming standard logic: VIDEO = Play icon, anything else = Read icon
        if (content.contentType.equals("VIDEO", ignoreCase = true)) {
            imgcontenttype.setImageResource(R.drawable.ic_playrledit)
        } else {
            imgcontenttype.setImageResource(R.drawable.read)
        }

        // 4. Safe Image Loading
        // Use 'thumbnail.context' -> safer than requireActivity() in helper functions
        val imageUrl = content.thumbnail?.url

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(thumbnail.context)
                .load(ApiClient.CDN_URL_QA + imageUrl)
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .transform(RoundedCorners(25))
                .into(thumbnail)
        } else {
            // Explicitly set placeholder if url is missing so recycled views don't show old images
            thumbnail.setImageResource(R.drawable.rl_placeholder)
        }
    }

    private fun setModuleColor(imgtag: ImageView, moduleId: String) {
        if (moduleId.equals("EAT_RIGHT", ignoreCase = true)) {
            val colorStateList = ContextCompat.getColorStateList(requireContext(), R.color.eatright)
            imgtag.imageTintList = colorStateList
        } else if (moduleId.equals("THINK_RIGHT", ignoreCase = true)) {
            val colorStateList =
                ContextCompat.getColorStateList(requireContext(), R.color.thinkright)
            imgtag.imageTintList = colorStateList
        } else if (moduleId.equals("SLEEP_RIGHT", ignoreCase = true)) {
            val colorStateList =
                ContextCompat.getColorStateList(requireContext(), R.color.sleepright)
            imgtag.imageTintList = colorStateList
        } else if (moduleId.equals("MOVE_RIGHT", ignoreCase = true)) {
            val colorStateList =
                ContextCompat.getColorStateList(requireContext(), R.color.moveright)
            imgtag.imageTintList = colorStateList
        }
    }

    private fun callRlEditDetailActivity(position: Int) {
        val contentType = rightLifeEditResponse?.data?.topList?.get(position)?.contentType
        val contentId = rightLifeEditResponse?.data?.topList?.get(position)?.id
        if (contentType.equals("TEXT", ignoreCase = true)) {
            startActivity(Intent(requireContext(), ArticlesDetailActivity::class.java).apply {
                putExtra("contentId", contentId)
            })
        } else if (contentType.equals("VIDEO", ignoreCase = true) || contentType
                .equals("AUDIO", ignoreCase = true)
        ) {
            startActivity(Intent(requireContext(), ContentDetailsActivity::class.java).apply {
                putExtra("contentId", contentId)
            })
        } else if (contentType.equals("SERIES", ignoreCase = true)) {
            startActivity(Intent(requireContext(), SeriesListActivity::class.java).apply {
                putExtra("contentId", contentId)
            })
        }
    }


    private fun callRlEditDetailActivity(item: CardItem) {
        // Assuming rightLifeEditResponse is accessible in this class
        var contentType: String? = null
        var contentId: String? = null
        contentType = item.selectedContentType
        contentId = item.seriesId

        if (contentType != null && contentType.equals("TEXT", ignoreCase = true)) {
            val intent = Intent(requireActivity(), ArticlesDetailActivity::class.java)
            intent.putExtra("contentId", contentId)
            startActivity(intent)
        } else if (contentType != null && (contentType.equals(
                "VIDEO",
                ignoreCase = true
            ) || contentType.equals("AUDIO", ignoreCase = true))
        ) {
            val intent = Intent(requireActivity(), ContentDetailsActivity::class.java)
            intent.putExtra("contentId", contentId)
            startActivity(intent)
        } else if (contentType != null && contentType.equals("SERIES", ignoreCase = true)) {
            val intent = Intent(requireActivity(), SeriesListActivity::class.java)
            intent.putExtra("contentId", contentId)
            startActivity(intent)
        }
    }

    private fun callWellnessDetailActivity(position: Int) {
        if (wellnessApiResponse != null) {
            val gson = Gson()
            val json = gson.toJson(wellnessApiResponse)
            val intent = Intent(requireContext(), SeriesListActivity::class.java)
            intent.putExtra("responseJson", json)
            intent.putExtra("position", position)
            intent.putExtra("contentId", wellnessApiResponse!!.data.contentList[position]._id)
            startActivity(intent)
        } else {
            // Handle null case
            Toast.makeText(requireContext(), "Response is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun callExploreModuleActivity(responseJson: SubModuleResponse) {
        val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
        intent.putExtra("moduleId", responseJson.data[0].moduleId)
        startActivity(intent)
    }

    private fun showInternetError() {
        Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
    }

    private fun getJumpBackInData() {
        val call: Call<ResponseBody> =
            apiService.getContinueData(
                sharedPreferenceManager.accessToken,
                "continue",
                10,
                0,
                "all"
            )
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>
            ) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonString = response.body()?.string()

                    val responseObj: ContentResponse =
                        gson.fromJson(jsonString, ContentResponse::class.java)

                    val contentDetails: List<ContentDetails>? = responseObj.data?.contentDetails

                    binding.rlJumpInBack.visibility =
                        if (contentDetails?.isEmpty() == true) View.GONE else View.VISIBLE

                    val adapter =
                        contentDetails?.let { ContentDetailsAdapter(requireContext(), it) }

                    // Horizontal LayoutManager
                    val layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    binding.recyclerViewJumpBackIn.layoutManager = layoutManager
                    binding.recyclerViewJumpBackIn.adapter = adapter

                } else {
                    Log.e("API_ERROR", "Response Code: " + response.code())
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })

    }


    private fun isFragmentSafe(): Boolean {
        return isAdded && activity != null && !requireActivity().isFinishing && !requireActivity().isDestroyed
    }

    private fun freeTrialDialogActivity(featureFlag: String = "") {
        val intent = Intent(requireActivity(), BeginMyFreeTrialActivity::class.java).apply {
            putExtra(FeatureFlags.EXTRA_ENTRY_DEST, featureFlag)
        }
        startActivity(intent)
    }


    // depplinking to detail pages
    fun deeplinkExploreModuleActivity() {
        callExploreModuleActivity(SleepRSubModuleResponse!!)
    }
}