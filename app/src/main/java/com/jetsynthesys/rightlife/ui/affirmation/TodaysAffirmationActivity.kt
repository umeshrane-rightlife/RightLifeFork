package com.jetsynthesys.rightlife.ui.affirmation

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityTodaysAffirmationBinding
import com.jetsynthesys.rightlife.databinding.LayoutDiscardBottomsheetBinding
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.ui.BalloonAlignment
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.CommonResponse
import com.jetsynthesys.rightlife.ui.affirmation.adapter.AffirmationCardPagerAdapter
import com.jetsynthesys.rightlife.ui.affirmation.pojo.AffirmationCategoryData
import com.jetsynthesys.rightlife.ui.affirmation.pojo.AffirmationCategoryListResponse
import com.jetsynthesys.rightlife.ui.affirmation.pojo.AffirmationSelectedCategoryData
import com.jetsynthesys.rightlife.ui.affirmation.pojo.AffirmationSelectedCategoryResponse
import com.jetsynthesys.rightlife.ui.affirmation.pojo.CreateAffirmationPlaylistRequest
import com.jetsynthesys.rightlife.ui.affirmation.pojo.GetAffirmationPlaylistResponse
import com.jetsynthesys.rightlife.ui.showBalloon
import com.jetsynthesys.rightlife.ui.showBalloonWithDim
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.math.abs


class TodaysAffirmationActivity : BaseActivity() {

    private lateinit var binding: ActivityTodaysAffirmationBinding
    private lateinit var recyclerViewCategory: RecyclerView
    private lateinit var categoryBottomSheetDialog: BottomSheetDialog
    private lateinit var closeBottomSheetDialog: BottomSheetDialog
    private lateinit var discardBottomSheetDialog: BottomSheetDialog
    private val affirmationPlaylist: ArrayList<AffirmationSelectedCategoryData> = ArrayList()
    private val affirmationPlaylistRequest: ArrayList<String> = ArrayList()
    private var selectedCategoryPosition = 0
    private val categoryList: ArrayList<AffirmationCategoryData> = ArrayList()

    private val affirmationList: ArrayList<AffirmationSelectedCategoryData> = ArrayList()
    private lateinit var affirmationCardPagerAdapter: AffirmationCardPagerAdapter
    private var startDate = ""

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodaysAffirmationBinding.inflate(layoutInflater)
        setChildContentView(binding.getRoot())

        startDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

        setupCategoryBottomSheet()
        setupCloseBottomSheet()
        setupDiscardBottomSheet()
        getAffirmationPlaylist()
        getCategoryList()

        onBackPressedDispatcher.addCallback(this) {
            callPostMindFullDataAPI()
            finish()
        }

        /*if (sharedPreferenceManager.firstTimeUserAffirmationInfoShown)
            showInfoDialog()*/

        binding.llCategorySelection.setOnClickListener {
            categoryBottomSheetDialog.show()
        }

        binding.infoAffirmation.setOnClickListener {
            it.disableViewForSeconds()
            showInfoDialog()
        }

        binding.shareAffirmation.setOnClickListener {
            it.disableViewForSeconds()
            val bitmap =
                getBitmapFromView(affirmationCardPagerAdapter.getViewAt(binding.cardViewPager.currentItem)!!)
            val imageUri = saveBitmapToCache(bitmap)
            imageUri?.let {
                shareImage(it)
            }
        }

        binding.ivClose.setOnClickListener {
            it.disableViewForSeconds()
            if (sharedPreferenceManager.firstTimeUserForAffirmation)
                closeBottomSheetDialog.show()
            else if (affirmationPlaylistRequest.isNotEmpty())
                discardBottomSheetDialog.show()
            else {
                callPostMindFullDataAPI()
                finish()
            }
        }

