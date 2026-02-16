package com.jetsynthesys.rightlife.newdashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseFragment
import com.jetsynthesys.rightlife.HealthConnectConstants
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.ui.MainAIActivity
import com.jetsynthesys.rightlife.databinding.FragmentHomeDashboardBinding
import com.jetsynthesys.rightlife.newdashboard.model.AiDashboardResponseMain
import com.jetsynthesys.rightlife.newdashboard.model.ChecklistResponse
import com.jetsynthesys.rightlife.newdashboard.model.DashboardChecklistManager
import com.jetsynthesys.rightlife.newdashboard.model.DashboardChecklistResponse
import com.jetsynthesys.rightlife.newdashboard.model.DiscoverDataItem
import com.jetsynthesys.rightlife.runWhenAttached
import com.jetsynthesys.rightlife.ui.ActivityUtils
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.DialogUtils
import com.jetsynthesys.rightlife.ui.healthcam.NewHealthCamReportActivity
import com.jetsynthesys.rightlife.ui.healthcam.basicdetails.HealthCamBasicDetailsNewActivity
import com.jetsynthesys.rightlife.ui.new_design.OnboardingQuestionnaireActivity
import com.jetsynthesys.rightlife.ui.scan_history.PastReportActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils
import com.jetsynthesys.rightlife.ui.utility.FeatureFlags
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeDashboardFragment : BaseFragment() {
    private var _binding: FragmentHomeDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var healthConnectClient: HealthConnectClient
    private var checkListCount = 0
    private var snapMealId: String = ""
    private var isWaitingForPermissionReturn = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeDashboardBinding.inflate(inflater, container, false)
        AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.HOME_DASHBOARD_VISIT)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshDashboardData()
        if (isWaitingForPermissionReturn) {
            checkHealthPermissionsAndSync()
        }
        (requireActivity() as? HomeNewActivity)?.hideChallengeLayout()
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupClickListeners()
        setupSwipeRefresh()
    }

    /**
     * Modern approach: Refresh all data concurrently using Coroutines.
     * Removes the need for manual delays.
     */
    private fun refreshDashboardData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Launching all requests concurrently
            launch { getAiDashboard() }
            launch { getDashboardChecklist() }
            launch { getDashboardChecklistStatus() }
        }
    }

    private fun checkHealthPermissionsAndSync() {
        val status = HealthConnectClient.getSdkStatus(requireContext())
        if (status == HealthConnectClient.SDK_AVAILABLE) {
            healthConnectClient = HealthConnectClient.getOrCreate(requireContext())

            lifecycleScope.launch {
                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                // If any permissions are granted now, mark it complete
                if (granted.isNotEmpty()) {
                    // Optional: Reset denial counter since they finally said yes
                    sharedPreferenceManager.setHealthPermissionDenialCount(0)
                    markHealthSyncChecklistCompleted()
                    isWaitingForPermissionReturn = false // Reset the flag
                }
            }
        }
    }

    /**
     * Setup static UI elements
     */
    private fun setupUI() {
        binding.tvDateDashboard.text = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
            .format(Calendar.getInstance().time)
    }

    /**
     * Centralized Click Listener Logic
     * Reduces boilerplate for subscription checks and debounce logic.
     */
    private fun setupClickListeners() {
        with(binding.includeChecklist) {
            rlChecklistEatright.setOnClickListener {
                it.handleAction { ActivityUtils.startEatRightQuestionnaireActivity(requireContext()) }
            }

            rlChecklistSleepright.setOnClickListener {
                it.handleAction { ActivityUtils.startThinkRightQuestionnaireActivity(requireContext()) }
            }

            rlChecklistSynchealth.setOnClickListener {
                it.handleAction { checkAndRequestHealthConnect() }
                AnalyticsLogger.logEvent(
                    requireContext(),
                    AnalyticsEvent.Checklist_SyncHealthConnect_Tap
                )
            }

            rlChecklistProfile.setOnClickListener {
                it.handleAction {
                    val intent = Intent(
                        requireContext(),
                        OnboardingQuestionnaireActivity::class.java
                    ).apply {
                        putExtra("forProfileChecklist", true)
                    }
                    startActivity(intent)
                }
            }

            rlChecklistSnapmeal.setOnClickListener {
                it.handleAction(FeatureFlags.MEAL_SCAN) {
                    val safeId = snapMealId.ifBlank { sharedPreferenceManager.snapMealId ?: "" }
                    logAndOpenMeal(safeId)
                }
            }

            rlChecklistFacescan.setOnClickListener {
                it.handleAction(FeatureFlags.FACE_SCAN) { handleFaceScanNavigation() }
            }

            imgQuestionmarkChecklist.setOnClickListener {
                it.disableViewForSeconds()
                DialogUtils.showWhyChecklistMattersDialog(requireContext(), "Here’s Why It Matters")
                AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.WHY_CHECKLIST_CLICK)
            }
            rlChecklistWhyThisDialog.setOnClickListener {
                it.disableViewForSeconds()
                AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.FINISH_TO_UNLOCK_CLICK)
                DialogUtils.showWhyChecklistMattersDialog(requireContext())
            }
        }

        // Main Dashboard Cards
        binding.cardThinkRight.setOnClickListener {
            handleCardNavigation(AnalyticsEvent.THINK_RIGHT_CLICK) {
                ActivityUtils.startThinkRightReportsActivity(
                    requireContext(),
                    "Not"
                )
            }
        }
        binding.cardEatRight.setOnClickListener {
            handleCardNavigation(AnalyticsEvent.EAT_RIGHT_CLICK) {
                ActivityUtils.startEatRightReportsActivity(
                    requireContext(),
                    "Not"
                )
            }
        }
        binding.cardMoveRight.setOnClickListener {
            handleCardNavigation(AnalyticsEvent.MOVE_RIGHT_CLICK) {
                ActivityUtils.startMoveRightReportsActivity(
                    requireContext(),
                    "Not"
                )
            }
        }
        binding.cardSleepRight.setOnClickListener {
            handleCardNavigation(AnalyticsEvent.SLEEP_RIGHT_CLICK) {
                ActivityUtils.startSleepRightReportsActivity(
                    requireContext(),
                    "Not"
                )
            }
        }

        binding.rlViewPastReports.setOnClickListener {
            it.disableViewForSeconds()
            startActivity(Intent(requireContext(), PastReportActivity::class.java))
        }
    }

    private fun setupSwipeRefresh() {
        activity?.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)?.apply {
            setOnRefreshListener {
                refreshDashboardData()
                isRefreshing = false
            }
        }
    }

    // --- Helper Extensions and Logic ---

    /**
     * Extension function to handle common checklist click logic:
     * 1. Debounce (disable for seconds)
     * 2. Subscription check
     * 3. Feature flag handling
     */
    private fun View.handleAction(feature: String = "", action: () -> Unit) {
        this.disableViewForSeconds()
        if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
            freeTrialDialogActivity(feature)
        } else {
            action()
        }
    }

    private fun handleCardNavigation(event: String, navigation: () -> Unit) {
        AnalyticsLogger.logEvent(requireContext(), event)
        if (checkTrailEndedAndShowDialog()) {
            navigation()
        }
    }

    private fun handleFaceScanNavigation() {
        val isFacialScanService = sharedPreferenceManager.userProfile.facialScanService ?: false
        if (DashboardChecklistManager.facialScanStatus) {
            startActivity(Intent(requireContext(), NewHealthCamReportActivity::class.java))
        } else if (isFacialScanService) {
            ActivityUtils.startFaceScanActivity(requireContext())
        } else {
            (requireActivity() as HomeNewActivity).showSwitchAccountDialog(requireContext(), "", "")
        }
    }

    private fun checkAndRequestHealthConnect() {
        if (HealthConnectClient.getSdkStatus(requireContext()) == HealthConnectClient.SDK_AVAILABLE) {
            healthConnectClient = HealthConnectClient.getOrCreate(requireContext())
            viewLifecycleOwner.lifecycleScope.launch { requestPermissionsAndReadAllData() }
        } else {
            installHealthConnect(requireContext())
        }
    }

    private suspend fun requestPermissionsAndReadAllData() {
        try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (HealthConnectConstants.allReadPermissions.all { it in granted }) {
                markHealthSyncChecklistCompleted()
            } else {
                requestPermissionsLauncher.launch(HealthConnectConstants.allReadPermissions)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Error checking permissions: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
            try {
                lifecycleScope.launch {
                    if (granted.containsAll(HealthConnectConstants.allReadPermissions)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT)
                                .show()
                        }
                        markHealthSyncChecklistCompleted()
                    } else if (granted.isNotEmpty()) {
                        // User granted some permissions. This is a success case.
                        Toast.makeText(requireContext(), "Permissions Granted", Toast.LENGTH_SHORT)
                            .show()

                        // Proceed to mark the checklist as completed and sync the data.
                        markHealthSyncChecklistCompleted()

                    } else {
                        // User explicitly denied ALL permissions. This is a failure case.
                        handlePermissionDenial()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private fun handlePermissionDenial() {
        val currentDenialCount = sharedPreferenceManager.healthPermissionDenialCount
        val newCount = currentDenialCount + 1
        sharedPreferenceManager.setHealthPermissionDenialCount(newCount)

        if (newCount >= 2) {
            // User has denied twice or more, show the permanent rationale
            showSettingsRationaleDialog()
        } else {
            Toast.makeText(
                requireContext(),
                "Permissions are required to sync your health data.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showSettingsRationaleDialog() {
        DialogUtils.showConfirmationDialog(
            context = requireContext(),
            title = "Sync Connection Required",
            message = "You have declined health permissions. To track your progress automatically, please enable them in the Health Connect settings.",
            positiveButtonText = "Open Settings",
            negativeButtonText = "Not Now",
            onPositiveClick = { openHealthConnectSettings() }
        )
    }

    private fun openHealthConnectSettings() {
        isWaitingForPermissionReturn = true // Set the flag here
        try {
            // Best practice: Try Health Connect specific settings first
            val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback: App Info settings
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
            }
            startActivity(intent)
        }
    }

    /**
     * ✅ Helper function to update checklist on server ONLY after permission confirmed.
     */
    private fun markHealthSyncChecklistCompleted() {
        lifecycleScope.launch {
            try {

                AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.HealthSync_Success)
                CommonAPICall.updateChecklistStatus(
                    requireContext(),
                    "sync_health_data",
                    AppConstants.CHECKLIST_COMPLETED
                ) { status ->
                    if (status) {
                        // 🔁 Refresh checklist UI
                        lifecycleScope.launch {
                            getDashboardChecklist()
                        }
                        Toast.makeText(
                            requireContext(),
                            "Health data sync marked as completed",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to update checklist on server",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error updating checklist", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun getDashboardChecklist() {
        // Make the API call
        val call = apiService.getDashboardChecklist(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    val promotionResponse2 = response.body()!!.string()
                    val gson = Gson()
                    val checklistResponse =
                        gson.fromJson(promotionResponse2, ChecklistResponse::class.java)
                    sharedPreferenceManager.saveChecklistResponse(checklistResponse)
                    runWhenAttached {
                        if (!isFragmentSafe()) return@runWhenAttached
                        handleChecklistResponse(checklistResponse)
                    }
                    checkListCount = 0
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t) // Print the full stack trace for more details
            }
        })
    }

    // A simple data helper to map API fields to UI components
    private data class ChecklistTask(
        val status: String?,
        val iconView: ImageView,
        val layout: View,
        val eventName: String,
        val shouldDisableOnClick: Boolean = true
    )

    private fun handleChecklistResponse(checklistResponse: ChecklistResponse?) {
        val data = checklistResponse?.data ?: return

        // 1. Reset completion counter
        checkListCount = 0

        // 2. Map tasks to their views and events
        val tasks = listOf(
            ChecklistTask(
                data.profile,
                binding.includeChecklist.imgCheck,
                binding.includeChecklist.rlChecklistProfile,
                AnalyticsEvent.PROFILE_STATUS
            ),
            ChecklistTask(
                data.meal_snap,
                binding.includeChecklist.imgCheckSnapmeal,
                binding.includeChecklist.rlChecklistSnapmeal,
                AnalyticsEvent.SNAP_MEAL_STATUS
            ),
            ChecklistTask(
                data.sync_health_data,
                binding.includeChecklist.imgCheckSynchealth,
                binding.includeChecklist.rlChecklistSynchealth,
                AnalyticsEvent.SYNC_DATA_STATUS
            ),
            ChecklistTask(
                data.vital_facial_scan,
                binding.includeChecklist.imgCheckFacescan,
                binding.includeChecklist.rlChecklistFacescan,
                AnalyticsEvent.FACIAL_SCAN_STATUS,
                false
            ),
            ChecklistTask(
                data.unlock_sleep,
                binding.includeChecklist.imgCheckSleepright,
                binding.includeChecklist.rlChecklistSleepright,
                AnalyticsEvent.TR_SR_ASSESSMENT_STATUS
            ),
            ChecklistTask(
                data.discover_eating,
                binding.includeChecklist.imgCheckEatright,
                binding.includeChecklist.rlChecklistEatright,
                AnalyticsEvent.ER_MR_ASSESSMENT_STATUS
            )
        )

        // 3. Process each task efficiently
        tasks.forEach { task ->
            updateTaskUI(task)
            AnalyticsLogger.logEvent(
                requireContext(),
                task.eventName,
                mapOf("status" to (task.status ?: "UNKNOWN"))
            )
        }

        // 4. Update Header Text (Modern string interpolation)
        binding.includeChecklist.tvChecklistNumber.text =
            getString(R.string.tasks_completed_format, checkListCount, 6)

        // 5. Visibility Logic (Simplified)
        val isComplete = DashboardChecklistManager.checklistStatus
        binding.llDashboardMainData.visibility = if (isComplete) View.VISIBLE else View.GONE
        binding.includeChecklist.llLayoutChecklist.visibility =
            if (isComplete) View.GONE else View.VISIBLE

        // 6. Log completion events only if needed
        handleFirstTimeEvents(isComplete)

        // 7. SnapMeal ID handling (Modern null-coalescing)
        val safeMealId = data.snap_mealId ?: ""
        this.snapMealId = safeMealId
        sharedPreferenceManager.saveSnapMealId(safeMealId)

        getDashboardChecklistStatus()
    }

    private fun updateTaskUI(task: ChecklistTask) {
        val (iconRes, isDone) = when (task.status) {
            "COMPLETED" -> R.drawable.ic_checklist_complete to true
            "INPROGRESS" -> R.drawable.ic_checklist_pending to false
            else -> R.drawable.ic_checklist_tick_bg to false
        }

        task.iconView.setImageResource(iconRes)

        if (isDone) {
            checkListCount++
            if (task.shouldDisableOnClick) {
                task.layout.setOnClickListener(null)
                task.layout.isClickable = false
            }
        }
    }

    private fun handleFirstTimeEvents(isComplete: Boolean) {
        if (isComplete && sharedPreferenceManager.firstTimeCheckListEventLogged) {
            sharedPreferenceManager.firstTimeCheckListEventLogged = false
            AnalyticsLogger.logEvent(
                requireContext(),
                AnalyticsEvent.CHECKLIST_COMPLETE,
                mapOf(AnalyticsParam.CHECKLIST_COMPLETE to true)
            )
        }

        if (sharedPreferenceManager.firstTimeForHomeDashboard) {
            sharedPreferenceManager.firstTimeForHomeDashboard = false
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.MyHealth_Dashboard_FirstOpen)
        }
    }

    // New api Requested by backend team to be added in app need to discuss with them
    private fun fetchDashboardData() {
        val userId = sharedPreferenceManager.userId ?: return

        val date = DateTimeUtils.formatDateForOneApi()

        val call = apiServiceFastApi.getLandingDashboardData(userId, date, "android", true)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val jsonString = response.body()!!.string()
                    try {
                        Log.d("DashboardRaw", jsonString)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    // showToast("Failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    // get AI Dashboard Data
    private fun getAiDashboard() {
        // Make the API call
        val date = DateTimeUtils.formatDateForOneApi()
        val call = apiService.getAiDashboard(sharedPreferenceManager.accessToken, date)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    val promotionResponse2 = response.body()!!.string()

                    val gson = Gson()

                    val aiDashboardResponseMain =
                        gson.fromJson(promotionResponse2, AiDashboardResponseMain::class.java)
                    runWhenAttached {
                        //handleSelectedModule(aiDashboardResponseMain)
                        if (!isFragmentSafe()) return@runWhenAttached
                        handleDescoverList(aiDashboardResponseMain)
                        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                        binding.recyclerView.adapter = HeartRateAdapter(
                            aiDashboardResponseMain.data?.facialScan,
                            requireContext()
                        )
                    }
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun getDashboardChecklistStatus() {
        apiService.getdashboardChecklistStatus(sharedPreferenceManager.accessToken)
            .enqueue(object : Callback<DashboardChecklistResponse> {
                override fun onResponse(
                    call: Call<DashboardChecklistResponse>,
                    response: Response<DashboardChecklistResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.data?.let {
                            DashboardChecklistManager.updateFrom(it)
                        }
                        runWhenAttached {
                            if (!isFragmentSafe()) return@runWhenAttached
                            if (DashboardChecklistManager.isDataLoaded) {
                                // Chceklist completion logic
                                if (DashboardChecklistManager.checklistStatus) {
                                    binding.llDashboardMainData.visibility = View.VISIBLE
                                    binding.includeChecklist.llLayoutChecklist.visibility =
                                        View.GONE
                                    binding.llDiscoverLayout.visibility = View.GONE
                                } else {
                                    binding.llDashboardMainData.visibility = View.GONE
                                    binding.includeChecklist.llLayoutChecklist.visibility =
                                        View.VISIBLE
                                    binding.llDiscoverLayout.visibility = View.VISIBLE

                                    // first time Check list visit event
                                    if (sharedPreferenceManager.firstTimeCheckListVisitLogged) {
                                        sharedPreferenceManager.setFirstTimeCheckListVisitLogged(
                                            false
                                        )
                                        AnalyticsLogger.logEvent(
                                            requireContext(),
                                            AnalyticsEvent.Checklist_FirstTime_Open
                                        )
                                    }

                                }/*(requireActivity() as? HomeNewActivity)?.showSubsribeLayout(
                                                 DashboardChecklistManager.paymentStatus
                                             )*/
                                (requireActivity() as? HomeNewActivity)?.getUserDetails()
                                setChecklistRowArrow(DashboardChecklistManager.paymentStatus)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Server Error: " + response.code(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<DashboardChecklistResponse>, t: Throwable) {
                    handleNoInternetView(t)
                }

            })
    }

    private fun handleDescoverList(aiDashboardResponseMain: AiDashboardResponseMain?) {
        if (aiDashboardResponseMain?.data?.discoverData != null) {
            setHealthNoDataCardAdapter(aiDashboardResponseMain.data.discoverData)
        } else {
            binding.llDiscoverLayout.visibility = View.GONE
        }
    }

    private fun setHealthNoDataCardAdapter(discoverData: List<DiscoverDataItem>?) {
        binding.healthCardRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = HealthCardAdapter(discoverData) { item ->
                if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
                    freeTrialDialogActivity()
                } else {
                    val intent = if (DashboardChecklistManager.facialScanStatus) {
                        Intent(requireContext(), NewHealthCamReportActivity::class.java)
                    } else {
                        Intent(requireContext(), HealthCamBasicDetailsNewActivity::class.java)
                    }
                    startActivity(intent)
                }

                AnalyticsLogger.logEvent(
                    requireContext(),
                    AnalyticsEvent.DISCOVER_CLICK,
                    mapOf(AnalyticsParam.DISCOVER_TYPE to item?.parameter!!)
                )
            }
            isNestedScrollingEnabled = false // 👈 important for smooth scroll
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        if (discoverData.isNullOrEmpty()) {
            binding.llDiscoverLayout.visibility = View.GONE
        } else {
            binding.llDiscoverLayout.visibility = View.VISIBLE
        }
    }

    private fun checkTrailEndedAndShowDialog(): Boolean {
        return if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
            freeTrialDialogActivity()
            false // Return false if condition is true and dialog is shown
        } else {
            if (!DashboardChecklistManager.checklistStatus) {
                AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.FINISH_TO_UNLOCK_CLICK)
                DialogUtils.showCheckListQuestionCommonDialog(requireContext())
                false
            } else if (sharedPreferenceManager.userProfile?.user_sub_status == 2) {
                (requireActivity() as HomeNewActivity).showTrailEndedBottomSheet()
                false // Return false if condition is true and dialog is shown
            } else if (sharedPreferenceManager.userProfile?.user_sub_status == 3) {
                (requireActivity() as HomeNewActivity).showSubsciptionEndedBottomSheet()
                false // Return false if condition is true and dialog is shown
            } else {
                true
            }
        }
        return true
    }


    private fun installHealthConnect(context: Context) {
        val uri =
            "https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setChecklistRowArrow(isAccessible: Boolean) {
        if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
            binding.includeChecklist.imgChecklistProfileArrow.setImageResource(R.drawable.checklist_lock)
            binding.includeChecklist.imgChecklistSleepRightArrow.setImageResource(R.drawable.checklist_lock)
            binding.includeChecklist.imgChecklistSnapMealArrow.setImageResource(R.drawable.checklist_lock)
            binding.includeChecklist.imgChecklistEatRightArrow.setImageResource(R.drawable.checklist_lock)
            binding.includeChecklist.imgChecklistSyncDataArrow.setImageResource(R.drawable.checklist_lock)
            binding.includeChecklist.imgChecklistFaceScanArrow.setImageResource(R.drawable.checklist_lock)
        } else {
            binding.includeChecklist.imgChecklistProfileArrow.setImageResource(R.drawable.ic_checklist_smallarrow)
            binding.includeChecklist.imgChecklistSleepRightArrow.setImageResource(R.drawable.ic_checklist_smallarrow)
            binding.includeChecklist.imgChecklistSnapMealArrow.setImageResource(R.drawable.ic_checklist_smallarrow)
            binding.includeChecklist.imgChecklistEatRightArrow.setImageResource(R.drawable.ic_checklist_smallarrow)
            binding.includeChecklist.imgChecklistSyncDataArrow.setImageResource(R.drawable.ic_checklist_smallarrow)
            binding.includeChecklist.imgChecklistFaceScanArrow.setImageResource(R.drawable.ic_checklist_smallarrow)
        }
    }

    private fun freeTrialDialogActivity(featureFlag: String = "") {
        val intent = Intent(requireActivity(), BeginMyFreeTrialActivity::class.java).apply {
            putExtra(FeatureFlags.EXTRA_ENTRY_DEST, featureFlag)
        }
        startActivity(intent)
    }


    private fun logAndOpenMeal(snapId: String) {
        AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.EOS_SNAP_MEAL_CLICK)
        //ActivityUtils.startEatRightReportsActivity(requireContext(), "SnapMealTypeEat", snapId)
        startActivity(Intent(context, MainAIActivity::class.java).apply {
            putExtra("ModuleName", "EatRight")
            putExtra("BottomSeatName", "SnapMealTypeEat")
            putExtra("snapMealId", snapId)
        })
    }

    private fun isFragmentSafe(): Boolean {
        return isAdded && activity != null && !requireActivity().isFinishing && !requireActivity().isDestroyed
    }

}