        binding.addAffirmation.setOnClickListener {
            it.disableViewForSeconds()
            val position = binding.cardViewPager.currentItem
            if (checkAffirmationIsAlreadyAdded(position)) {
                if (affirmationPlaylist.size == 3)
                    showDeleteAffirmationPlaylistDialog()
                else {
                    if (checkAffirmationAddedLocally(position)) {
                        removeAffirmationLocally(position)
                    } else {
                        if (affirmationList.isNotEmpty())
                            affirmationList[binding.cardViewPager.currentItem].id?.let { it1 ->
                                removeFromPlaylist(
                                    it1, position
                                )
                            }
                    }
                }
            } else {
                if (affirmationList.isNotEmpty()) {
                    AnalyticsLogger.logEvent(
                        this,
                        AnalyticsEvent.AFFIRMATION_ADDED,
                        mapOf(
                            AnalyticsParam.AFFIRMATION_ID to affirmationList[binding.cardViewPager.currentItem].id!!,
                            AnalyticsParam.AFFIRMATION_TYPE to affirmationList[binding.cardViewPager.currentItem].categoryName!!
                        )
                    )
                    addCardToPlaylist()
                }
            }
        }

        binding.btnCreateAffirmation.setOnClickListener {
            it.disableViewForSeconds()
            if (affirmationPlaylistRequest.isNotEmpty()) {
                createAffirmationPlaylist()
            }
        }

        setSelectedCategoryAdapter(affirmationList)

        val gestureDetector =
            GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 == null || e2 == null) return false

                    val deltaX = e2.x - e1.x
                    val deltaY = e2.y - e1.y

                    // Determine if it's a vertical or horizontal swipe
                    // If horizontal movement is greater than vertical, let ViewPager handle it
                    if (abs(deltaX) > abs(deltaY)) {
                        // Horizontal swipe - let ViewPager handle it
                        return false
                    }

                    // Vertical swipe - handle category change
                    if (abs(deltaY) > 150) {
                        if (deltaY < 0) {
                            onSwipeUp()
                        } else {
                            onSwipeDown()
                        }
                        return true
                    }
                    return false
                }
            })

        binding.cardViewPager.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            // Return false to allow ViewPager to also handle the touch for horizontal scrolling
            false
        }

        AnalyticsLogger.logEvent(
            this,
            AnalyticsEvent.TR_AffirmationPageGen_Open,
            mapOf(AnalyticsParam.TIMESTAMP to System.currentTimeMillis())
        )
    }

    private fun checkAffirmationIsAlreadyAdded(position: Int): Boolean {
        for (playlist in affirmationPlaylist) {
            if (playlist.id == affirmationList[position].id) {
                return true
            }
        }
        return false
    }

    private fun checkAffirmationAddedLocally(position: Int): Boolean {
        for (playlist in affirmationPlaylistRequest) {
            if (playlist == affirmationList[position].id) {
                return true
            }
        }
        return false
    }

    private fun removeAffirmationLocally(position: Int) {
        for (playlist in affirmationPlaylistRequest) {
            if (playlist == affirmationList[position].id) {
                affirmationPlaylistRequest.remove(playlist)
                binding.addAffirmation.setImageResource(R.drawable.add_affirmation)
                break
            }
        }
        for (playlist in affirmationPlaylist) {
            if (playlist.id == affirmationList[position].id) {
                affirmationPlaylist.remove(playlist)
                binding.addAffirmation.setImageResource(R.drawable.add_affirmation)
                break
            }
        }
    }

    private fun onSwipeUp() {
        if (selectedCategoryPosition < categoryList.size - 1) {
            // Animate the entire ViewPager, not just one child
            binding.cardViewPager.animate()
                ?.translationYBy(-(binding.cardViewPager.height.toFloat() + 100))
                ?.setDuration(300)
                ?.withEndAction {
                    selectedCategoryPosition += 1

                    // Bounds check to prevent crash
                    if (selectedCategoryPosition >= categoryList.size) {
                        selectedCategoryPosition = categoryList.size - 1
                        return@withEndAction
                    }

                    getSelectedCategoryData(categoryList[selectedCategoryPosition].id)
                    binding.tvCategory.text = categoryList[selectedCategoryPosition].title

                    // Reset ViewPager position below screen
                    binding.cardViewPager.translationY =
                        binding.cardViewPager.height.toFloat() + 100

                    // Animate back to original position
                    binding.cardViewPager.animate()
                        .translationY(0f)
                        .setDuration(300)
                        .start()
                }
                ?.start()
        }
    }

    private fun onSwipeDown() {
        if (selectedCategoryPosition > 0) {
            // Animate the entire ViewPager, not just one child
            binding.cardViewPager.animate()
                ?.translationYBy(binding.cardViewPager.height.toFloat() + 100)
                ?.setDuration(300)
                ?.withEndAction {
                    selectedCategoryPosition -= 1

                    // Bounds check to prevent crash
                    if (selectedCategoryPosition < 0) {
                        selectedCategoryPosition = 0
                        return@withEndAction
                    }

                    getSelectedCategoryData(categoryList[selectedCategoryPosition].id)
                    binding.tvCategory.text = categoryList[selectedCategoryPosition].title

                    // Reset ViewPager position above screen
                    binding.cardViewPager.translationY =
                        -(binding.cardViewPager.height.toFloat() + 100)

                    // Animate back to original position
                    binding.cardViewPager.animate()
                        .translationY(0f)
                        .setDuration(300)
                        .start()
                }
                ?.start()
        }
    }
    /*    private fun onSwipeUp() {
            if (selectedCategoryPosition < categoryList.size - 1) {
                val currentPage = binding.cardViewPager.getChildAt(0) // current visible page view

                currentPage?.animate()
                    ?.translationYBy(-(currentPage.height.toFloat() + 100))
                    ?.setDuration(300)
                    ?.withEndAction {
                        selectedCategoryPosition += 1
                        getSelectedCategoryData(categoryList[selectedCategoryPosition].id)
                        binding.tvCategory.text = categoryList[selectedCategoryPosition].title

                        // Reset below screen
                        currentPage.translationY = currentPage.height.toFloat() + 100

                        currentPage.animate()
                            .translationY(0f)
                            .setDuration(300)
                            .start()
                    }
                    ?.start()
            }
        }

        private fun onSwipeDown() {
            if (selectedCategoryPosition > 0) {
                val currentPage = binding.cardViewPager.getChildAt(0) // current visible page view

                currentPage?.animate()
                    ?.translationYBy(currentPage.height.toFloat() + 100)
                    ?.setDuration(300)
                    ?.withEndAction {
                        selectedCategoryPosition -= 1
                        getSelectedCategoryData(categoryList[selectedCategoryPosition].id)
                        binding.tvCategory.text = categoryList[selectedCategoryPosition].title

                        // Reset above screen
                        currentPage.translationY = -(currentPage.height.toFloat() + 100)

                        currentPage.animate()
                            .translationY(0f)
                            .setDuration(300)
                            .start()
                    }
                    ?.start()
            }
        }*/


    private fun addCardToPlaylist() {
        val yOff = -200
        val xOff = 10
        affirmationPlaylist.add(affirmationList[binding.cardViewPager.currentItem])
        binding.addAffirmation.setImageResource(R.drawable.playlist_added)
        affirmationList[binding.cardViewPager.currentItem].id?.let {
            affirmationPlaylistRequest.add(
                it
            )
        }
        if (affirmationPlaylist.size >= 3)
            binding.btnCreateAffirmation.isEnabled = true

        if (sharedPreferenceManager.firstTimeUserForAffirmation) {
            when (affirmationPlaylist.size) {
                1 -> {
                    //Toast.makeText(this, "Great choice, keep going.", Toast.LENGTH_SHORT).show()
                    /*showBalloon(
                        binding.addAffirmation, "Great choice, keep going.",
                        BalloonAlignment.BOTTOM,0.5f, xOff = 10, yOff = -100
                    )*/
                    showBalloonWithDim(
                        binding.addAffirmation, "Great choice, keep going.", "AffirmationAddButton",
                        BalloonAlignment.BOTTOM, 0.5f, xOff = 10, yOff = -100
                    )
                }

                2 -> {
                    /*Toast.makeText(
                        this,
                        "One more and your playlist is ready to go.",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    showBalloon(
                        binding.addAffirmation,
                        "One more and your playlist is ready to go.", xOff = xOff, yOff = yOff
                    )
                }

                3 -> {
                    //Toast.makeText(this, "Playlist Unlocked!", Toast.LENGTH_SHORT).show()
                    showBalloon(
                        binding.addAffirmation,
                        "Playlist Unlocked!",
                        xOff = xOff,
                        yOff = yOff
                    )
                }

                else -> {
                    /*Toast.makeText(
                        this,
                        "${affirmationPlaylist.size} Affirmation Added!",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    showBalloon(
                        binding.addAffirmation,
                        "${affirmationPlaylist.size} Affirmation Added!", xOff = xOff, yOff = yOff
                    )
                }
            }
        } else {
            /*Toast.makeText(
                this,
                "${affirmationPlaylistRequest.size} Affirmation Added!",
                Toast.LENGTH_SHORT
            ).show()*/
            showBalloon(
                binding.addAffirmation,
                "${affirmationPlaylistRequest.size} Affirmation Added!", xOff = xOff, yOff = yOff
            )
        }
    }

    private fun setSelectedCategoryAdapter(affirmationList: ArrayList<AffirmationSelectedCategoryData>) {
        affirmationCardPagerAdapter =
            AffirmationCardPagerAdapter(affirmationList, this, binding.cardViewPager)
        binding.cardViewPager.setPageTransformer(true, AffirmationPageTransformer())
        binding.cardViewPager.adapter = affirmationCardPagerAdapter
        if (affirmationList.isNotEmpty())
            updateAddButtonImage(0)
        binding.cardViewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                updateAddButtonImage(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    private fun updateAddButtonImage(position: Int) {
        var flag = false
        for (playlist in affirmationPlaylist) {
            if (playlist.id == affirmationList[position].id) {
                flag = true
            }
        }
        if (flag) {
            binding.addAffirmation.setImageResource(R.drawable.playlist_added)
        } else {
            binding.addAffirmation.setImageResource(R.drawable.add_affirmation)
        }
    }

    private fun setupCategoryBottomSheet() {
        // Create and configure BottomSheetDialog
        categoryBottomSheetDialog = BottomSheetDialog(this)

        categoryBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // Optional: prevent it from collapsing when user scrolls
        categoryBottomSheetDialog.behavior.skipCollapsed = true
        categoryBottomSheetDialog.behavior.isDraggable = false

        // Inflate the BottomSheet layout
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)

        // Set up RecyclerView in the BottomSheet
        recyclerViewCategory = bottomSheetView.findViewById(R.id.recyclerView)
        recyclerViewCategory.setLayoutManager(LinearLayoutManager(this))
        //recyclerViewCategory.setAdapter(myAdapter)

        categoryBottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }
        bottomSheetView.post {
            val bottomSheet =
                categoryBottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                it.requestLayout()
            }
        }
        val ivClose = bottomSheetView.findViewById<ImageView>(R.id.ivClose)
        ivClose.setOnClickListener {
            categoryBottomSheetDialog.dismiss()
        }

    }

    private fun showDeleteAffirmationPlaylistDialog() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate using ViewBinding
        val binding = LayoutDiscardBottomsheetBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(binding.root)

        // Optional animation on root layout (you may need to use another ID if this isn't correct)
        val slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
        binding.root.startAnimation(slideUpAnimation)

        binding.tvHeader.text = "Delete Playlist?"
        binding.tvDescription.text = "Do you want to delete Affirmation Playlist?"

        // Set up button listeners
        binding.btnNo.setOnClickListener {
            affirmationPlaylist[0].id?.let { removeFromPlaylist(it) }
            bottomSheetDialog.dismiss()
            callPostMindFullDataAPI()
            finish()
        }
        binding.btnYes.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun setupDiscardBottomSheet() {
        // Create and configure BottomSheetDialog
        discardBottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val bottomSheetView = layoutInflater.inflate(R.layout.layout_discard_bottomsheet, null)


        discardBottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        bottomSheetView.findViewById<Button>(R.id.btnNo).setOnClickListener {
            callPostMindFullDataAPI()
            finish()
        }
        bottomSheetView.findViewById<Button>(R.id.btnYes).setOnClickListener {
            discardBottomSheetDialog.dismiss()
        }

    }

    private fun setupCloseBottomSheet() {
        // Create and configure BottomSheetDialog
        closeBottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val bottomSheetView = layoutInflater.inflate(R.layout.layout_close_bottomsheet, null)


        closeBottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        bottomSheetView.findViewById<Button>(R.id.btnGoBackAndSave3).setOnClickListener {
            closeBottomSheetDialog.dismiss()
        }
        bottomSheetView.findViewById<Button>(R.id.btnQuitAnyway).setOnClickListener {
            callPostMindFullDataAPI()
            finish()
        }

    }

    private fun setupCategoryAdapter() {
        val affirmationCategoryListAdapter = AffirmationCategoryListAdapter(
            this, categoryList
        ) { category ->
            selectedCategoryPosition = categoryList.indexOf(category)
            getSelectedCategoryData(category.id)
            if (categoryBottomSheetDialog.isShowing)
                categoryBottomSheetDialog.dismiss()
            binding.tvCategory.text = category.title
        }
        recyclerViewCategory.adapter = affirmationCategoryListAdapter
    }

    private fun getCategoryList() {
        val call = apiService.getAffirmationCategoryList(sharedPreferenceManager.accessToken)

        call.enqueue(object : Callback<AffirmationCategoryListResponse> {
            override fun onResponse(
                call: Call<AffirmationCategoryListResponse>,
                response: Response<AffirmationCategoryListResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.data?.let {
                        categoryList.addAll(it)
                        setupCategoryAdapter()
                        if (it.isNotEmpty())
                            getSelectedCategoryData(it[0].id)
                    }
                } else {
                    Toast.makeText(
                        this@TodaysAffirmationActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AffirmationCategoryListResponse>, t: Throwable) {
                handleNoInternetView(t)
            }

        })
    }

    private fun getSelectedCategoryData(id: String?) {
        Utils.showLoader(this)
        val call =
            apiService.getAffirmationSelectedCategoryData(sharedPreferenceManager.accessToken, id)

        call.enqueue(object : Callback<AffirmationSelectedCategoryResponse> {
            override fun onResponse(
                call: Call<AffirmationSelectedCategoryResponse>,
                response: Response<AffirmationSelectedCategoryResponse>
            ) {
                Utils.dismissLoader(this@TodaysAffirmationActivity)
                if (response.isSuccessful && response.body() != null) {
                    affirmationList.clear()
                    response.body()?.data?.let { affirmationList.addAll(it) }
                    setSelectedCategoryAdapter(affirmationList)
                } else {
                    Toast.makeText(
                        this@TodaysAffirmationActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AffirmationSelectedCategoryResponse>, t: Throwable) {
                Utils.dismissLoader(this@TodaysAffirmationActivity)
                handleNoInternetView(t)
            }

        })
    }

    private fun getAffirmationPlaylist() {
        Utils.showLoader(this)
        val call = apiService.getAffirmationPlaylist(sharedPreferenceManager.accessToken)

        call.enqueue(object : Callback<GetAffirmationPlaylistResponse> {
            override fun onResponse(
                call: Call<GetAffirmationPlaylistResponse>,
                response: Response<GetAffirmationPlaylistResponse>
            ) {
                Utils.dismissLoader(this@TodaysAffirmationActivity)
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.data?.let { affirmationPlaylist.addAll(it) }
                    if (affirmationPlaylist.isNotEmpty()) {
                        binding.btnCreateAffirmation.text = "Save"
                        binding.btnCreateAffirmation.isEnabled = true
                    } else {
                        showInfoDialog()
                    }
                } else {
                    Toast.makeText(
                        this@TodaysAffirmationActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<GetAffirmationPlaylistResponse>, t: Throwable) {
                Utils.dismissLoader(this@TodaysAffirmationActivity)
                handleNoInternetView(t)
            }

        })
    }

    private fun removeFromPlaylist(songId: String, position: Int = -1) {
        Utils.showLoader(this)
        val call =
            apiService.removeFromAffirmationPlaylist(sharedPreferenceManager.accessToken, songId)

        call.enqueue(object : Callback<CommonResponse> {
            override fun onResponse(
                call: Call<CommonResponse>,
                response: Response<CommonResponse>
            ) {
                Utils.dismissLoader(this@TodaysAffirmationActivity)
                if (response.isSuccessful && response.body() != null) {
                    if (position != -1)
                        removeAffirmationLocally(position)
                    else {
                        if (affirmationPlaylist.isNotEmpty()) {
                            affirmationPlaylist.removeAt(0)
                            if (affirmationPlaylist.size >= 1)
                                affirmationPlaylist[0].id?.let { removeFromPlaylist(it) }
                        }
                    }
                    showCustomToast("Affirmation removed from Playlist!")
                } else {
                    showToast("try again!: ${response.code()}")
                }

            }

            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                //Utils.dismissLoader(this@NewSleepSoundActivity)
                showToast("Network Error: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun createAffirmationPlaylist() {
        Utils.showLoader(this)

        val createAffirmationPlaylistRequest = CreateAffirmationPlaylistRequest()
        createAffirmationPlaylistRequest.list?.addAll(affirmationPlaylistRequest)

        val call = apiService.createAffirmationPlaylist(
            sharedPreferenceManager.accessToken,
            createAffirmationPlaylistRequest
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Utils.dismissLoader(this@TodaysAffirmationActivity)
                if (response.isSuccessful && response.body() != null) {

                    AnalyticsLogger.logEvent(
                        this@TodaysAffirmationActivity,
                        AnalyticsEvent.AFFIRMATION_PLAYLIST_CREATED,
                        mapOf(
                            AnalyticsParam.AFFIRMATION_PLAYLIST_ID to sharedPreferenceManager.userId,
                        )
                    )

                    Log.e(
                        "firstaffirmation",
                        "onResponse: bool " + sharedPreferenceManager.firstTimeUserPlaylistAffirmation
                    )
                    if (sharedPreferenceManager.firstTimeUserPlaylistAffirmation) {
                        CommonAPICall.postWellnessStreak(
                            this@TodaysAffirmationActivity,
                            "Affirmation"
                        )
                        showCreatedUpdatedDialog("Playlist Created")
                        sharedPreferenceManager.firstTimeUserPlaylistAffirmation = false
                        sharedPreferenceManager.firstTimeUserForAffirmation = false
                    } else {
                        showCreatedUpdatedDialog("Changes Saved")
                    }
                } else {
                    Toast.makeText(
                        this@TodaysAffirmationActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Utils.dismissLoader(this@TodaysAffirmationActivity)
                Toast.makeText(
                    this@TodaysAffirmationActivity,
                    "Network Error: " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun showInfoDialog() {

        // Create the dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_info_affirmation)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val window = dialog.window

        // Set the dim amount
        val layoutParams = window?.attributes
        layoutParams?.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window?.attributes = layoutParams

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        layoutParams?.width = width
        val card = dialog.findViewById<View>(R.id.dialog_root_layout)
        card.setOnClickListener { dialog.dismiss() }

        dialog.setOnCancelListener {
            if (sharedPreferenceManager.firstTimeUserForAffirmation)
                showBalloonWithDim(
                    binding.btnCreateAffirmation,
                    "Select at least 3 affirmations to build your personal affirmation playlist!",
                    "AffirmationCreateButton", xOff = -200, yOff = 20
                ) {
                    showBalloonWithDim(
                        binding.addAffirmation,
                        "Save to your Playlist",
                        "AffirmationAddButton",
                        BalloonAlignment.BOTTOM,
                        0.5f,
                        xOff = 10,
                        yOff = -100
                    )
                }
        }
        dialog.show()
        //sharedPreferenceManager.firstTimeUserAffirmationInfoShown = false
    }

    private fun showCreatedUpdatedDialog(message: String) {

        // Create the dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_playlist_created)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val window = dialog.window

        // Set the dim amount
        val layoutParams = window?.attributes
        layoutParams?.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window?.attributes = layoutParams

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        layoutParams?.width = width

        dialog.findViewById<TextView>(R.id.tvDialogPlaylistCreated).text = message

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            callPostMindFullDataAPI()
            finish()
        }, 1000)
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        val cachePath = File(cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "shared_image.png")

        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider", // Must match the authority in your manifest
            file
        )
    }

    private fun shareImage(imageUri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun callPostMindFullDataAPI() {
        val endDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        CommonAPICall.postMindFullData(this, "Affirmation", startDate, endDate)
    }


}