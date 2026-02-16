package com.jetsynthesys.rightlife.newdashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ComponentCaller
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.BuildConfig
import com.jetsynthesys.rightlife.HealthConnectConstants
import com.jetsynthesys.rightlife.MainApplication
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.SyncManager
import com.jetsynthesys.rightlife.ai_package.PermissionManager
import com.jetsynthesys.rightlife.ai_package.model.BloodPressure
import com.jetsynthesys.rightlife.ai_package.model.BodyFatPercentage
import com.jetsynthesys.rightlife.ai_package.model.BodyMass
import com.jetsynthesys.rightlife.ai_package.model.Distance
import com.jetsynthesys.rightlife.ai_package.model.EnergyBurnedRequest
import com.jetsynthesys.rightlife.ai_package.model.HeartRateRequest
import com.jetsynthesys.rightlife.ai_package.model.HeartRateVariabilityRequest
import com.jetsynthesys.rightlife.ai_package.model.OxygenSaturation
import com.jetsynthesys.rightlife.ai_package.model.RespiratoryRate
import com.jetsynthesys.rightlife.ai_package.model.SleepStageJson
import com.jetsynthesys.rightlife.ai_package.model.StepCountRequest
import com.jetsynthesys.rightlife.ai_package.model.StoreHealthDataRequest
import com.jetsynthesys.rightlife.ai_package.model.WorkoutRequest
import com.jetsynthesys.rightlife.ai_package.ui.MainAIActivity
import com.jetsynthesys.rightlife.apimodel.appconfig.AppConfigResponse
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse
import com.jetsynthesys.rightlife.databinding.ActivityHomeNewBinding
import com.jetsynthesys.rightlife.databinding.DialogForceUpdateBinding
import com.jetsynthesys.rightlife.databinding.DialogSwitchAccountBinding
import com.jetsynthesys.rightlife.newdashboard.model.ChallengeDateData
import com.jetsynthesys.rightlife.newdashboard.model.ChallengeDateResponse
import com.jetsynthesys.rightlife.newdashboard.model.ChecklistResponse
import com.jetsynthesys.rightlife.newdashboard.model.DashboardChecklistManager
import com.jetsynthesys.rightlife.newdashboard.model.DashboardChecklistResponse
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.subscriptions.SubscriptionPlanListActivity
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentSuccessRequest
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentSuccessResponse
import com.jetsynthesys.rightlife.subscriptions.pojo.SdkDetail
import com.jetsynthesys.rightlife.subscriptions.pojo.SubscriptionPlansResponse
import com.jetsynthesys.rightlife.ui.ActivityUtils
import com.jetsynthesys.rightlife.ui.AppLoader
import com.jetsynthesys.rightlife.ui.Articles.ArticlesDetailActivity
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.CommonResponse
import com.jetsynthesys.rightlife.ui.DialogUtils
import com.jetsynthesys.rightlife.ui.NewCategoryListActivity
import com.jetsynthesys.rightlife.ui.NewSleepSounds.NewSleepSoundActivity
import com.jetsynthesys.rightlife.ui.affirmation.PractiseAffirmationPlaylistActivity
import com.jetsynthesys.rightlife.ui.aireport.AIReportWebViewActivity
import com.jetsynthesys.rightlife.ui.challenge.ChallengeActivity
import com.jetsynthesys.rightlife.ui.challenge.ChallengeBottomSheetHelper
import com.jetsynthesys.rightlife.ui.challenge.ChallengeBottomSheetHelper.showChallengeInfoBottomSheet
import com.jetsynthesys.rightlife.ui.challenge.ChallengeEmptyActivity
import com.jetsynthesys.rightlife.ui.challenge.DateHelper
import com.jetsynthesys.rightlife.ui.challenge.DateHelper.getChallengeDateRange
import com.jetsynthesys.rightlife.ui.challenge.DateHelper.getDaySuffix
import com.jetsynthesys.rightlife.ui.challenge.DateHelper.getDaysFromToday
import com.jetsynthesys.rightlife.ui.challenge.LeaderboardActivity
import com.jetsynthesys.rightlife.ui.challenge.ScoreColorHelper.getColorCode
import com.jetsynthesys.rightlife.ui.challenge.ScoreColorHelper.setSeekBarProgressColor
import com.jetsynthesys.rightlife.ui.challenge.pojo.DailyScoreResponse
import com.jetsynthesys.rightlife.ui.contentdetailvideo.ContentDetailsActivity
import com.jetsynthesys.rightlife.ui.deeplink.DeepLinkTarget
import com.jetsynthesys.rightlife.ui.deeplink.DeepLinkTarget.Companion.EXTRA_DEEP_LINK_TARGET
import com.jetsynthesys.rightlife.ui.healthcam.NewHealthCamReportActivity
import com.jetsynthesys.rightlife.ui.jounal.new_journal.JournalListActivity
import com.jetsynthesys.rightlife.ui.mindaudit.MASuggestedAssessmentActivity
import com.jetsynthesys.rightlife.ui.new_design.DataControlActivity
import com.jetsynthesys.rightlife.ui.profile_new.ProfileSettingsActivity
import com.jetsynthesys.rightlife.ui.profile_new.SavedItemListActivity
import com.jetsynthesys.rightlife.ui.settings.PurchasePlansActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils
import com.jetsynthesys.rightlife.ui.utility.FeatureFlags
import com.jetsynthesys.rightlife.ui.utility.NetworkUtils
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceConstants
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

class HomeNewActivity : BaseActivity() {
    // 🔹 Deeplink readiness flags
    private var pendingDeepLinkTarget: DeepLinkTarget? = null
    private var isUserProfileLoaded = false
    var isCategoryModuleLoaded = false
    private var isChecklistLoaded = false

    private var checklistCount = 0
    private var syncStatus = false
    private var isFirstTime = true

    private fun isInitialDataReadyFor(target: DeepLinkTarget): Boolean {
        // For simple navigation, no need to wait
        return when (target) {
            DeepLinkTarget.HOME,
            DeepLinkTarget.MY_HEALTH -> true

            // Features that depend on user profile & checklist
            DeepLinkTarget.JOURNAL,
            DeepLinkTarget.MEAL_LOG,
            DeepLinkTarget.BREATHING,
            DeepLinkTarget.SLEEP_SOUND,
            DeepLinkTarget.SLEEP_SOUND_PLAYLIST,
            DeepLinkTarget.PROFILE,
            DeepLinkTarget.CHALLENGE_HOME,
            DeepLinkTarget.CHALLENGE_LEADERBOARD -> {
                isUserProfileLoaded && isChecklistLoaded
            }

            DeepLinkTarget.CATEGORY_LIST -> {
                isCategoryModuleLoaded
            }

            else -> {
                // By default, be conservative (includes logs, plans, and mind audit)
                isUserProfileLoaded && isChecklistLoaded
            }
        }
    }

    private fun tryProcessPendingDeepLink() {
        val target = pendingDeepLinkTarget ?: return

        if (isInitialDataReadyFor(target)) {
            // Clear before calling to avoid infinite loops
            pendingDeepLinkTarget = null
            handleDeepLinkTarget(target)
        }
    }

    private fun handleDeepLinkTarget(target: DeepLinkTarget?) {
        if (target == null) return

        // If data not ready for this target, just store it and return
        // Note: If isInitialDataReadyFor still takes a String, use target.name or target.slug
        if (!isInitialDataReadyFor(target)) {
            pendingDeepLinkTarget = target
            return
        }

        // ✅ Data ready – now actually act
        when (target) {
            DeepLinkTarget.HOME -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeExploreFragment())
                    .commit()
                updateMenuSelection(R.id.menu_home)
            }

            DeepLinkTarget.MY_HEALTH -> {
                myHealthFragmentSelected()
            }

            DeepLinkTarget.MEAL_LOG -> {
                AnalyticsLogger.logEvent(this, AnalyticsEvent.LYA_FOOD_LOG_CLICK)
                if (checkTrailEndedAndShowDialog()) {
                    startActivity(Intent(this, MainAIActivity::class.java).apply {
                        putExtra("ModuleName", "EatRight")
                        putExtra("BottomSeatName", "MealLogTypeEat")
                        putExtra("snapMealId", snapMealId)
                    })
                }
            }

            DeepLinkTarget.BREATHING -> {
                AnalyticsLogger.logEvent(this, AnalyticsEvent.EOS_BREATH_WORK_CLICK)
                if (checkTrailEndedAndShowDialog()) {
                    ActivityUtils.startBreathWorkActivity(this)
                }
            }

            DeepLinkTarget.PROFILE -> {
                startActivity(Intent(this, ProfileSettingsActivity::class.java))
            }

            DeepLinkTarget.CATEGORY_LIST -> {
                startActivity(Intent(this, NewCategoryListActivity::class.java).apply {
                    putExtra("moduleId", "ThinkRight")
                })
            }

            DeepLinkTarget.AI_REPORT -> {
                if (sharedPreferenceManager.userProfile.isReportGenerated)
                    callAIReportCardClick()
            }

            DeepLinkTarget.MIND_AUDIT -> callMindAuditClick()

            DeepLinkTarget.FACE_SCAN -> callFaceScanClick()

            DeepLinkTarget.EAT_HOME -> {
                if (checkTrailEndedAndShowDialog()) {
                    startActivity(Intent(this, MainAIActivity::class.java).apply {
                        putExtra("ModuleName", "EatRight")
                        putExtra("BottomSeatName", "Not")
                    })
                }
            }

            DeepLinkTarget.SLEEP_HOME -> {
                if (checkTrailEndedAndShowDialog()) {
                    startActivity(Intent(this, MainAIActivity::class.java).apply {
                        putExtra("ModuleName", "SleepRight")
                        putExtra("BottomSeatName", "Not")
                    })
                }
            }

            DeepLinkTarget.MOVE_HOME -> {
                if (checkTrailEndedAndShowDialog()) {
                    startActivity(Intent(this, MainAIActivity::class.java).apply {
                        putExtra("ModuleName", "MoveRight")
                        putExtra("BottomSeatName", "Not")
                    })
                }
            }

            DeepLinkTarget.WORKOUT_LOG_DEEP -> {
                if (checkTrailEndedAndShowDialog()) {
                    startActivity(Intent(this, MainAIActivity::class.java).apply {
                        putExtra("ModuleName", "MoveRight")
                        putExtra("BottomSeatName", "SearchActivityLogMove")
                    })
                }
            }

            DeepLinkTarget.WEIGHT_LOG_DEEP -> {
                if (checkTrailEndedAndShowDialog()) {
                    startActivity(Intent(this, MainAIActivity::class.java).apply {
                        putExtra("ModuleName", "EatRight")
                        putExtra("BottomSeatName", "LogWeightEat")
                    })
                }
            }

            DeepLinkTarget.WATER_LOG_DEEP -> {
                if (checkTrailEndedAndShowDialog()) {
                    startActivity(Intent(this, MainAIActivity::class.java).apply {
                        putExtra("ModuleName", "EatRight")
                        putExtra("BottomSeatName", "LogWaterIntakeEat")
                    })
                }
            }

            DeepLinkTarget.SLEEP_LOG_DEEP -> {
                if (checkTrailEndedAndShowDialog()) {
                    startActivity(Intent(this, MainAIActivity::class.java).apply {
                        putExtra("ModuleName", "SleepRight")
                        putExtra("BottomSeatName", "LogLastNightSleep")
                    })
                }
            }

            DeepLinkTarget.THINK_HOME -> {
                if (checkTrailEndedAndShowDialog()) {
                    startActivity(Intent(this, MainAIActivity::class.java).apply {
                        putExtra("ModuleName", "ThinkRight")
                        putExtra("BottomSeatName", "Not")
                    })
                }
            }

            DeepLinkTarget.SNAP_MEAL -> callSnapMealClick()

            DeepLinkTarget.JOURNAL -> {
                if (checkTrailEndedAndShowDialog()) {
                    ActivityUtils.startJournalListActivity(this)
                }
            }

            DeepLinkTarget.SLEEP_SOUND -> {
                if (checkTrailEndedAndShowDialog()) {
                    ActivityUtils.startSleepSoundActivity(this)
                }
            }

            DeepLinkTarget.SLEEP_SOUND_PLAYLIST -> {
                if (checkTrailEndedAndShowDialog()) {
                    startActivity(Intent(this, NewSleepSoundActivity::class.java).apply {
                        putExtra("PlayList", "ForPlayList")
                    })
                }
            }

            DeepLinkTarget.AFFIRMATION -> {
                if (checkTrailEndedAndShowDialog()) {
                    ActivityUtils.startTodaysAffirmationActivity(this)
                }
            }

            DeepLinkTarget.AFFIRMATION_PLAYLIST -> {
                if (checkTrailEndedAndShowDialog()) {
                    startActivity(Intent(this, PractiseAffirmationPlaylistActivity::class.java))
                }
            }

            // Quick link logs
            DeepLinkTarget.ACTIVITY_LOG -> callLogActivitylick()
            DeepLinkTarget.WEIGHT_LOG -> callLogWeightClick()
            DeepLinkTarget.WATER_LOG -> callLogWaterClick()
            DeepLinkTarget.SLEEP_LOG -> callLogSleepClick()
            DeepLinkTarget.FOOD_LOG -> callLogFoodClick()

            DeepLinkTarget.JUMPBACK -> callJumpBackIn()

            DeepLinkTarget.SAVED_ITEMS -> {
                startActivity(Intent(this, SavedItemListActivity::class.java))
            }

            DeepLinkTarget.THINK_EXPLORE -> {
                startActivity(Intent(this, NewCategoryListActivity::class.java).apply {
                    putExtra("moduleId", "THINK_RIGHT")
                })
            }

            DeepLinkTarget.EAT_EXPLORE -> {
                startActivity(Intent(this, NewCategoryListActivity::class.java).apply {
                    putExtra("moduleId", "EAT_RIGHT")
                })
            }

            DeepLinkTarget.SLEEP_EXPLORE -> {
                startActivity(Intent(this, NewCategoryListActivity::class.java).apply {
                    putExtra("moduleId", "SLEEP_RIGHT")
                })
            }

            DeepLinkTarget.MOVE_EXPLORE -> {
                startActivity(Intent(this, NewCategoryListActivity::class.java).apply {
                    putExtra("moduleId", "MOVE_RIGHT")
                })
            }

            DeepLinkTarget.CHALLENGE_HOME -> {
                val isValidState = sharedPreferenceManager.challengeState in listOf(3, 4)
                if (isValidState && sharedPreferenceManager.challengeParticipatedDate.isNotEmpty() && DashboardChecklistManager.checklistStatus) {
                    startActivity(
                        Intent(
                            this,
                            ChallengeActivity::class.java
                        ).putExtra("SYNC_STATUS", syncStatus)
                    )
                }
            }

            DeepLinkTarget.CHALLENGE_LEADERBOARD -> {
                val isValidState = sharedPreferenceManager.challengeState in listOf(3, 4)
                if (isValidState && sharedPreferenceManager.challengeParticipatedDate.isNotEmpty() && DashboardChecklistManager.checklistStatus) {
                    startActivity(Intent(this, LeaderboardActivity::class.java))
                }
            }

            DeepLinkTarget.SUBSCRIPTION_PLAN -> {
                startActivity(Intent(this, SubscriptionPlanListActivity::class.java).apply {
                    putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN")
                })
            }

            DeepLinkTarget.BOOSTER_PLAN -> {
                startActivity(Intent(this, SubscriptionPlanListActivity::class.java).apply {
                    putExtra("SUBSCRIPTION_TYPE", "FACIAL_SCAN")
                })
            }

            DeepLinkTarget.MIND_AUDIT_PHQ9 -> callMindAuditDeepLinkClick("PHQ-9")
            DeepLinkTarget.MIND_AUDIT_OHQ -> callMindAuditDeepLinkClick("OHQ")
            DeepLinkTarget.MIND_AUDIT_CAS -> callMindAuditDeepLinkClick("CAS")
            DeepLinkTarget.MIND_AUDIT_DASS21 -> callMindAuditDeepLinkClick("DASS-21")
            DeepLinkTarget.MIND_AUDIT_GAD7 -> callMindAuditDeepLinkClick("GAD-7")

            // Breathing variations - all call the same utility
            DeepLinkTarget.BREATHING_ALTERNATE,
            DeepLinkTarget.BREATHING_BOX,
            DeepLinkTarget.BREATHING_CUSTOM,
            DeepLinkTarget.BREATHING_4_7_8 -> {
                AnalyticsLogger.logEvent(this, AnalyticsEvent.EOS_BREATH_WORK_CLICK)
                if (checkTrailEndedAndShowDialog()) {
                    ActivityUtils.startBreathWorkActivity(this)
                }
            }

            // Dynamic content types
            DeepLinkTarget.ARTICLE, DeepLinkTarget.AUDIO, DeepLinkTarget.VIDEO -> {
                val activityClass = if (target == DeepLinkTarget.ARTICLE) {
                    ArticlesDetailActivity::class.java
                } else {
                    ContentDetailsActivity::class.java
                }

                // You should pass the detail ID via EXTRA_DEEP_LINK_DETAIL_ID from the RouterActivity
                val contentId = intent.getStringExtra(DeepLinkTarget.EXTRA_DEEP_LINK_DETAIL_ID)
                    ?.split("/")?.getOrNull(2)

                if (contentId != null) {
                    startActivity(Intent(this, activityClass).putExtra("contentId", contentId))
                }
            }
        }
    }


    private var snapMealId = ""
    private lateinit var binding: ActivityHomeNewBinding
    private var isAdd = true
    private var showheaderFlag = false
    var isTrialExpired = false
    private var isCountDownVisible = false
    private var totalCaloriesBurnedRecord: List<TotalCaloriesBurnedRecord>? = null
    private var activeCalorieBurnedRecord: List<ActiveCaloriesBurnedRecord>? = null
    private var stepsRecord: List<StepsRecord>? = null
    private var heartRateRecord: List<HeartRateRecord>? = null
    private var heartRateVariability: List<HeartRateVariabilityRmssdRecord>? = null
    private var restingHeartRecord: List<RestingHeartRateRecord>? = null
    private var basalMetabolicRateRecord: List<BasalMetabolicRateRecord>? = null
    private var bloodPressureRecord: List<BloodPressureRecord>? = null
    private var sleepSessionRecord: List<SleepSessionRecord>? = null
    private var exerciseSessionRecord: List<ExerciseSessionRecord>? = null
    private var weightRecord: List<WeightRecord>? = null
    private var distanceRecord: List<DistanceRecord>? = null
    private var bodyFatRecord: List<BodyFatRecord>? = null
    private var oxygenSaturationRecord: List<OxygenSaturationRecord>? = null
    private var respiratoryRateRecord: List<RespiratoryRateRecord>? = null
    private lateinit var healthConnectClient: HealthConnectClient


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* window.apply {
             // Allow content to draw behind status bar
             decorView.systemUiVisibility =
                 View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
             statusBarColor = android.graphics.Color.TRANSPARENT // Transparent status bar
         }*/

        binding = ActivityHomeNewBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        // Load default fragment only on first launch
        val openMyHealth = intent.getBooleanExtra("OPEN_MY_HEALTH", false)
        if (savedInstanceState == null) {
            if (openMyHealth) {
                myHealthFragmentSelected()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeExploreFragment())
                    .commit()
                updateMenuSelection(R.id.menu_home)
            }
        } else {
            if (openMyHealth) {
                myHealthFragmentSelected()
            } else {
                // 🟢 Restore menu highlight based on current fragment
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                when (currentFragment) {
                    is HomeDashboardFragment -> updateMenuSelection(R.id.menu_explore)
                    is HomeExploreFragment -> updateMenuSelection(R.id.menu_home)
                }
            }
        }

        binding.layoutChallengeToCompleteChecklist.seekBar.apply {
            setOnTouchListener { _, _ -> true }
            splitTrack = false
            isFocusable = false
        }

        binding.layoutChallengeDailyScore.scoreSeekBar.apply {
            setOnTouchListener { _, _ -> true }
            splitTrack = false
            isFocusable = false
        }


        /*DialogUtils.showFreeTrailRelatedBottomSheet(this,
            "Unlock RightLife Pro to keep your health journey uninterrupted.",
            "Free Trial Ending In 2 Days",
            "Upgrade Now",
            false,
            R.drawable.ft_days_left,
            R.drawable.ft_close,
            onOkayClick = {})

        DialogUtils.showFreeTrailRelatedBottomSheet(this,
            "Your trial ends today.\n" +
                    "Upgrade to Pro to continue accessing your data and insights.",
            "Free Trial ends today",
            "Upgrade To Pro",
            false,
            R.drawable.ft_expired_today,
            R.drawable.ft_warning,
            onOkayClick = {})

        DialogUtils.showFreeTrailRelatedBottomSheet(this,
            "Your 7-Day Trial has ended. You can still view your 7-day journey, but new tracking is locked. Upgrade to Pro to continue building your health story.",
            "Free Trial ended",
            "See Plans",
            false,
            R.drawable.ft_ended,
            R.drawable.ft_warning,
            onOkayClick = {})

        DialogUtils.showFreeTrailRelatedBottomSheet(this,
            "Your Monthly Pro Plan ends in 7 days. Renew now to avoid interruptions.",
            "7 days left",
            "Renew Plan",
            false,
            R.drawable.ft_days_left,
            R.drawable.ft_close,
            onOkayClick = {})

        DialogUtils.showFreeTrailRelatedBottomSheet(this,
            "Your Monthly Pro Plan access ends tomorrow. Renew to keep your dashboard active.",
            "Plan Ends tomorrow",
            "Renew Plan",
            false,
            R.drawable.ft_expired_today,
            R.drawable.ft_close,
            onOkayClick = {})

        DialogUtils.showFreeTrailRelatedBottomSheet(this,
            "Your Pro Plan has expired. Reactivate now to continue tracking and improving your health.",
            "Plan expired",
            "Renew Plan",
            false,
            R.drawable.ft_ended,
            R.drawable.ft_warning,
            onOkayClick = {})

        DialogUtils.showFreeTrailRelatedBottomSheet(this,
            "Your Annual Pro Plan ends in 14 days. Renew today to continue enjoying full access.",
            "Plan Expiring Soon",
            "Renew Plan",
            true,
            R.drawable.ft_days_left,
            R.drawable.ft_close,
            onOkayClick = {})

        DialogUtils.showFreeTrailRelatedBottomSheet(this,
            "Just 3 days left in your Annual Pro Plan. Don’t lose your progress.",
            "3 days left",
            "Renew Plan",
            true,
            R.drawable.ft_expired_today,
            R.drawable.ft_close,
            onOkayClick = {})

        DialogUtils.showFreeTrailRelatedBottomSheet(this,
            "Your Annual Pro Plan has expired. Renew now to keep your yearly savings and full access.",
            "Annual Plan expired",
            "Renew Plan",
            true,
            R.drawable.ft_ended,
            R.drawable.ft_warning,
            onOkayClick = {})*/

        binding.flFreeTrial.setOnClickListener {
            startActivity(Intent(this, BeginMyFreeTrialActivity::class.java))
        }

        binding.scrollView.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->
            binding.swipeRefreshLayout.isEnabled = scrollY <= 5
        }

        if (sharedPreferenceManager.userProfile?.user_sub_status == 2) {
            showTrailEndedBottomSheet()
        } else if (sharedPreferenceManager.userProfile?.user_sub_status == 3) {
            showSubsciptionEndedBottomSheet()
        }

        onBackPressedDispatcher.addCallback {
            if (binding.includedhomebottomsheet.bottomSheet.visibility == View.VISIBLE) {
                binding.includedhomebottomsheet.bottomSheet.visibility = View.GONE
                binding.includedhomebottomsheet.bottomSheetParent.apply {
                    isClickable = false
                    isFocusable = false
                    visibility = View.GONE
                }

                binding.includedhomebottomsheet.bottomSheetParent.setBackgroundColor(Color.TRANSPARENT)
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                when (currentFragment) {
                    is HomeDashboardFragment -> updateMenuSelection(R.id.menu_explore)
                    is HomeExploreFragment -> updateMenuSelection(R.id.menu_home)
                }

                binding.fab.setImageResource(R.drawable.icon_quicklink_plus) // Change back to add icon
                binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                    this@HomeNewActivity, R.color.white
                )
                binding.fab.imageTintList = ColorStateList.valueOf(
                    resources.getColor(
                        R.color.rightlife
                    )
                )
                isAdd = !isAdd // Toggle the state

            } else {
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                when (currentFragment) {
                    is HomeDashboardFragment -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, HomeExploreFragment())
                            .commit()
                        updateMenuSelection(R.id.menu_home)
                    }

                    is HomeExploreFragment -> finish()
                }
            }
        }

        // Handle FAB click
        binding.fab.backgroundTintList = ContextCompat.getColorStateList(
            this, android.R.color.white
        )
        binding.fab.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.rightlife))
        val bottom_sheet = binding.includedhomebottomsheet.bottomSheet
        binding.fab.setOnClickListener { v ->

            if (binding.includedhomebottomsheet.bottomSheet.visibility == View.VISIBLE) {
                bottom_sheet.visibility = View.GONE
                binding.includedhomebottomsheet.bottomSheetParent.apply {
                    isClickable = false
                    isFocusable = false
                    visibility = View.GONE
                }
                binding.includedhomebottomsheet.bottomSheetParent.setBackgroundColor(Color.TRANSPARENT)
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                when (currentFragment) {
                    is HomeDashboardFragment -> updateMenuSelection(R.id.menu_explore)
                    is HomeExploreFragment -> updateMenuSelection(R.id.menu_home)
                }
            } else {
                bottom_sheet.visibility = View.VISIBLE
                binding.includedhomebottomsheet.bottomSheetParent.visibility = View.VISIBLE
                binding.includedhomebottomsheet.bottomSheetParent.setBackgroundColor(
                    Color.parseColor(
                        "#CC000000"
                    )
                )
            }
            v.isSelected = !v.isSelected

            binding.fab.animate().rotationBy(180f).setDuration(60)
                .setInterpolator(DecelerateInterpolator()).withEndAction {
                    // Change icon after rotation
                    if (isAdd) {
                        binding.fab.setImageResource(R.drawable.icon_quicklink_plus_black) // Change to close icon
                        binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.rightlife
                        )
                        binding.fab.imageTintList = ColorStateList.valueOf(
                            resources.getColor(
                                R.color.black
                            )
                        )
                    } else {
                        binding.fab.setImageResource(R.drawable.icon_quicklink_plus) // Change back to add icon
                        binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.white
                        )
                        binding.fab.imageTintList = ColorStateList.valueOf(
                            resources.getColor(
                                R.color.rightlife
                            )
                        )
                    }
                    isAdd = !isAdd // Toggle the state
                }.start()
        }

        binding.profileImage.setOnClickListener {
            binding.profileImage.disableViewForSeconds()  // 👈 prevent double click

            startActivity(Intent(this, ProfileSettingsActivity::class.java))
        }

        // Handle menu item clicks
        binding.menuHome.setOnClickListener {
            val currentFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            if (currentFragment is HomeExploreFragment) return@setOnClickListener

            if (binding.includedhomebottomsheet.bottomSheet.visibility == View.VISIBLE) {
                bottom_sheet.visibility = View.GONE
                binding.includedhomebottomsheet.bottomSheetParent.apply {
                    isClickable = false
                    isFocusable = false
                    visibility = View.GONE
                }
                binding.includedhomebottomsheet.bottomSheetParent.setBackgroundColor(Color.TRANSPARENT)
                binding.fab.animate().rotationBy(180f).setDuration(60)
                    .setInterpolator(DecelerateInterpolator()).withEndAction {
                        // Change icon after rotation
                        if (isAdd) {
                            binding.fab.setImageResource(R.drawable.icon_quicklink_plus_black) // Change to close icon
                            binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                                this, R.color.rightlife
                            )
                            binding.fab.imageTintList = ColorStateList.valueOf(
                                resources.getColor(
                                    R.color.black
                                )
                            )
                        } else {
                            binding.fab.setImageResource(R.drawable.icon_quicklink_plus) // Change back to add icon
                            binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                                this, R.color.white
                            )
                            binding.fab.imageTintList = ColorStateList.valueOf(
                                resources.getColor(
                                    R.color.rightlife
                                )
                            )
                        }
                        isAdd = !isAdd // Toggle the state
                    }.start()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeExploreFragment())
                    .commit()
                updateMenuSelection(R.id.menu_home)
            }
        }

        /*   binding.menuExplore.setOnClickListener {
               val currentFragment =
                   supportFragmentManager.findFragmentById(R.id.fragmentContainer)
               if (currentFragment is HomeDashboardFragment) return@setOnClickListener
               myHealthFragmentSelected()
           }*/
        binding.menuExplore.setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            if (currentFragment is HomeDashboardFragment) return@setOnClickListener

            // ✅ Step 1: If bottom sheet is currently visible, close it first
            if (binding.includedhomebottomsheet.bottomSheet.visibility == View.VISIBLE) {
                // Hide bottom sheet view
                binding.includedhomebottomsheet.bottomSheet.visibility = View.GONE

                // Hide parent dim/overlay
                binding.includedhomebottomsheet.bottomSheetParent.apply {
                    isClickable = false
                    isFocusable = false
                    visibility = View.GONE
                    setBackgroundColor(Color.TRANSPARENT)
                }

                // Reset FAB icon and colors
                binding.fab.animate().rotationBy(180f).setDuration(60)
                    .setInterpolator(DecelerateInterpolator())
                    .withEndAction {
                        if (isAdd) {
                            binding.fab.setImageResource(R.drawable.icon_quicklink_plus_black)
                            binding.fab.backgroundTintList =
                                ContextCompat.getColorStateList(this, R.color.rightlife)
                            binding.fab.imageTintList =
                                ColorStateList.valueOf(resources.getColor(R.color.black))
                        } else {
                            binding.fab.setImageResource(R.drawable.icon_quicklink_plus)
                            binding.fab.backgroundTintList =
                                ContextCompat.getColorStateList(this, R.color.white)
                            binding.fab.imageTintList =
                                ColorStateList.valueOf(resources.getColor(R.color.rightlife))
                        }
                        isAdd = !isAdd
                    }.start()

                // ✅ Don’t navigate yet — just return after closing the bottom sheet
                return@setOnClickListener
            }

            // ✅ Step 2: If bottom sheet is already closed, open Explore normally
            myHealthFragmentSelected()
        }


        with(binding) {
            includedhomebottomsheet.llJournal.setOnClickListener {
                AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.EOS_JOURNALING_CLICK)
                if (checkTrailEndedAndShowDialog()) {
                    ActivityUtils.startJournalListActivity(this@HomeNewActivity)
                }
            }
            includedhomebottomsheet.llAffirmations.setOnClickListener {
                AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.EOS_AFFIRMATION_CLICK)
                if (checkTrailEndedAndShowDialog()) {
                    ActivityUtils.startTodaysAffirmationActivity(this@HomeNewActivity)
                }
            }
            includedhomebottomsheet.llSleepsounds.setOnClickListener {
                AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.EOS_SLEEP_SOUNDS)
                if (checkTrailEndedAndShowDialog()) {
                    ActivityUtils.startSleepSoundActivity(this@HomeNewActivity)
                }
            }
            includedhomebottomsheet.llBreathwork.setOnClickListener {
                AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.EOS_BREATH_WORK_CLICK)
                if (checkTrailEndedAndShowDialog()) {
                    ActivityUtils.startBreathWorkActivity(this@HomeNewActivity)
                }
            }
            includedhomebottomsheet.llHealthCamQl.setOnClickListener {

                callFaceScanClick()
            }
            includedhomebottomsheet.llMealplan.setOnClickListener {
                callSnapMealClick()
            }

        }

        binding.includedhomebottomsheet.llFoodLog.setOnClickListener {
            AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.LYA_FOOD_LOG_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                //ActivityUtils.startEatRightReportsActivity(this@HomeNewActivity, "MealLogTypeEat")
                startActivity(Intent(this@HomeNewActivity, MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "EatRight")
                    putExtra("BottomSeatName", "MealLogTypeEat")
                    putExtra("snapMealId", snapMealId)
                })
            }
        }
        binding.includedhomebottomsheet.llActivityLog.setOnClickListener {
            AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.LYA_ACTIVITY_LOG_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startMoveRightReportsActivity(
                    this@HomeNewActivity,
                    "SearchActivityLogMove"
                )
            }
        }
        binding.includedhomebottomsheet.llMoodLog.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
            }
        }
        binding.includedhomebottomsheet.llSleepLog.setOnClickListener {
            AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.LYA_SLEEP_LOG_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startSleepRightReportsActivity(
                    this@HomeNewActivity,
                    "LogLastNightSleep"
                )
            }
        }
        binding.includedhomebottomsheet.llWeightLog.setOnClickListener {
            AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.LYA_WEIGHT_LOG_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startEatRightReportsActivity(this@HomeNewActivity, "LogWeightEat")
            }
        }
        binding.includedhomebottomsheet.llWaterLog.setOnClickListener {
            AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.LYA_WATER_LOG_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startEatRightReportsActivity(
                    this@HomeNewActivity,
                    "LogWaterIntakeEat"
                )
            }
        }

        // Open AI Report WebView on click   // Also logic to hide this button if Report is not generated pending
        binding.rightLifeReportCard.setOnClickListener {
            var dynamicReportId = "" // This Is User ID
            dynamicReportId = SharedPreferenceManager.getInstance(applicationContext).userId
            if (dynamicReportId.isEmpty()) {
                // Some error handling if the ID is not available
            } else {
                val intent = Intent(this, AIReportWebViewActivity::class.java).apply {
                    // Put the dynamic ID as an extra
                    putExtra(AIReportWebViewActivity.EXTRA_REPORT_ID, dynamicReportId)
                }
                startActivity(intent)
                AnalyticsLogger.logEvent(this, AnalyticsEvent.RL_AI_Report_Card_Tap)
            }

        }

        // Handling Subscribe to RightLife
        binding.trialExpiredLayout.btnSubscription.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SubscriptionPlanListActivity::class.java
                ).apply {
                    putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN")
                })
        }

        binding.llFreeTrailExpired.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SubscriptionPlanListActivity::class.java
                ).apply {
                    putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN")
                })
        }
        binding.tvStriketroughPrice.paintFlags =
            binding.tvStriketroughPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        Log.d(
            "UI_DEBUG", """
    flFreeTrial visible=${binding.flFreeTrial.visibility}
    flFreeTrial clickable=${binding.flFreeTrial.isClickable}
    flFreeTrial enabled=${binding.flFreeTrial.isEnabled}
    homeBottomSheet visible=${binding.includedhomebottomsheet.root.visibility}
""".trimIndent()
        )

        // deeplinks
        val deepLinkTarget = intent.getSerializableExtra(EXTRA_DEEP_LINK_TARGET) as? DeepLinkTarget
        handleDeepLinkTarget(deepLinkTarget)

        sendTokenToServer("")

        if (!sharedPreferenceManager.isHomeFirstVisited) {
            sharedPreferenceManager.isHomeFirstVisited = true
            lifecycleScope.launch {
                delay(2000)
                AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.HOME_PAGE_FIRST_OPEN)
            }
        }

        if (MainApplication.isFreshLaunch && (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED || !sharedPreferenceManager.enableNotificationServer)
        ) {
            ChallengeBottomSheetHelper.showChallengeReminderBottomSheet(this) { dialog ->
                checkPermission()
                dialog.dismiss()
            }
            MainApplication.isFreshLaunch = false
        }

        fetchHealthDataFromHealthConnect()
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (isNotificationPermanentlyDenied()) {
                    showGoToSettingsDialog()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        100
                    )
                }
                return false
            } else {
                enableNotificationServer()
                return true
            }
        } else {
            enableNotificationServer()
            // Permission not required before Android 13
            return true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableNotificationServer()
            } else {
                showCustomToast("Notification permission denied", false)
                sharedPreferenceManager.enableNotificationServer = false
            }
        }
    }


    override fun onResume() {
        super.onResume()
        //checkForUpdate()
        //new force update check below
        checkForceUpdate()
        getUserDetails()
        initBillingAndRecover()
        getDashboardChecklistStatus()
        getDashboardChecklist()
        getSubscriptionList()
        //getSubscriptionProducts(binding.tvStriketroughPrice);

        this.let {
            if (!isFirstTime) {
                if (HealthConnectClient.getSdkStatus(it) == HealthConnectClient.SDK_AVAILABLE) {
                    healthConnectClient = HealthConnectClient.getOrCreate(it)
                    lifecycleScope.launch {
                        val granted =
                            healthConnectClient.permissionController.getGrantedPermissions()
                        if (HealthConnectConstants.allReadPermissions.all { it in granted }) {
                            fetchAllHealthData()
                        }
                    }
                }
            }
            isFirstTime = false
        }
    }

    private fun enableNotificationServer() {
        val requestBody = mapOf("pushNotification" to true)
        CommonAPICall.updateNotificationSettings(this, requestBody) { result, message ->
            //showToast(message)
            sharedPreferenceManager.enableNotificationServer = true
        }
    }

    // API 35+ (Android 15)
    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        handleIncomingIntent(intent)
        // Make sure getIntent() returns the latest one if you use it elsewhere
        setIntent(intent)
    }

    // Older platforms
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
        setIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent) {
        when {
            intent.getBooleanExtra("OPEN_MY_HEALTH", false) -> {
                myHealthFragmentSelected()
            }

            intent.getBooleanExtra("start_journal", false) -> {
                startActivity(Intent(this, JournalListActivity::class.java))
            }

            intent.getBooleanExtra("start_profile", false) -> {
                startActivity(Intent(this, ProfileSettingsActivity::class.java))
            }

            intent.getBooleanExtra("finish_MindAudit", false) &&
                    intent.getBooleanExtra("FROM_THINK_RIGHT", false) -> {
                startActivity(Intent(this, MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "ThinkRight")
                    putExtra("BottomSeatName", "Not")
                })
            }

            intent.getBooleanExtra("finish_Journal", false) &&
                    intent.getBooleanExtra("FROM_THINK_RIGHT", false) -> {
                startActivity(Intent(this, MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "ThinkRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }
        val target = intent.getSerializableExtra(EXTRA_DEEP_LINK_TARGET) as? DeepLinkTarget
        handleDeepLinkTarget(target)
    }


    // get user details
    fun getUserDetails() {
        // Make the API call
        val call = apiService.getUserDetais(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    val ResponseObj = gson.fromJson(
                        jsonResponse, UserProfileResponse::class.java
                    )
                    SharedPreferenceManager.getInstance(applicationContext)
                        .saveUserId(ResponseObj.userdata.id)
                    ResponseObj.userdata.bodyFat = ResponseObj.bodyFat
                    SharedPreferenceManager.getInstance(applicationContext)
                        .saveUserProfile(ResponseObj)

                    SharedPreferenceManager.getInstance(applicationContext)
                        .setAIReportGeneratedView(ResponseObj.reportView)

                    if (ResponseObj.userdata.profilePicture != null) {
                        Glide.with(this@HomeNewActivity)
                            .load(ApiClient.CDN_URL_QA + ResponseObj.userdata.profilePicture)
                            .placeholder(R.drawable.rl_profile).error(R.drawable.rl_profile)
                            .into(binding.profileImage)
                    }

                    var name = ResponseObj.userdata.firstName
                    /*if (!ResponseObj.userdata.lastName.isNullOrEmpty())
                        name = name.plus(" ${ResponseObj.userdata.lastName}")*/
                    binding.userName.text = name

                    val tvGreetingText = findViewById<TextView>(R.id.greetingText)
                    tvGreetingText.text = "Good " + DateTimeUtils.getWishingMessage() + ","

                    if (ResponseObj.isReportGenerated && !ResponseObj.reportView) {
                        binding.rightLifeReportCard.visibility = View.VISIBLE
                    } else {
                        binding.rightLifeReportCard.visibility = View.GONE
                    }

                    // Find HealthCam service and check if it's free
                    var isHealthCamFree = ResponseObj?.homeServices
                        ?.find { it.title == "HealthCam" }
                        ?.isFree ?: false
                    Log.d("isHealthCamFree", isHealthCamFree.toString())
                    handleUserSubscriptionStatus(ResponseObj.user_sub_status)
                    if (ResponseObj.freeServiceDate.isNotEmpty()) {
                        // Get the current fragment
                        supportFragmentManager.findFragmentById(R.id.fragmentContainer)

                        if (ResponseObj.user_sub_status != 1 || ResponseObj.user_sub_status != 3)
                            binding.llCountDown.visibility = View.VISIBLE
                        showSevenDayCountdown(ResponseObj.freeServiceDate, binding.tvDays)
                    } else {
                        binding.tvDays.text = ""
                        binding.llCountDown.visibility = View.GONE
                    }
                    isUserProfileLoaded = true
                    tryProcessPendingDeepLink()
                    if (ResponseObj.user_sub_status == 2) {
                        AnalyticsLogger.logEvent(
                            this@HomeNewActivity,
                            AnalyticsEvent.FreeTrialEnded
                        )
                    }
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    if (response.code() == 401) {
                        clearUserDataAndFinish()
                    }
                    isUserProfileLoaded = true
                    tryProcessPendingDeepLink()
                }

                /*  if (!DashboardChecklistManager.paymentStatus) {
                      binding.trialExpiredLayout.trialExpiredLayout.visibility = View.VISIBLE
                      isTrialExpired = true
                  } else {
                      binding.trialExpiredLayout.trialExpiredLayout.visibility = View.GONE
                      isTrialExpired = false
                  }*/
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                handleNoInternetView(t)
                isUserProfileLoaded = true
                tryProcessPendingDeepLink()
            }
        })
    }

    fun checkTrailEndedAndShowDialog(): Boolean {
        /*  binding.includedhomebottomsheet.bottomSheet.visibility = View.GONE
          binding.includedhomebottomsheet.bottomSheetParent.apply {
              isClickable = false
              isFocusable = false
              visibility = View.GONE
          }
          binding.includedhomebottomsheet.bottomSheetParent.setBackgroundColor(Color.TRANSPARENT)
          binding.fab.setImageResource(R.drawable.icon_quicklink_plus) // Change back to add icon
          binding.fab.backgroundTintList = ContextCompat.getColorStateList(
              this@HomeNewActivity, R.color.white
          )
          binding.fab.imageTintList = ColorStateList.valueOf(
              resources.getColor(
                  R.color.rightlife
              )
          )
          isAdd = !isAdd // Toggle the state*/
        // commented above code as now as per product it should be kept open while going to next screenṄ

        return if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
            freeTrialDialogActivity()
            false // Return false if condition is true and dialog is shown
        } else {
            if (!DashboardChecklistManager.checklistStatus) {
                DialogUtils.showCheckListQuestionCommonDialog(this) {
                    //myHealthFragmentSelected()
                    HandleQuicklinkmenu()
                }
                false
            } else if (sharedPreferenceManager.userProfile?.user_sub_status == 2) {
                showTrailEndedBottomSheet()
                false // Return false if condition is true and dialog is shown
            } else if (sharedPreferenceManager.userProfile?.user_sub_status == 3) {
                showSubsciptionEndedBottomSheet()
                false // Return false if condition is true and dialog is shown
            } else {
                true
            }
        }
        return true

    }

    private fun checkForceUpdate() {
        if (!sharedPreferenceManager.isForceUpdateEnabled) return

        val minRequired = sharedPreferenceManager.forceUpdateMinVersion
        val currentVersion = BuildConfig.VERSION_NAME ?: "0"

        if (isVersionLower(currentVersion, minRequired)) {
            showForceUpdateDialog(
                sharedPreferenceManager.forceUpdateMessage,
                sharedPreferenceManager.forceUpdateUrl
            )
        }
    }

    private fun isVersionLower(current: String?, required: String?): Boolean {
        if (current.isNullOrBlank() || required.isNullOrBlank()) return false

        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val requiredParts = required.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(currentParts.size, requiredParts.size)

        for (i in 0 until maxLength) {
            val currentValue = currentParts.getOrElse(i) { 0 }
            val requiredValue = requiredParts.getOrElse(i) { 0 }

            if (currentValue < requiredValue) return true
            if (currentValue > requiredValue) return false
        }
        return false
    }


    private fun checkForUpdate() {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(
            mapOf(
                "force_update_current_version" to "1.0.0",
                "isForceUpdate" to false,
                "force_update_build_number" to "261"
            )
        )

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val latestVersion = remoteConfig.getString("force_update_current_version")
                val isForceUpdate = remoteConfig.getBoolean("isForceUpdate")
                remoteConfig.getString("force_update_build_number")

                val currentVersion = BuildConfig.VERSION_NAME

                if (isForceUpdate && isVersionOutdated(currentVersion, latestVersion)) {
                    showForceUpdateDialog(
                        sharedPreferenceManager.forceUpdateMessage,
                        sharedPreferenceManager.forceUpdateUrl
                    )
                }
            }
        }
    }

    private fun isVersionOutdated(current: String, latest: String): Boolean {
        return current != latest // or use a version comparator for more complex rules
    }

    private fun getCountDownDays(pastTimestamp: Long): Int {
        val currentTimestamp = System.currentTimeMillis()

        val diffInMillis = currentTimestamp - pastTimestamp
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
        return diffInDays.toInt()
    }

    private fun showForceUpdateDialog(forceUpdateMessage: String, forceUpdateUrl: String) {

        // Create the dialog
        val dialog = Dialog(this)
        val binding = DialogForceUpdateBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(false) // Prevent back press
        dialog.setCanceledOnTouchOutside(false) // Prevent outside tap
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val window = dialog.window
        window?.let {
            val layoutParams = it.attributes
            layoutParams.dimAmount = 0.7f
            it.attributes = layoutParams
        }

        binding.btnUpdate.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")
                )
            )
            finish()
        }

        dialog.show()

    }


    private fun updateMenuSelection(selectedMenuId: Int) {
        // Reset both to unselected
        binding.iconHome.setImageResource(R.drawable.new_home_unselected_svg)
        binding.labelHome.setTextColor(ContextCompat.getColor(this, R.color.gray))
        binding.labelHome.setTypeface(null, Typeface.NORMAL)

        binding.iconExplore.setImageResource(R.drawable.my_health_menu_unselected_new)
        binding.labelExplore.setTextColor(ContextCompat.getColor(this, R.color.gray))
        binding.labelExplore.setTypeface(null, Typeface.NORMAL)

        // Now highlight the selected one
        when (selectedMenuId) {
            R.id.menu_home -> {
                binding.iconHome.setImageResource(R.drawable.new_home_selected_svg)
                binding.labelHome.setTextColor(ContextCompat.getColor(this, R.color.rightlife))
                binding.labelHome.setTypeface(null, Typeface.BOLD)
            }

            R.id.menu_explore -> {
                binding.iconExplore.setImageResource(R.drawable.my_health_menu_selected_new)
                binding.labelExplore.setTextColor(ContextCompat.getColor(this, R.color.rightlife))
                binding.labelExplore.setTypeface(null, Typeface.BOLD)
            }
        }
    }

    fun showTrailEndedBottomSheet() {
        DialogUtils.showFreeTrailRelatedBottomSheet(
            this,
            "Your 7-Day Trial has ended. You can still view your 7-day journey, but new tracking is locked. Upgrade to Pro to continue building your health story.",
            "Free Trial Ended",
            "See Plans",
            false,
            R.drawable.ft_ended,
            R.drawable.ft_warning,
            onOkayClick = {
                if (NetworkUtils.isInternetAvailable(this)) {
                    startActivity(Intent(this, SubscriptionPlanListActivity::class.java).apply {
                        putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN")
                    })
                } else {
                    showInternetError()
                }
            })
    }

    fun showSubsciptionEndedBottomSheet() {
        DialogUtils.showFreeTrailRelatedBottomSheet(
            this,
            "Reactivate now to continue tracking and improving your health.",
            "No Active Plan",
            "See Plans",
            false,
            R.drawable.ft_ended,
            R.drawable.ft_warning,
            onOkayClick = {
                if (NetworkUtils.isInternetAvailable(this)) {
                    startActivity(Intent(this, SubscriptionPlanListActivity::class.java).apply {
                        putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN")
                    })
                    AnalyticsLogger.logEvent(this, AnalyticsEvent.ManageSubs_Explore)
                } else {
                    showInternetError()
                }
            })
    }

    /*private fun showTrailEndedBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetTrialEndedBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)


        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnExplorePlan.setOnClickListener {
            bottomSheetDialog.dismiss()
            if (NetworkUtils.isInternetAvailable(this)) {
                startActivity(Intent(this, SubscriptionPlanListActivity::class.java).apply {
                    putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN")
                })
            } else {
                showInternetError()
            }
            //finish()
        }

        bottomSheetDialog.show()
    }*/

    fun showHeader(show: Boolean) {
        showheaderFlag = show
        if (sharedPreferenceManager.userProfile?.user_sub_status == 1 || sharedPreferenceManager.userProfile?.freeServiceDate?.isNotEmpty() == true) {
            binding.llCountDown.visibility =
                if (show && isCountDownVisible) View.VISIBLE else View.GONE
        } else if (sharedPreferenceManager.userProfile?.user_sub_status == 2) {
            binding.llFreeTrailExpired.visibility = View.VISIBLE
            binding.llCountDown.visibility = View.GONE
        }
    }

    private fun showInternetError() {
        Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
    }

    fun showSubsribeLayout(show: Boolean) {
        if (!DashboardChecklistManager.paymentStatus) {
            binding.trialExpiredLayout.trialExpiredLayout.visibility = View.VISIBLE
            binding.llFreeTrailExpired.visibility = View.VISIBLE
            isTrialExpired = true
        } else {
            binding.trialExpiredLayout.trialExpiredLayout.visibility = View.GONE
            binding.llFreeTrailExpired.visibility = View.GONE
            isTrialExpired = false
        }
    }

    private lateinit var billingClient: BillingClient


    private fun initBillingAndRecover() {

        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener { billingResult, purchases ->

                // Optional: React to new purchases here if needed

            }.enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()

            )

            .build()



        billingClient.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult) {

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                    // Query in-app purchases

                    recoverInAppPurchases()

                    // Query subscriptions

                    recoverSubscriptions()
                    //getSubscriptionProducts(binding.tvStriketroughPrice);
                }

            }


            override fun onBillingServiceDisconnected() {

                // Retry connection if needed

            }

        })

    }


    private val TAG = "BillingRecovery"

    private fun recoverInAppPurchases() {
        Log.d(TAG, "Starting recoverInAppPurchases()...")

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            Log.d(
                TAG,
                "INAPP queryPurchasesAsync() result: code=${billingResult.responseCode}, purchasesCount=${purchases.size}"
            )


            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    Log.d(
                        TAG,
                        "Found INAPP purchase: token=${purchase.purchaseToken}, state=${purchase.purchaseState}, acknowledged=${purchase.isAcknowledged}"
                    )

                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        Log.d(TAG, "Processing INAPP purchase: ${purchase.purchaseToken}")
                        consumeIfNeeded(purchase)
                    }
                }
            } else {
                Log.d(TAG, "INAPP purchase query failed: ${billingResult.debugMessage}")
            }
        }
    }

    private fun recoverSubscriptions() {
        Log.d(TAG, "Starting recoverSubscriptions()...")

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            Log.d(
                TAG,
                "SUBS queryPurchasesAsync() result: code=${billingResult.responseCode}, purchasesCount=${purchases.size}"
            )

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    Log.d(
                        TAG,
                        "Found SUBS purchase: token=${purchase.purchaseToken}, state=${purchase.purchaseState}, acknowledged=${purchase.isAcknowledged}"
                    )

                    if (!purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        Log.d(TAG, "Acknowledging subscription: ${purchase.purchaseToken}")
                        acknowledgeSubscription(purchase)
                    } else if (purchase.isAcknowledged) {
                        Log.d(
                            TAG,
                            "Subscription already acknowledged: ${purchase.purchaseToken} — consider updating backend/UI"
                        )
                        updateBackendForPurchase(purchase)
                    }
                }
            } else {
                Log.d(TAG, "SUBS purchase query failed: ${billingResult.debugMessage}")
            }
        }
    }

    private fun consumeIfNeeded(purchase: Purchase) {
        Log.d(TAG, "consumeIfNeeded() called for token=${purchase.purchaseToken}")

        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { result, _ ->
            Log.d(
                TAG,
                "consumeAsync() result: code=${result.responseCode} for token=${purchase.purchaseToken}"
            )

            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase consumed successfully: ${purchase.purchaseToken}")
                updateBackendForPurchase(purchase)
            } else {
                Log.d(TAG, "Failed to consume purchase: ${result.debugMessage}")
            }
        }
    }

    private fun acknowledgeSubscription(purchase: Purchase) {
        Log.d(TAG, "acknowledgeSubscription() called for token=${purchase.purchaseToken}")

        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgeParams) { result ->
            Log.d(
                TAG,
                "acknowledgePurchase() result: code=${result.responseCode} for token=${purchase.purchaseToken}"
            )

            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Subscription acknowledged successfully: ${purchase.purchaseToken}")
                updateBackendForPurchase(purchase)
            } else {
                Log.d(TAG, "Failed to acknowledge subscription: ${result.debugMessage}")
            }
        }
    }

    /*    private fun updateBackendForPurchase(purchase: Purchase) {
            Log.d(
                TAG,
                "Updating backend for purchase: token=${purchase.purchaseToken}, products=${purchase.products}"
            )

            val paymentSuccessRequest = PaymentSuccessRequest().apply {
                planId = purchase.products.firstOrNull() ?: ""
                planName = purchase.products.firstOrNull() ?: ""
                paymentGateway = "googlePlay"
                orderId = purchase.orderId ?: ""
                environment = "payment"
                notifyType = "SDK"
                couponId = ""
                obfuscatedExternalAccountId = ""
                price = purchase.products.firstOrNull() ?: ""

                sdkDetail = SdkDetail().apply {
                    price = ""
                    orderId = purchase.orderId ?: ""
                    title = ""
                    environment = "payment"
                    description = ""
                    currencyCode = "INR"
                    currencySymbol = "₹"
                }
            }

            saveSubscriptionSuccess(paymentSuccessRequest)
        }*/

    private fun updateBackendForPurchase(purchase: Purchase) {
        Log.d(
            TAG,
            "Updating backend for purchase: token=${purchase.purchaseToken}, products=${purchase.products}"
        )

        // ✅ Get user profile from SharedPreferences
        val userProfileResponse = SharedPreferenceManager.getInstance(this).userProfile
        val purchasedProductId = purchase.products.firstOrNull()

        var resolvedPlanId = ""
        var resolvedPlanName = ""

        if (userProfileResponse != null && purchasedProductId != null) {
            // check subscriptions
            userProfileResponse.subscription?.firstOrNull {
                it.productId.equals(purchasedProductId, ignoreCase = true)
            }?.let {
                resolvedPlanId = it.planId ?: ""
                resolvedPlanName = it.planName ?: ""
            }

            // if not found in subscription, check boosters
            if (resolvedPlanId.isEmpty()) {
                userProfileResponse.booster?.firstOrNull {
                    it.productId.equals(purchasedProductId, ignoreCase = true)
                }?.let {
                    resolvedPlanId = it.planId ?: ""
                    resolvedPlanName = it.planName ?: ""
                }
            }
        }

        val paymentSuccessRequest = PaymentSuccessRequest().apply {
            planId = resolvedPlanId
            planName = resolvedPlanName
            paymentGateway = "googlePlay"
            orderId = purchase.orderId ?: ""
            environment = "payment"
            notifyType = "SDK"
            couponId = ""
            obfuscatedExternalAccountId = ""
            price = purchasedProductId ?: ""

            sdkDetail = SdkDetail().apply {
                price = ""
                orderId = purchase.orderId ?: ""
                title = resolvedPlanName
                environment = "payment"
                description = ""
                currencyCode = "INR"
                currencySymbol = "₹"
            }
        }

        saveSubscriptionSuccess(paymentSuccessRequest)
    }


    private fun saveSubscriptionSuccess(paymentSuccessRequest: PaymentSuccessRequest) {
        Log.d(
            TAG,
            "Calling saveSubscriptionSuccess() with planId=${paymentSuccessRequest.planId}, orderId=${paymentSuccessRequest.orderId}"
        )

        val call = apiService.savePaymentSuccess(
            sharedPreferenceManager.accessToken,
            paymentSuccessRequest
        )
        call.enqueue(object : Callback<PaymentSuccessResponse> {
            override fun onResponse(
                call: Call<PaymentSuccessResponse>,
                response: Response<PaymentSuccessResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "savePaymentSuccess() success: ${response.body()}")
                } else {
                    Log.d(
                        TAG,
                        "savePaymentSuccess() failed: code=${response.code()}, message=${response.message()}"
                    )
                }
            }

            override fun onFailure(call: Call<PaymentSuccessResponse>, t: Throwable) {
                Log.d(
                    TAG,
                    "savePaymentSuccess() failed due to network or server error: ${t.localizedMessage}"
                )
                handleNoInternetView(t)
            }
        })
    }


    fun showSwitchAccountDialog(context: Context, header: String, htmlText: String) {
        val dialog = Dialog(context)
        val binding = DialogSwitchAccountBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window = dialog.window
        // Set the dim amount
        val layoutParams = window!!.attributes
        layoutParams.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window.attributes = layoutParams
        binding.btnSwitchAccount.text = "Cancel"
        binding.btnSwitchAccount.visibility = View.GONE
        binding.tvTitle.text = "Free Service Already Used on This Device"
        binding.tvDescription.text =
            "Looks like this device has already claimed a free service under another account. To continue, log out and switch to a new device."

        // Handle close button click
        binding.btnOk.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnSwitchAccount.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun fetchHealthDataFromHealthConnect() {
        val availabilityStatus = HealthConnectClient.getSdkStatus(this)
        if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
            healthConnectClient = HealthConnectClient.getOrCreate(this)
            lifecycleScope.launch {
                requestPermissionsAndReadAllData()
            }
        } else {
            Toast.makeText(
                this@HomeNewActivity,
                "Please install or update health connect from the Play Store.",
                Toast.LENGTH_LONG
            ).show()
            onSyncCompleteActions()
        }
    }

    private suspend fun requestPermissionsAndReadAllData() {
        try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (HealthConnectConstants.allReadPermissions.all { it in granted }) {
                fetchAllHealthData()
            } else {
                requestPermissionsLauncher.launch(HealthConnectConstants.allReadPermissions)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@HomeNewActivity,
                    "Error checking permissions: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
            lifecycleScope.launch {
                if (granted.containsAll(HealthConnectConstants.allReadPermissions)) {
                    fetchAllHealthData()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@HomeNewActivity,
                            "Permissions Granted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                    }
                }
            }
        }

    private suspend fun fetchAllHealthData() {
        runOnUiThread {
            updateResyncTextView(true)
            if (sharedPreferenceManager.isNewUser) {
                updateSync(isLoading = true)
                syncStatus = true
            } else {
                showCompactSyncView()
            }
        }
        try {
            val ctx = this@HomeNewActivity ?: return
            val client = healthConnectClient
            val granted = try {
                client.permissionController.getGrantedPermissions()
            } catch (e: Exception) {
                Log.e("HealthSync", "Permission fetch failed", e)
                emptySet()
            }
            Log.d("HealthSync", "Granted permissions = $granted")
            val now = Instant.now()
            val savedSync = SharedPreferenceManager
                .getInstance(ctx)
                .moveRightSyncTime
                .orEmpty()

            val isFirstSync = savedSync.isBlank()
            val defaultStart = now.minus(Duration.ofDays(30))
            val lastSyncInstant = if (isFirstSync) null else runCatching {
                Instant.parse(savedSync)
            }.getOrNull()

            // ✅ TIMEZONE SAFE
            val todayStart = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()

            val computedStartTime = when {
                isFirstSync -> defaultStart
                lastSyncInstant != null -> {
                    if (lastSyncInstant.isAfter(todayStart)) todayStart else lastSyncInstant
                }

                else -> defaultStart
            }
            val endTime = now
            Log.d("HealthSync", "StartTime = $computedStartTime")
            Log.d("HealthSync", "EndTime   = $endTime")
            var latestModified: Instant? = null
            var foundNewData = false

            fun markModified(record: Record) {
                foundNewData = true
                val modified = record.metadata.lastModifiedTime
                if (latestModified == null || modified.isAfter(latestModified)) {
                    latestModified = modified
                }
            }

            suspend fun <T : Record> fetchChunk(type: KClass<T>): List<T> {
                return try {
                    if (isFirstSync) {
                        fetchChunked(type, computedStartTime, endTime, 15)
                    } else {
                        client.readRecords(
                            ReadRecordsRequest(
                                type,
                                TimeRangeFilter.between(computedStartTime, endTime)
                            )
                        ).records
                    }
                } catch (e: Exception) {
                    Log.e("HealthSync", "Fetch failed for ${type.simpleName}", e)
                    emptyList()
                }
            }

            fun <T : Record> hasPermission(type: KClass<T>): Boolean {
                val perm = HealthPermission.getReadPermission(type)
                val grantedNow = perm in granted
                if (!grantedNow) {
                    Log.w("HealthSync", "Missing permission for ${type.simpleName}")
                }
                return grantedNow
            }

            suspend fun <T : Record> load(
                type: KClass<T>,
                assign: (List<T>) -> Unit
            ) {
                Log.d("HealthSync", "Loading ${type.simpleName}")
                if (!hasPermission(type)) {
                    assign(emptyList())
                    return
                }
                val records = fetchChunk(type)
                Log.d("HealthSync", "${type.simpleName} count = ${records.size}")
                assign(records)
                records.forEach { markModified(it) }
            }
            // ------------------------------
            // FETCH
            // ------------------------------
            load(TotalCaloriesBurnedRecord::class) { totalCaloriesBurnedRecord = it }
            load(StepsRecord::class) { stepsRecord = it }
            load(HeartRateRecord::class) { heartRateRecord = it }
            load(RestingHeartRateRecord::class) { restingHeartRecord = it }
            load(ActiveCaloriesBurnedRecord::class) { activeCalorieBurnedRecord = it }
            load(HeartRateVariabilityRmssdRecord::class) { heartRateVariability = it }
            load(SleepSessionRecord::class) { sleepSessionRecord = it }
            load(ExerciseSessionRecord::class) { exerciseSessionRecord = it }
            load(RespiratoryRateRecord::class) { respiratoryRateRecord = it }
            load(WeightRecord::class) { weightRecord = it }
            load(BodyFatRecord::class) { bodyFatRecord = it }
            load(DistanceRecord::class) { distanceRecord = it }
            load(OxygenSaturationRecord::class) { oxygenSaturationRecord = it }
            load(BasalMetabolicRateRecord::class) { basalMetabolicRateRecord = it }
            load(BloodPressureRecord::class) { bloodPressureRecord = it }

            // ------------------------------
            // DEVICE DETECTION
            // ------------------------------
            val devicePackage =
                stepsRecord?.firstOrNull()?.metadata?.dataOrigin?.packageName ?: "unknown"
            val deviceManufacturer =
                stepsRecord?.firstOrNull()?.metadata?.device?.manufacturer ?: devicePackage
            SharedPreferenceManager.getInstance(ctx).saveDeviceName(deviceManufacturer)
            // ------------------------------
            // SAVE SYNC TIME
            // ------------------------------
            if (foundNewData && latestModified != null) {
                SharedPreferenceManager
                    .getInstance(ctx)
                    .saveMoveRightSyncTime(latestModified.toString())

                Log.d("HealthSync", "Updated lastSync = $latestModified")
            } else {
                Log.d("HealthSync", "No new data. Sync time unchanged")
            }

            // ------------------------------
            // PUSH TO SERVER
            // ------------------------------
            when (devicePackage) {
                "com.google.android.apps.fitness" -> storeHealthData()
                "com.sec.android.app.shealth",
                "com.samsung.android.wear.shealth" -> storeSamsungHealthData()

                else -> storeHealthData()
            }

        } catch (e: Exception) {
            Log.e("HealthSync", "Fatal error", e)
        } finally {
            //hideLoaderSafe()
        }
    }

    private suspend fun <T : Record> fetchChunked(
        type: KClass<T>,
        start: Instant,
        end: Instant,
        chunks: Int
    ): List<T> {
        val output = mutableListOf<T>()
        val total = Duration.between(start, end)
        val chunk = total.dividedBy(chunks.toLong())

        var cursor = start
        repeat(chunks) { i ->
            val next = if (i == chunks - 1) end else cursor.plus(chunk)
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(type, TimeRangeFilter.between(cursor, next))
            )
            output.addAll(response.records)
            cursor = next
        }
        return output
    }

    private fun storeHealthData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val timeZone = ZoneId.systemDefault().id
                val userid = SharedPreferenceManager.getInstance(this@HomeNewActivity).userId
                var activeEnergyBurned: List<EnergyBurnedRequest>? = null
                if (activeCalorieBurnedRecord!!.isNotEmpty()) {
                    activeEnergyBurned = activeCalorieBurnedRecord?.mapNotNull { record ->
                        if (record.energy.inKilocalories > 0) {
                            EnergyBurnedRequest(
                                start_datetime = convertToTargetFormat(record.startTime.toString()),
                                end_datetime = convertToTargetFormat(record.endTime.toString()),
                                record_type = "ActiveEnergyBurned",
                                unit = "kcal",
                                value = record.energy.inKilocalories.toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    } ?: emptyList()
                } else {
                    activeEnergyBurned = totalCaloriesBurnedRecord?.mapNotNull { record ->
                        if (record.energy.inKilocalories > 0) {
                            EnergyBurnedRequest(
                                start_datetime = convertToTargetFormat(record.startTime.toString()),
                                end_datetime = convertToTargetFormat(record.endTime.toString()),
                                record_type = "ActiveEnergyBurned",
                                unit = "kcal",
                                value = record.energy.inKilocalories.toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    } ?: emptyList()
                }
                val basalEnergyBurned = basalMetabolicRateRecord?.map { record ->
                    EnergyBurnedRequest(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BasalMetabolic",
                        unit = "power",
                        value = record.basalMetabolicRate.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val distanceWalkingRunning = distanceRecord?.mapNotNull { record ->
                    if (record.distance.inKilometers > 0) {
                        val km = record.distance.inKilometers
                        val safeKm = if (km.isFinite()) km else 0.0
                        Distance(
                            start_datetime = convertToTargetFormat(record.startTime.toString()),
                            end_datetime = convertToTargetFormat(record.endTime.toString()),
                            record_type = "DistanceWalkingRunning",
                            unit = "km",
                            value = String.format(Locale.US, "%.2f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val stepCount = stepsRecord?.mapNotNull { record ->
                    if (record.count > 0) {
                        StepCountRequest(
                            start_datetime = convertToTargetFormat(record.startTime.toString()),
                            end_datetime = convertToTargetFormat(record.endTime.toString()),
                            record_type = "StepCount",
                            unit = "count",
                            value = record.count.toString(),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val heartRate = heartRateRecord?.flatMap { record ->
                    record.samples.mapNotNull { sample ->
                        if (sample.beatsPerMinute > 0) {
                            HeartRateRequest(
                                start_datetime = convertToTargetFormat(record.startTime.toString()),
                                end_datetime = convertToTargetFormat(record.endTime.toString()),
                                record_type = "HeartRate",
                                unit = "bpm",
                                value = sample.beatsPerMinute.toInt().toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    }
                } ?: emptyList()
                val heartRateVariability = heartRateVariability?.map { record ->
                    HeartRateVariabilityRequest(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "HeartRateVariability",
                        unit = "double",
                        value = record.heartRateVariabilityMillis.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val restingHeartRate = restingHeartRecord?.map { record ->
                    HeartRateRequest(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "RestingHeartRate",
                        unit = "bpm",
                        value = record.beatsPerMinute.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val respiratoryRate = respiratoryRateRecord?.mapNotNull { record ->
                    if (record.rate > 0) {
                        val km = record.rate
                        val safeKm = if (km.isFinite()) km else 0.0
                        RespiratoryRate(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "RespiratoryRate",
                            unit = "breaths/min",
                            value = String.format(Locale.US, "%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val oxygenSaturation = oxygenSaturationRecord?.mapNotNull { record ->
                    if (record.percentage.value > 0) {
                        val km = record.percentage.value
                        val safeKm = if (km.isFinite()) km else 0.0
                        OxygenSaturation(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "OxygenSaturation",
                            unit = "%",
                            value = String.format(Locale.US, "%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val bloodPressureSystolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BloodPressureSystolic",
                        unit = "millimeterOfMercury",
                        value = record.systolic.inMillimetersOfMercury.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val bloodPressureDiastolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BloodPressureDiastolic",
                        unit = "millimeterOfMercury",
                        value = record.diastolic.inMillimetersOfMercury.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val bodyMass = weightRecord?.mapNotNull { record ->
                    if (record.weight.inKilograms > 0) {
                        val km = record.weight.inKilograms
                        val safeKm = if (km.isFinite()) km else 0.0
                        BodyMass(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "BodyMass",
                            unit = "kg",
                            value = String.format(Locale.US, "%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val bodyFatPercentage = bodyFatRecord?.mapNotNull { record ->
                    val km = record.percentage.value
                    val safeKm = if (km.isFinite()) km else 0.0
                    BodyFatPercentage(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BodyFat",
                        unit = "percentage",
                        value = String.format(Locale.US, "%.1f", safeKm),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val sleepStage = sleepSessionRecord?.flatMap { record ->
                    if (record.stages.isEmpty()) {
                        // No stages → return default "sleep"
                        listOf(
                            SleepStageJson(
                                start_datetime = convertToTargetFormat(record.startTime.toString()),
                                end_datetime = convertToTargetFormat(record.endTime.toString()),
                                record_type = "Asleep",
                                unit = "stage",
                                value = "Asleep",
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        )
                    } else {
                        // Map actual stages
                        record.stages.mapNotNull { stage ->
                            val stageValue = when (stage.stage) {
                                SleepSessionRecord.STAGE_TYPE_DEEP -> "Deep Sleep"
                                SleepSessionRecord.STAGE_TYPE_LIGHT -> "Light Sleep"
                                SleepSessionRecord.STAGE_TYPE_REM -> "REM Sleep"
                                SleepSessionRecord.STAGE_TYPE_AWAKE -> "Awake"
                                else -> null
                            }
                            stageValue?.let {
                                SleepStageJson(
                                    start_datetime = convertToTargetFormat(stage.startTime.toString()),
                                    end_datetime = convertToTargetFormat(stage.endTime.toString()),
                                    record_type = it,
                                    unit = "sleep_stage",
                                    value = it,
                                    source_name = record.metadata.dataOrigin.packageName
                                )
                            }
                        }
                    }
                } ?: emptyList()
                val workout = exerciseSessionRecord?.mapNotNull { record ->
                    val workoutType = when (record.exerciseType) {
                        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "Running"
                        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "Walking"
                        ExerciseSessionRecord.EXERCISE_TYPE_GYMNASTICS -> "Gym"
                        ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT -> "Other Workout"
                        ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "Biking"
                        ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY -> "Biking Stationary"
                        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "Cycling"
                        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> "Swimming"
                        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "Strength Training"
                        ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "Yoga"
                        ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> "HIIT"
                        ExerciseSessionRecord.EXERCISE_TYPE_BADMINTON -> "Badminton"
                        ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL -> "Basketball"
                        ExerciseSessionRecord.EXERCISE_TYPE_BASEBALL -> "Baseball"
                        else -> "Other"
                    }
                    val distance = record.metadata.dataOrigin.let { 5.0 }
                    val safeDistance = if (distance.isFinite()) distance else 0.0
                    WorkoutRequest(
                        start_datetime = convertToTargetFormat(record.startTime.toString()),
                        end_datetime = convertToTargetFormat(record.endTime.toString()),
                        source_name = record.metadata.dataOrigin.packageName,
                        record_type = "Workout",
                        workout_type = workoutType,
                        duration = ((record.endTime.toEpochMilli() - record.startTime.toEpochMilli()) / 1000 / 60).toString(),
                        calories_burned = "",
                        distance = String.format(Locale.US, "%.1f", safeDistance),
                        duration_unit = "minutes",
                        calories_unit = "kcal",
                        distance_unit = "km"
                    )
                } ?: emptyList()
                StoreHealthDataRequest(
                    user_id = userid,
                    source = "android",
                    active_energy_burned = activeEnergyBurned,
                    basal_energy_burned = basalEnergyBurned,
                    distance_walking_running = distanceWalkingRunning,
                    step_count = stepCount,
                    heart_rate = heartRate,
                    heart_rate_variability_SDNN = heartRateVariability,
                    resting_heart_rate = restingHeartRate,
                    respiratory_rate = respiratoryRate,
                    oxygen_saturation = oxygenSaturation,
                    blood_pressure_systolic = bloodPressureSystolic,
                    blood_pressure_diastolic = bloodPressureDiastolic,
                    body_mass = bodyMass,
                    body_fat_percentage = bodyFatPercentage,
                    sleep_stage = sleepStage,
                    workout = workout,
                    time_zone = timeZone
                )
                val gson = Gson()
                val allRecords = mutableListOf<Any>()
                allRecords.addAll(activeEnergyBurned)
                allRecords.addAll(basalEnergyBurned)
                allRecords.addAll(distanceWalkingRunning)
                allRecords.addAll(stepCount)
                allRecords.addAll(heartRate)
                allRecords.addAll(heartRateVariability)
                allRecords.addAll(restingHeartRate)
                allRecords.addAll(respiratoryRate)
                allRecords.addAll(oxygenSaturation)
                allRecords.addAll(bloodPressureSystolic)
                allRecords.addAll(bloodPressureDiastolic)
                allRecords.addAll(bodyMass)
                allRecords.addAll(bodyFatPercentage)
                allRecords.addAll(sleepStage)
                allRecords.addAll(workout)

                // Chunk upload variables
                var currentBatch = mutableListOf<Any>()
                var currentSize = 0
                val maxSize = 10 * 1024 * 1024 // 10MB

                suspend fun uploadBatch(batch: List<Any>) {
                    if (batch.isEmpty()) return
                    val req = StoreHealthDataRequest(
                        user_id = userid,
                        source = "android",
                        active_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>()
                            .filter { it.record_type == "ActiveEnergyBurned" },
                        basal_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>()
                            .filter { it.record_type == "BasalMetabolic" },
                        distance_walking_running = batch.filterIsInstance<Distance>(),
                        step_count = batch.filterIsInstance<StepCountRequest>(),
                        heart_rate = batch.filterIsInstance<HeartRateRequest>()
                            .filter { it.record_type == "HeartRate" },
                        heart_rate_variability_SDNN = batch.filterIsInstance<HeartRateVariabilityRequest>(),
                        resting_heart_rate = batch.filterIsInstance<HeartRateRequest>()
                            .filter { it.record_type == "RestingHeartRate" },
                        respiratory_rate = batch.filterIsInstance<RespiratoryRate>(),
                        oxygen_saturation = batch.filterIsInstance<OxygenSaturation>(),
                        blood_pressure_systolic = batch.filterIsInstance<BloodPressure>()
                            .filter { it.record_type == "BloodPressureSystolic" },
                        blood_pressure_diastolic = batch.filterIsInstance<BloodPressure>()
                            .filter { it.record_type == "BloodPressureDiastolic" },
                        body_mass = batch.filterIsInstance<BodyMass>(),
                        body_fat_percentage = batch.filterIsInstance<BodyFatPercentage>(),
                        sleep_stage = batch.filterIsInstance<SleepStageJson>(),
                        workout = batch.filterIsInstance<WorkoutRequest>(),
                        time_zone = timeZone
                    )
                    val response =
                        com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient.apiServiceFastApi.storeHealthData(
                            req
                        )
                    if (!response.isSuccessful) {
                        throw Exception("Batch upload failed with code: ${response.code()}")
                    }
                }
                // Loop through all records and split into chunks
                for (record in allRecords) {
                    val json = gson.toJson(record)
                    val size = json.toByteArray().size
                    if (currentSize + size > maxSize) {
                        uploadBatch(currentBatch)
                        currentBatch = mutableListOf()
                        currentSize = 0
                    }
                    currentBatch.add(record)
                    currentSize += size
                }
                // Upload remaining batch
                if (currentBatch.isNotEmpty()) {
                    uploadBatch(currentBatch)
                }
                // ✅ Done, update sync time
                withContext(Dispatchers.Main) {
//                    SharedPreferenceManager.getInstance(this@HomeNewActivity)
//                        .saveMoveRightSyncTime(Instant.now().toString())
                    onSyncCompleteActions()
                    val isValidState = sharedPreferenceManager.challengeState in listOf(3)

                    if (
                        isValidState &&
                        sharedPreferenceManager.challengeParticipatedDate.isNotEmpty() &&
                        DashboardChecklistManager.checklistStatus
                    ) {
                        lifecycleScope.launch {
                            delay(2000)
                            getDailyTasks(DateHelper.getTodayDate())
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@HomeNewActivity,
                        "Exception: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    onSyncCompleteActions()
                }
            }
        }
    }

    private fun storeSamsungHealthData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val timeZone = ZoneId.systemDefault().id
                val userid = SharedPreferenceManager.getInstance(this@HomeNewActivity).userId
                var activeEnergyBurned: List<EnergyBurnedRequest>? = null
                if (activeCalorieBurnedRecord!!.isNotEmpty()) {
                    activeEnergyBurned = activeCalorieBurnedRecord?.mapNotNull { record ->
                        if (record.energy.inKilocalories > 0) {
                            EnergyBurnedRequest(
                                start_datetime = convertToSamsungFormat(record.startTime.toString()),
                                end_datetime = convertToSamsungFormat(record.endTime.toString()),
                                record_type = "ActiveEnergyBurned",
                                unit = "kcal",
                                value = record.energy.inKilocalories.toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    } ?: emptyList()
                } else {
                    activeEnergyBurned = totalCaloriesBurnedRecord?.mapNotNull { record ->
                        if (record.energy.inKilocalories > 0) {
                            EnergyBurnedRequest(
                                start_datetime = convertToSamsungFormat(record.startTime.toString()),
                                end_datetime = convertToSamsungFormat(record.endTime.toString()),
                                record_type = "ActiveEnergyBurned",
                                unit = "kcal",
                                value = record.energy.inKilocalories.toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    } ?: emptyList()
                }
                val basalEnergyBurned = basalMetabolicRateRecord?.map { record ->
                    EnergyBurnedRequest(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BasalMetabolic",
                        unit = "power",
                        value = record.basalMetabolicRate.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val distanceWalkingRunning = distanceRecord?.mapNotNull { record ->
                    if (record.distance.inKilometers > 0) {
                        val km = record.distance.inKilometers
                        val safeKm = if (km.isFinite()) km else 0.0
                        Distance(
                            start_datetime = convertToSamsungFormat(record.startTime.toString()),
                            end_datetime = convertToSamsungFormat(record.endTime.toString()),
                            record_type = "DistanceWalkingRunning",
                            unit = "km",
                            value = String.format(Locale.US, "%.2f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val stepCount = stepsRecord?.mapNotNull { record ->
                    if (record.count > 0) {
                        StepCountRequest(
                            start_datetime = convertToSamsungFormat(record.startTime.toString()),
                            end_datetime = convertToSamsungFormat(record.endTime.toString()),
                            record_type = "StepCount",
                            unit = "count",
                            value = record.count.toString(),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val heartRate = heartRateRecord?.flatMap { record ->
                    record.samples.mapNotNull { sample ->
                        if (sample.beatsPerMinute > 0) {
                            HeartRateRequest(
                                start_datetime = convertToSamsungFormat(record.startTime.toString()),
                                end_datetime = convertToSamsungFormat(record.endTime.toString()),
                                record_type = "HeartRate",
                                unit = "bpm",
                                value = sample.beatsPerMinute.toInt().toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    }
                } ?: emptyList()
                val heartRateVariability = heartRateVariability?.map { record ->
                    HeartRateVariabilityRequest(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "HeartRateVariability",
                        unit = "double",
                        value = record.heartRateVariabilityMillis.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val restingHeartRate = restingHeartRecord?.map { record ->
                    HeartRateRequest(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "RestingHeartRate",
                        unit = "bpm",
                        value = record.beatsPerMinute.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val respiratoryRate = respiratoryRateRecord?.mapNotNull { record ->
                    if (record.rate > 0) {
                        val km = record.rate
                        val safeKm = if (km.isFinite()) km else 0.0
                        RespiratoryRate(
                            start_datetime = convertToSamsungFormat(record.time.toString()),
                            end_datetime = convertToSamsungFormat(record.time.toString()),
                            record_type = "RespiratoryRate",
                            unit = "breaths/min",
                            value = String.format(Locale.US, "%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val oxygenSaturation = oxygenSaturationRecord?.mapNotNull { record ->
                    if (record.percentage.value > 0) {
                        val km = record.percentage.value
                        val safeKm = if (km.isFinite()) km else 0.0
                        OxygenSaturation(
                            start_datetime = convertToSamsungFormat(record.time.toString()),
                            end_datetime = convertToSamsungFormat(record.time.toString()),
                            record_type = "OxygenSaturation",
                            unit = "%",
                            value = String.format(Locale.US, "%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val bloodPressureSystolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BloodPressureSystolic",
                        unit = "millimeterOfMercury",
                        value = record.systolic.inMillimetersOfMercury.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val bloodPressureDiastolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BloodPressureDiastolic",
                        unit = "millimeterOfMercury",
                        value = record.diastolic.inMillimetersOfMercury.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val bodyMass = weightRecord?.mapNotNull { record ->
                    if (record.weight.inKilograms > 0) {
                        val km = record.weight.inKilograms
                        val safeKm = if (km.isFinite()) km else 0.0
                        BodyMass(
                            start_datetime = convertToSamsungFormat(record.time.toString()),
                            end_datetime = convertToSamsungFormat(record.time.toString()),
                            record_type = "BodyMass",
                            unit = "kg",
                            value = String.format(Locale.US, "%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val bodyFatPercentage = bodyFatRecord?.mapNotNull { record ->
                    val km = record.percentage.value
                    val safeKm = if (km.isFinite()) km else 0.0
                    BodyFatPercentage(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BodyFat",
                        unit = "percentage",
                        value = String.format(Locale.US, "%.1f", safeKm),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val sleepStage = sleepSessionRecord?.flatMap { record ->
                    if (record.stages.isEmpty()) {
                        // No stages → return default "sleep"
                        listOf(
                            SleepStageJson(
                                start_datetime = convertToSamsungFormat(record.startTime.toString()),
                                end_datetime = convertToSamsungFormat(record.endTime.toString()),
                                record_type = "Asleep",
                                unit = "stage",
                                value = "Asleep",
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        )
                    } else {
                        // Map actual stages
                        record.stages.mapNotNull { stage ->
                            val stageValue = when (stage.stage) {
                                SleepSessionRecord.STAGE_TYPE_DEEP -> "Deep Sleep"
                                SleepSessionRecord.STAGE_TYPE_LIGHT -> "Light Sleep"
                                SleepSessionRecord.STAGE_TYPE_REM -> "REM Sleep"
                                SleepSessionRecord.STAGE_TYPE_AWAKE -> "Awake"
                                else -> null
                            }
                            stageValue?.let {
                                SleepStageJson(
                                    start_datetime = convertToSamsungFormat(stage.startTime.toString()),
                                    end_datetime = convertToSamsungFormat(stage.endTime.toString()),
                                    record_type = it,
                                    unit = "sleep_stage",
                                    value = it,
                                    source_name = record.metadata.dataOrigin.packageName
                                )
                            }
                        }
                    }
                } ?: emptyList()
                val workout = exerciseSessionRecord?.mapNotNull { record ->
                    val workoutType = when (record.exerciseType) {
                        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "Running"
                        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "Walking"
                        ExerciseSessionRecord.EXERCISE_TYPE_GYMNASTICS -> "Gym"
                        ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT -> "Other Workout"
                        ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "Biking"
                        ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY -> "Biking Stationary"
                        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "Cycling"
                        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> "Swimming"
                        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "Strength Training"
                        ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "Yoga"
                        ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> "HIIT"
                        ExerciseSessionRecord.EXERCISE_TYPE_BADMINTON -> "Badminton"
                        ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL -> "Basketball"
                        ExerciseSessionRecord.EXERCISE_TYPE_BASEBALL -> "Baseball"
                        else -> "Other"
                    }
                    val distance = record.metadata.dataOrigin.let { 5.0 }
                    val safeDistance = if (distance.isFinite()) distance else 0.0
                    WorkoutRequest(
                        start_datetime = convertToSamsungFormat(record.startTime.toString()),
                        end_datetime = convertToSamsungFormat(record.endTime.toString()),
                        source_name = record.metadata.dataOrigin.packageName,
                        record_type = "Workout",
                        workout_type = workoutType,
                        duration = ((record.endTime.toEpochMilli() - record.startTime.toEpochMilli()) / 1000 / 60).toString(),
                        calories_burned = "",
                        distance = String.format(Locale.US, "%.1f", safeDistance),
                        duration_unit = "minutes",
                        calories_unit = "kcal",
                        distance_unit = "km"
                    )
                } ?: emptyList()
                StoreHealthDataRequest(
                    user_id = userid,
                    source = "android",
                    active_energy_burned = activeEnergyBurned,
                    basal_energy_burned = basalEnergyBurned,
                    distance_walking_running = distanceWalkingRunning,
                    step_count = stepCount,
                    heart_rate = heartRate,
                    heart_rate_variability_SDNN = heartRateVariability,
                    resting_heart_rate = restingHeartRate,
                    respiratory_rate = respiratoryRate,
                    oxygen_saturation = oxygenSaturation,
                    blood_pressure_systolic = bloodPressureSystolic,
                    blood_pressure_diastolic = bloodPressureDiastolic,
                    body_mass = bodyMass,
                    body_fat_percentage = bodyFatPercentage,
                    sleep_stage = sleepStage,
                    workout = workout,
                    time_zone = timeZone
                )
                val gson = Gson()
                val allRecords = mutableListOf<Any>()
                allRecords.addAll(activeEnergyBurned)
                allRecords.addAll(basalEnergyBurned)
                allRecords.addAll(distanceWalkingRunning)
                allRecords.addAll(stepCount)
                allRecords.addAll(heartRate)
                allRecords.addAll(heartRateVariability)
                allRecords.addAll(restingHeartRate)
                allRecords.addAll(respiratoryRate)
                allRecords.addAll(oxygenSaturation)
                allRecords.addAll(bloodPressureSystolic)
                allRecords.addAll(bloodPressureDiastolic)
                allRecords.addAll(bodyMass)
                allRecords.addAll(bodyFatPercentage)
                allRecords.addAll(sleepStage)
                allRecords.addAll(workout)

                // Chunk upload variables
                var currentBatch = mutableListOf<Any>()
                var currentSize = 0
                val maxSize = 10 * 1024 * 1024 // 10MB

                suspend fun uploadBatch(batch: List<Any>) {
                    if (batch.isEmpty()) return
                    val req = StoreHealthDataRequest(
                        user_id = userid,
                        source = "android",
                        active_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>()
                            .filter { it.record_type == "ActiveEnergyBurned" },
                        basal_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>()
                            .filter { it.record_type == "BasalMetabolic" },
                        distance_walking_running = batch.filterIsInstance<Distance>(),
                        step_count = batch.filterIsInstance<StepCountRequest>(),
                        heart_rate = batch.filterIsInstance<HeartRateRequest>()
                            .filter { it.record_type == "HeartRate" },
                        heart_rate_variability_SDNN = batch.filterIsInstance<HeartRateVariabilityRequest>(),
                        resting_heart_rate = batch.filterIsInstance<HeartRateRequest>()
                            .filter { it.record_type == "RestingHeartRate" },
                        respiratory_rate = batch.filterIsInstance<RespiratoryRate>(),
                        oxygen_saturation = batch.filterIsInstance<OxygenSaturation>(),
                        blood_pressure_systolic = batch.filterIsInstance<BloodPressure>()
                            .filter { it.record_type == "BloodPressureSystolic" },
                        blood_pressure_diastolic = batch.filterIsInstance<BloodPressure>()
                            .filter { it.record_type == "BloodPressureDiastolic" },
                        body_mass = batch.filterIsInstance<BodyMass>(),
                        body_fat_percentage = batch.filterIsInstance<BodyFatPercentage>(),
                        sleep_stage = batch.filterIsInstance<SleepStageJson>(),
                        workout = batch.filterIsInstance<WorkoutRequest>(),
                        time_zone = timeZone
                    )
                    val response =
                        com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient.apiServiceFastApi.storeHealthData(
                            req
                        )
                    if (!response.isSuccessful) {
                        throw Exception("Batch upload failed with code: ${response.code()}")
                    }
                }
                // Loop through all records and split into chunks
                for (record in allRecords) {
                    val json = gson.toJson(record)
                    val size = json.toByteArray().size
                    if (currentSize + size > maxSize) {
                        uploadBatch(currentBatch)
                        currentBatch = mutableListOf()
                        currentSize = 0
                    }
                    currentBatch.add(record)
                    currentSize += size
                }
                // Upload remaining batch
                if (currentBatch.isNotEmpty()) {
                    uploadBatch(currentBatch)
                }
                // ✅ Done, update sync time
                withContext(Dispatchers.Main) {
//                    SharedPreferenceManager.getInstance(this@HomeNewActivity)
//                        .saveMoveRightSyncTime(Instant.now().toString())
                    onSyncCompleteActions()
                    val isValidState = sharedPreferenceManager.challengeState in listOf(3)

                    if (
                        isValidState &&
                        sharedPreferenceManager.challengeParticipatedDate.isNotEmpty() &&
                        DashboardChecklistManager.checklistStatus
                    ) {
                        lifecycleScope.launch {
                            delay(2000)
                            getDailyTasks(DateHelper.getTodayDate())
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@HomeNewActivity,
                        "Exception: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    onSyncCompleteActions()
                }
            }
        }
    }

    fun convertUtcToInstant(utcString: String): Instant {
        val zonedDateTime = ZonedDateTime.parse(utcString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
        return zonedDateTime.toInstant()
    }

    fun convertToSamsungFormat(input: String): String {
        val possibleFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ssX",         // ISO with timezone
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",     // ISO with milliseconds
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX", // ISO with nanoseconds
            "yyyy-MM-dd HH:mm:ss",            // Common DB format
            "yyyy/MM/dd HH:mm:ss",            // Slash format
            "dd-MM-yyyy HH:mm:ss",            // Day-Month-Year
            "MM/dd/yyyy HH:mm:ss",            // US format
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        )

        // Check if it's a nanosecond timestamp
        if (input.matches(Regex("^\\d{18,}$"))) {
            return try {
                val nanos = input.toLong()
                val seconds = nanos / 1_000_000_000
                val nanoAdjustment = (nanos % 1_000_000_000).toInt()
                val instant = Instant.ofEpochSecond(seconds, nanoAdjustment.toLong())
                val targetFormatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(
                        ZoneOffset.UTC
                    )
                targetFormatter.format(instant)
            } catch (e: Exception) {
                ""
            }
        }

        // Try known patterns
        for (pattern in possibleFormats) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern)

                return if (pattern.contains("X") || pattern.contains("'Z'")) {
                    // Pattern has timezone — parse as instant
                    val temporal = formatter.parse(input)
                    val instant = Instant.from(temporal)
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .withZone(ZoneOffset.UTC)
                        .format(instant)
                } else {
                    // No timezone info — treat as local time and convert to UTC
                    val localDateTime = LocalDateTime.parse(input, formatter)
                    val zonedDateTime = localDateTime.atZone(ZoneId.systemDefault())
                    val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC)
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .withZone(ZoneOffset.UTC)
                        .format(utcDateTime)
                }

            } catch (e: DateTimeParseException) {
                // Try next format
            }
        }

        return "" // Unable to parse
    }

    fun convertToTargetFormat(input: String): String {
        val possibleFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ssX",        // ISO with timezone
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",    // ISO with milliseconds
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX", // ISO with nanoseconds
            "yyyy-MM-dd HH:mm:ss",           // Common DB format
            "yyyy/MM/dd HH:mm:ss",           // Slash format
            "dd-MM-yyyy HH:mm:ss",           // Day-Month-Year
            "MM/dd/yyyy HH:mm:ss",            // US format
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        )

        // Check if it's a nanosecond timestamp (very large number)
        if (input.matches(Regex("^\\d{18,}$"))) {
            return try {
                val nanos = input.toLong()
                val seconds = nanos / 1_000_000_000
                val nanoAdjustment = (nanos % 1_000_000_000).toInt()
                val instant = Instant.ofEpochSecond(seconds, nanoAdjustment.toLong())
                val targetFormatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
                return targetFormatter.format(instant)
            } catch (e: Exception) {
                ""
            }
        }

        // Try known patterns
        for (pattern in possibleFormats) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneOffset.UTC)
                val temporal = formatter.parse(input)
                val instant = Instant.from(temporal)
                val targetFormatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
                return targetFormatter.format(instant)
            } catch (e: DateTimeParseException) {
                // Try next format
            }
        }

        return "" // Unable to parse
    }


    //get planid to send in subscription request

    fun getPlanIdFromPurchase(context: Context, purchase: Purchase?): String? {
        val userProfileResponse = SharedPreferenceManager.getInstance(context).userProfile
            ?: return null // no profile saved

        if (purchase == null || purchase.products.isEmpty()) {
            return null
        }

        val purchasedProductId = purchase.products[0] // usually only 1 product
        var planId: String? = null

        // Check subscription list
        userProfileResponse.subscription?.let { subs ->
            for (sub in subs) {
                if (sub.productId.equals(purchasedProductId, ignoreCase = true)) {
                    planId = sub.planId
                    break
                }
            }
        }

        // If not found in subscription, check booster
        if (planId == null) {
            userProfileResponse.booster?.let { boosters ->
                for (booster in boosters) {
                    if (booster.productId.equals(purchasedProductId, ignoreCase = true)) {
                        planId = booster.planId
                        break
                    }
                }
            }
        }

        return planId
    }

    private fun handleUserSubscriptionStatus(userSubStatus: Int) {
        when (userSubStatus) {
            0 -> {
                //Toast.makeText(this, "Welcome! Start your free trial.", Toast.LENGTH_LONG).show()
                // maybe trigger free trial flow here
                binding.flFreeTrial.visibility = View.VISIBLE
                makeFreeTrialVisible()
                binding.trialExpiredLayout.trialExpiredLayout.visibility = View.GONE
                binding.llFreeTrailExpired.visibility = View.GONE
            }

            1 -> {
                //Toast.makeText(this, "You are subscribed. Enjoy premium features!", Toast.LENGTH_LONG).show()
                binding.flFreeTrial.visibility = View.GONE
                binding.trialExpiredLayout.trialExpiredLayout.visibility = View.GONE
                binding.llFreeTrailExpired.visibility = View.GONE
            }

            2 -> {
                //   Toast.makeText(this, "Your free trial has expired. Subscribe to continue.", Toast.LENGTH_LONG).show()
                // navigate to subscription screen if needed
                binding.flFreeTrial.visibility = View.GONE
                binding.trialExpiredLayout.trialExpiredLayout.visibility = View.VISIBLE
                binding.llFreeTrailExpired.visibility = View.VISIBLE
            }

            3 -> {
                // Toast.makeText(this, "Your subscription has ended. Renew to regain access.", Toast.LENGTH_LONG).show()
                binding.flFreeTrial.visibility = View.GONE
                binding.trialExpiredLayout.trialExpiredLayout.visibility = View.VISIBLE
                binding.llFreeTrailExpired.visibility = View.GONE
            }

            else -> {
                Toast.makeText(
                    this,
                    "Unknown subscription status. Please contact support.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    private fun makeFreeTrialVisible() {
        binding.flFreeTrial.apply {
            visibility = View.VISIBLE        // <-- must be visible
            isClickable = true               // <-- ensures touch can be received
            isFocusable = true
            setOnClickListener {
                Log.d("FreeTrialDebug", "Clicked!") // should appear in Logcat now
                startActivity(Intent(this@HomeNewActivity, BeginMyFreeTrialActivity::class.java))
            }
        }

    }

    // handle user subsciption Status Code Explanation imp Don't delete
    /*USER_SUB_STATUS = 0

    0 - New User(Free Trail Not Started) -  trial starts on API Hit
    1 - SUbribed (Subscribed using payment)
    2 - Free Trial expired
    3 - Subscription Ended*/

    // Trial countdown timer logic
    private var countDownTimer: CountDownTimer? = null

    /**
     * Shows a 7-day reverse countdown in a TextView based on API date.
     * If expired, shows "Trial expired".
     */
    /*private fun showSevenDayCountdown(apiDate: String, textView: TextView) {
        // Cancel any existing timer
        countDownTimer?.cancel()

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = dateFormat.parse(apiDate) ?: return

            // Add 7 days to start date
            val endTime = startDate.time + (7 * 24 * 60 * 60 * 1000L)
            val currentTime = System.currentTimeMillis()
            val timeRemaining = endTime - currentTime

            if (timeRemaining > 0) {
                countDownTimer = object : CountDownTimer(timeRemaining, 1000L) {
                    override fun onTick(millisUntilFinished: Long) {
                        val days = millisUntilFinished / (1000 * 60 * 60 * 24)
                        val hours = (millisUntilFinished % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
                        val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                        val seconds = (millisUntilFinished % (1000 * 60)) / 1000

                        *//*textView.text = String.format(
                            Locale.getDefault(),
                            "%d Days %02d:%02d:%02d",
                            days, hours, minutes, seconds
                        )*//*
                        binding.tvDays.text = String.format(
                            Locale.getDefault(),
                            "%02d",
                            days
                        )
                        binding.tvHours.text = String.format(
                            Locale.getDefault(),
                            "%02d",
                            hours
                        )
                        binding.tvMinutes.text = String.format(
                            Locale.getDefault(),
                            "%02d",
                            minutes
                        )
                    }

                    override fun onFinish() {
                        textView.text = "Trial expired"
                    }
                }.start()
            } else {
                // Already expired
                textView.text = "Trial expired"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            textView.text = "Invalid date"
        }
    }*/


    private fun showSevenDayCountdown(apiDate: String, textView: TextView) {
        // Cancel any existing timer
        countDownTimer?.cancel()

        try {
            val startDateMillis: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // API 26+ → use java.time
                Instant.parse(apiDate).toEpochMilli()
            } else {
                // API < 26 → fallback to SimpleDateFormat
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                dateFormat.parse(apiDate)?.time ?: return
            }

            // Add 7 days to start date
            val endTime = startDateMillis + (7 * 24 * 60 * 60 * 1000L)
            val currentTime = System.currentTimeMillis()
            val timeRemaining = endTime - currentTime

            if (timeRemaining > 0) {
                countDownTimer = object : CountDownTimer(timeRemaining, 1000L) {
                    override fun onTick(millisUntilFinished: Long) {
                        val days = millisUntilFinished / (1000 * 60 * 60 * 24)
                        val hours = (millisUntilFinished % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
                        val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                        (millisUntilFinished % (1000 * 60)) / 1000

                        // Update your UI
                        binding.tvDays.text = String.format(Locale.getDefault(), "%02d", days)
                        binding.tvHours.text = String.format(Locale.getDefault(), "%02d", hours)
                        binding.tvMinutes.text = String.format(Locale.getDefault(), "%02d", minutes)
                        // If you need seconds: binding.tvSeconds.text = "%02d".format(seconds)
                    }

                    override fun onFinish() {
                        //textView.text = "Trial expired"
                        binding.llFreeTrailExpired.visibility = View.VISIBLE
                    }
                }.start()
            } else {
                // Already expired
                //textView.text = "Trial expired"
                binding.llFreeTrailExpired.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //textView.text = "Invalid date"
        }
    }


    // Dashboard Checklist API call
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
                    }
                    isChecklistLoaded = true
                    tryProcessPendingDeepLink()
                }

                override fun onFailure(call: Call<DashboardChecklistResponse>, t: Throwable) {
                    handleNoInternetView(t)
                    isChecklistLoaded = true
                    tryProcessPendingDeepLink()
                }

            })
    }

    //getDashboardChecklist
    private fun getDashboardChecklist() {
        // Make the API call
        val call = apiService.getDashboardChecklist(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    val promotionResponse2 = response.body()!!.string()
                    val gson = Gson()
                    val checklistResponse = gson.fromJson(
                        promotionResponse2, ChecklistResponse::class.java
                    )
                    sharedPreferenceManager.saveChecklistResponse(checklistResponse)
                    handleChecklistResponse(checklistResponse)

                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t) // Print the full stack trace for more details
            }
        })
    }

    private fun handleChecklistResponse(checklistResponse: ChecklistResponse?) {
        checklistCount = 0
        if (checklistResponse != null) {
            if (checklistResponse.data.profile.equals("COMPLETED", ignoreCase = true)) {
                checklistCount++
            }

            if (checklistResponse.data.meal_snap.equals("COMPLETED", ignoreCase = true)) {
                checklistCount++
            }

            if (checklistResponse.data.sync_health_data.equals("COMPLETED", ignoreCase = true)) {
                checklistCount++
            }

            if (checklistResponse.data.vital_facial_scan.equals("COMPLETED", ignoreCase = true)) {
                checklistCount++
            }

            if (checklistResponse.data.unlock_sleep.equals("COMPLETED", ignoreCase = true)) {
                checklistCount++
            }

            if (checklistResponse.data.discover_eating.equals("COMPLETED", ignoreCase = true)) {
                checklistCount++
            }


            checklistResponse.data.snap_mealId.let { snapMealId ->
                if (!snapMealId.isNullOrEmpty()) {
                    sharedPreferenceManager.saveSnapMealId(snapMealId)
                    this.snapMealId = snapMealId
                } else {
                    sharedPreferenceManager.saveSnapMealId("")
                    this.snapMealId = ""
                }
            }
        }

    }

    private fun freeTrialDialogActivity(featureFlag: String = "") {
        val intent = Intent(this, BeginMyFreeTrialActivity::class.java).apply {
            putExtra(FeatureFlags.EXTRA_ENTRY_DEST, featureFlag)
        }
        startActivity(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun myHealthFragmentSelected() {
        // Get the current fragment
        supportFragmentManager.findFragmentById(R.id.fragmentContainer)

        if (binding.includedhomebottomsheet.bottomSheet.visibility == View.VISIBLE) {
            binding.includedhomebottomsheet.bottomSheet.visibility = View.GONE
            binding.includedhomebottomsheet.bottomSheetParent.apply {
                isClickable = false
                isFocusable = false
                visibility = View.GONE
            }

            binding.includedhomebottomsheet.bottomSheetParent.visibility = View.GONE
            binding.includedhomebottomsheet.bottomSheetParent.setBackgroundColor(Color.TRANSPARENT)
            binding.fab.animate().rotationBy(180f).setDuration(60)
                .setInterpolator(DecelerateInterpolator()).withEndAction {
                    // Change icon after rotation
                    if (isAdd) {
                        binding.fab.setImageResource(R.drawable.icon_quicklink_plus_black) // Change to close icon
                        binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.rightlife
                        )
                        binding.fab.imageTintList = ColorStateList.valueOf(
                            resources.getColor(
                                R.color.black
                            )
                        )
                    } else {
                        binding.fab.setImageResource(R.drawable.icon_quicklink_plus) // Change back to add icon
                        binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.white
                        )
                        binding.fab.imageTintList = ColorStateList.valueOf(
                            resources.getColor(
                                R.color.rightlife
                            )
                        )
                    }
                    isAdd = !isAdd // Toggle the state
                }.start()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeDashboardFragment())
                .commit()
            updateMenuSelection(R.id.menu_explore)
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeDashboardFragment())
                .commit()
            updateMenuSelection(R.id.menu_explore)
        }
    }

    fun callSnapMealClick() {

        val userProfile = sharedPreferenceManager.userProfile
        val userStatus = userProfile?.user_sub_status ?: -1
        val freeDate = userProfile?.freeServiceDate.orEmpty()

        when {
            userStatus == 0 -> {
                // Not subscribed
                freeTrialDialogActivity(FeatureFlags.MEAL_SCAN)
            }

            userStatus == 1 && freeDate.isNotEmpty() -> {
                // Free trial active with date
                if (!DashboardChecklistManager.checklistStatus) {
                    logAndOpenMeal(snapMealId)
                } else {
                    logAndOpenMeal("")
                }
            }

            userStatus == 1 && freeDate.isEmpty() -> {
                // Free trial active but no free date
                logAndOpenMeal("")
            }

            userStatus == 2 -> {
                // Free trial expired but has snap meal id
                //logAndOpenMeal(snapMealId)
                checkTrailEndedAndShowDialog()
            }

            userStatus == 3 -> {
                // Subscription ended
                showSubsciptionEndedBottomSheet()
            }

            snapMealId.isNotEmpty() -> {
                // Safety fallback: allow if snapMealId present
                logAndOpenMeal(snapMealId)
            }

            else -> {
                // Final fallback: maybe show dialog
                checkTrailEndedAndShowDialog()
            }
        }

    }


    fun callFaceScanClick() {
        AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.EOS_FACE_SCAN_CLICK)
        if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
            freeTrialDialogActivity(FeatureFlags.FACE_SCAN)
        } else {
            val isFacialScanService = sharedPreferenceManager.userProfile.facialScanService
                ?: false

            if (DashboardChecklistManager.facialScanStatus) {
                startActivity(
                    Intent(
                        this@HomeNewActivity, NewHealthCamReportActivity::class.java
                    )
                )
            } else {
                if (isFacialScanService) {
                    ActivityUtils.startFaceScanActivity(this@HomeNewActivity)
                } else {
                    showSwitchAccountDialog(this@HomeNewActivity, "", "")
                }
            }
        }
    }

    fun callMindAuditClick() {

        if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
            freeTrialDialogActivity(FeatureFlags.FACE_SCAN)
            false // Return false if condition is true and dialog is shown
        } else {
            if (!DashboardChecklistManager.checklistStatus) {
                DialogUtils.showCheckListQuestionCommonDialog(this) {
                    //myHealthFragmentSelected()
                    HandleQuicklinkmenu()
                }
                false
            } else if (sharedPreferenceManager.userProfile?.user_sub_status == 2) {
                showTrailEndedBottomSheet()
                false // Return false if condition is true and dialog is shown
            } else if (sharedPreferenceManager.userProfile?.user_sub_status == 3) {
                showSubsciptionEndedBottomSheet()
                false // Return false if condition is true and dialog is shown
            } else {
                ActivityUtils.startMindAuditActivity(this)
            }
        }
    }

    private fun callMindAuditDeepLinkClick(assessmentType: String) {

        if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
            freeTrialDialogActivity(FeatureFlags.FACE_SCAN)
            false // Return false if condition is true and dialog is shown
        } else {
            if (!DashboardChecklistManager.checklistStatus) {
                DialogUtils.showCheckListQuestionCommonDialog(this) {
                    //myHealthFragmentSelected()
                    HandleQuicklinkmenu()
                }
                false
            } else if (sharedPreferenceManager.userProfile?.user_sub_status == 2) {
                showTrailEndedBottomSheet()
                false // Return false if condition is true and dialog is shown
            } else if (sharedPreferenceManager.userProfile?.user_sub_status == 3) {
                showSubsciptionEndedBottomSheet()
                false // Return false if condition is true and dialog is shown
            } else {
                if (!DashboardChecklistManager.mindAuditStatus) {
                    ActivityUtils.startMindAuditActivity(this)
                } else {
                    startActivity(
                        Intent(
                            this,
                            MASuggestedAssessmentActivity::class.java
                        ).apply { putExtra("SelectedAssessment", assessmentType) })
                }
            }
        }
    }


    fun callLogWaterClick() {
        if (checkTrailEndedAndShowDialog()) {
            ActivityUtils.startEatRightReportsActivity(
                this@HomeNewActivity,
                "LogWaterIntakeEat"
            )
        }
    }

    fun callLogWeightClick() {
        if (checkTrailEndedAndShowDialog()) {
            ActivityUtils.startEatRightReportsActivity(this@HomeNewActivity, "LogWeightEat")
        }
    }

    fun callLogSleepClick() {
        if (checkTrailEndedAndShowDialog()) {
            ActivityUtils.startMoveRightReportsActivity(
                this@HomeNewActivity,
                "SearchActivityLogMove"
            )
        }
    }

    fun callLogFoodClick() {
        if (checkTrailEndedAndShowDialog()) {
            //ActivityUtils.startEatRightReportsActivity(this@HomeNewActivity, "MealLogTypeEat")
            startActivity(Intent(this@HomeNewActivity, MainAIActivity::class.java).apply {
                putExtra("ModuleName", "EatRight")
                putExtra("BottomSeatName", "MealLogTypeEat")
                putExtra("snapMealId", snapMealId)
            })
        }
    }

    fun callLogActivitylick() {
        if (checkTrailEndedAndShowDialog()) {
            ActivityUtils.startMoveRightReportsActivity(
                this@HomeNewActivity,
                "SearchActivityLogMove"
            )
        }
    }

    fun callAIReportCardClick() {
        var dynamicReportId = "" // This Is User ID
        dynamicReportId = SharedPreferenceManager.getInstance(this).userId
        if (dynamicReportId.isEmpty()) {
            // Some error handling if the ID is not available
        } else {
            val intent = Intent(this, AIReportWebViewActivity::class.java).apply {
                // Put the dynamic ID as an extra
                putExtra(AIReportWebViewActivity.EXTRA_REPORT_ID, dynamicReportId)
            }
            startActivity(intent)
        }
    }

    fun callJumpBackIn() {
        startActivity(Intent(this, JumpInBackActivity::class.java))
    }

    private fun logAndOpenMeal(snapId: String) {
        AnalyticsLogger.logEvent(
            this@HomeNewActivity,
            AnalyticsEvent.EOS_SNAP_MEAL_CLICK
        )
        ActivityUtils.startEatRightReportsActivity(
            this@HomeNewActivity,
            "SnapMealTypeEat",
            snapId
        )
    }

    private fun clearUserDataAndFinish() {
        val keysToKeep = setOf(
            SharedPreferenceConstants.ALL_IN_ONE_PLACE,
            SharedPreferenceConstants.AFFIRMATION_CONTEXT_SCREEN,
            SharedPreferenceConstants.BREATH_WORK_CONTEXT_SCREEN,
            SharedPreferenceConstants.FACE_SCAN_CONTEXT_SCREEN,
            SharedPreferenceConstants.JOURNAL_CONTEXT_SCREEN,
            SharedPreferenceConstants.MEAL_SCAN_CONTEXT_SCREEN,
            SharedPreferenceConstants.MIND_AUDIT_CONTEXT_SCREEN,
            SharedPreferenceConstants.MRER_CONTEXT_SCREEN,
            SharedPreferenceConstants.SLEEP_SOUND_CONTEXT_SCREEN,
            SharedPreferenceConstants.TRSR_CONTEXT_SCREEN,
            SharedPreferenceConstants.EAT_RIGHT_CONTEXT_SCREEN,
            SharedPreferenceConstants.MOVE_RIGHT_CONTEXT_SCREEN,
            SharedPreferenceConstants.SLEEP_RIGHT_CONTEXT_SCREEN,
            SharedPreferenceConstants.THINK_RIGHT_CONTEXT_SCREEN,
            SharedPreferenceConstants.RIGHT_LIFE_CONTEXT_SCREEN
        )

        // FIXED: Use the correct SharedPreferences file name
        val sharedPreferences = getSharedPreferences("app_shared_prefs", MODE_PRIVATE)
        removeKeysNotInKeepList(sharedPreferences, keysToKeep)

        val intent = Intent(this, DataControlActivity::class.java)
        startActivity(intent)

        finishAffinity()
    }

    private fun removeKeysNotInKeepList(
        sharedPreferences: SharedPreferences,
        keysToKeep: Set<String>
    ) {
        val editor = sharedPreferences.edit()

        // Get all current preference keys
        val allKeys = sharedPreferences.all.keys

        // Remove keys that are not in the keysToKeep list
        allKeys.forEach { key ->
            if (key !in keysToKeep) {
                editor.remove(key)
            }
        }

        // Explicitly remove access token to ensure it's cleared
        editor.remove(SharedPreferenceConstants.ACCESS_TOKEN)

        editor.apply()
    }


    private fun HandleQuicklinkmenu() {

        // Get the current fragment
        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer)

        // Check if it's HomeExploreFragment
        if (currentFragment is HomeDashboardFragment) {

        } else {


            if (binding.includedhomebottomsheet.bottomSheet.visibility == View.VISIBLE) {
                binding.includedhomebottomsheet.bottomSheet.visibility = View.GONE
                binding.includedhomebottomsheet.bottomSheetParent.apply {
                    isClickable = false
                    isFocusable = false
                    visibility = View.GONE
                }

                binding.includedhomebottomsheet.bottomSheetParent.visibility = View.GONE
                binding.includedhomebottomsheet.bottomSheetParent.setBackgroundColor(Color.TRANSPARENT)
                binding.fab.animate().rotationBy(180f).setDuration(60)
                    .setInterpolator(DecelerateInterpolator()).withEndAction {
                        // Change icon after rotation
                        if (isAdd) {
                            binding.fab.setImageResource(R.drawable.icon_quicklink_plus_black) // Change to close icon
                            binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                                this, R.color.rightlife
                            )
                            binding.fab.imageTintList = ColorStateList.valueOf(
                                resources.getColor(
                                    R.color.black
                                )
                            )
                        } else {
                            binding.fab.setImageResource(R.drawable.icon_quicklink_plus) // Change back to add icon
                            binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                                this, R.color.white
                            )
                            binding.fab.imageTintList = ColorStateList.valueOf(
                                resources.getColor(
                                    R.color.rightlife
                                )
                            )
                        }
                        isAdd = !isAdd // Toggle the state
                    }.start()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeDashboardFragment())
                    .commit()
                updateMenuSelection(R.id.menu_explore)
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeDashboardFragment())
                    .commit()
                updateMenuSelection(R.id.menu_explore)
            }
        }
    }

    // save FCM TOken
    private fun sendTokenToServer(token: String) {
        // Implement API call to send token to your backend
        Log.d("FCM_TOKEN", "Token should be sent to server: $token")
        //sharedPreferenceManager.getString(SharedPreferenceConstants.FCM_TOKEN,token)

        CommonAPICall.sendTokenToServer(
            applicationContext,
            sharedPreferenceManager.getString(SharedPreferenceConstants.FCM_TOKEN, token)
        )
    }

    private fun getSubscriptionList() {
        val call = apiService.getSubscriptionPlanList(
            sharedPreferenceManager.accessToken,
            "SUBSCRIPTION_PLAN"
        )
        call.enqueue(object : Callback<SubscriptionPlansResponse> {
            override fun onResponse(
                call: Call<SubscriptionPlansResponse>,
                response: Response<SubscriptionPlansResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.data?.result?.list?.let {
                        it.forEach { plan ->
                            if (plan.title.equals("Annual", true)) {
                                if (sharedPreferenceManager.userProfile.userdata.country.equals("IN")) {
                                    binding.tvStriketroughPrice.text = "₹ ${plan.price?.inr}"
                                    binding.tvZero.text = "₹0"
                                } else {
                                    binding.tvStriketroughPrice.text = "\$ ${plan.price?.usd}"
                                    binding.tvZero.text = "\$0"
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@HomeNewActivity, response.message(), Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<SubscriptionPlansResponse>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun joinChallenge() {
        AppLoader.show(this)
        apiService.postChallengeStart(sharedPreferenceManager.accessToken)
            .enqueue(object : Callback<CommonResponse> {
                override fun onResponse(
                    call: Call<CommonResponse?>,
                    response: Response<CommonResponse?>
                ) {
                    AppLoader.hide(this@HomeNewActivity)
                    if (response.isSuccessful) {
                        getChallengeStatus()
                        showCustomToast(response.body()?.successMessage ?: "", true)
                        AnalyticsLogger.logEvent(
                            this@HomeNewActivity,
                            AnalyticsEvent.ChlCard_Join_Tap
                        )
                    } else {
                        showCustomToast("Something went wrong!", false)
                    }
                }

                override fun onFailure(
                    call: Call<CommonResponse?>,
                    t: Throwable
                ) {
                    AppLoader.hide(this@HomeNewActivity)
                    handleNoInternetView(t)
                }

            })
    }

    private fun getChallengeStatus() {
        AppLoader.show(this)
        apiService.getChallengeStart(sharedPreferenceManager.accessToken)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    AppLoader.hide(this@HomeNewActivity)
                    if (response.isSuccessful && response.body() != null) {
                        val gson = Gson()
                        val jsonResponse = response.body()?.string()
                        val responseObj =
                            gson.fromJson(jsonResponse, ChallengeDateResponse::class.java)

                        val dates = responseObj.data
                        hideChallengeLayout()
                        // challenge related stuff
                        setChallengeLayout(dates)
                        handleChallengeUI(dates)
                    }
                }

                override fun onFailure(
                    call: Call<ResponseBody?>,
                    t: Throwable
                ) {
                    AppLoader.hide(this@HomeNewActivity)
                    handleNoInternetView(t)
                }

            })
    }

    private fun handleChallengeUI(dates: ChallengeDateData) {

        // Hide all layouts first to avoid overlapping UI
        hideChallengeLayout()

        val dateRange = getChallengeDateRange(
            dates.challengeStartDate,
            dates.challengeEndDate
        )

        sharedPreferenceManager.challengeState = dates.challengeStatus //3
        sharedPreferenceManager.challengeStartDate =
            dates.challengeStartDate //"12 Jan 2026, 09:00 PM"
        sharedPreferenceManager.challengeEndDate = dates.challengeEndDate //"28 Jan 2026, 09:00 PM"
        sharedPreferenceManager.challengeParticipatedDate = dates.participateDate

        if (dates.participateDate.isEmpty()) {

            if (dates.challengeStatus != 4) {
                binding.layoutRegisterChallenge.registerChallengeCard.visibility = View.VISIBLE
                binding.layoutRegisterChallenge.tvStartEndDate.text = dateRange
            } else {
                hideChallengeLayout()
            }

            return
        }

        when (sharedPreferenceManager.challengeState) {

            1 -> {
                if (dates.participateDate.isEmpty()) {
                    // Join challenge
                    binding.layoutRegisterChallenge.tvStartEndDate.text = dateRange
                    binding.layoutRegisterChallenge.registerChallengeCard.visibility = View.VISIBLE
                } else {
                    binding.layoutUnlockChallenge.tvStartEndDate.text =
                        getChallengeDateRange(
                            dates.challengeStartDate,
                            dates.challengeEndDate
                        )
                    dates.challengeLiveDate.let {
                        binding.layoutUnlockChallenge.tvChallengeLiveDate.text =
                            formatWithOrdinal(it)
                    }
                    binding.layoutUnlockChallenge.unlockChallengeCard.visibility =
                        View.VISIBLE
                }
            }

            2 -> {
                // Waiting for challenge
                if (!DashboardChecklistManager.checklistStatus) {
                    // Checklist not completed
                    binding.layoutChallengeToCompleteChecklist.apply {
                        tvChecklistNumber.text = "$checklistCount/6"
                        seekBar.progress = checklistCount.takeIf { it != 0 }?.times(10) ?: 1
                        imgChallenge.imageTintList = ColorStateList.valueOf("#F5B829".toColorInt())
                        tvChallenge.setTextColor("#F5B829".toColorInt())
                        tvChallenge.text = "Challenge Upcoming"
                        completeChallengeChecklist.visibility = View.VISIBLE
                    }
                } else {
                    // Checklist completed → Countdown card
                    binding.layoutChallengeCountDownDays.countDownTimeChallengeCard.visibility =
                        View.VISIBLE
                    val daysMin =
                        getDaysFromToday(sharedPreferenceManager.challengeStartDate).split(" ")
                    binding.layoutChallengeCountDownDays.tvCountDownDays.text = daysMin[0]
                    binding.layoutChallengeCountDownDays.tvDaysToGo.text = "${daysMin[1]} to go"

                }
            }

            3 -> {
                // Active challenge → Daily score
                if (!DashboardChecklistManager.checklistStatus) {
                    // Checklist not completed
                    binding.layoutChallengeToCompleteChecklist.apply {
                        tvChecklistNumber.text = "$checklistCount/6"
                        seekBar.progress = checklistCount.takeIf { it != 0 }?.times(10) ?: 1
                        imgChallenge.imageTintList = ColorStateList.valueOf("#06B27B".toColorInt())
                        tvChallenge.setTextColor("#06B27B".toColorInt())
                        tvChallenge.text = "Challenge Active"
                        completeChallengeChecklist.visibility = View.VISIBLE
                    }
                } else {
                    //Show Score Card
                    binding.layoutChallengeDailyScore.dailyScoreChallengeCard.visibility =
                        View.VISIBLE
                    getDailyTasks(DateHelper.getTodayDate())
                }
            }

            4 -> {
                // Challenge completed
                binding.layoutChallengeCompleted.challengeCompleted.visibility =
                    View.VISIBLE
            }
        }
    }


    private fun formatWithOrdinal(dateStr: String): String {
        val inputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        val date = inputFormat.parse(dateStr)

        val calendar = Calendar.getInstance()
        calendar.time = date!!

        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = SimpleDateFormat("MMM", Locale.ENGLISH).format(date)
        val year = SimpleDateFormat("yyyy", Locale.ENGLISH).format(date)

        return "$day${getDaySuffix(day)} $month $year"
    }


    fun hideChallengeLayout() {
        binding.layoutRegisterChallenge.registerChallengeCard.visibility = View.GONE
        binding.layoutUnlockChallenge.unlockChallengeCard.visibility = View.GONE
        binding.layoutChallengeToCompleteChecklist.completeChallengeChecklist.visibility = View.GONE
        binding.layoutChallengeCountDownDays.countDownTimeChallengeCard.visibility = View.GONE
        binding.layoutChallengeDailyScore.dailyScoreChallengeCard.visibility = View.GONE
        binding.layoutChallengeCompleted.challengeCompleted.visibility = View.GONE
    }

    fun showChallengeCard() {
        getDashboardChecklist()
        try {
            if (!sharedPreferenceManager.appConfigJson.isNullOrBlank()) {
                val appConfig =
                    Gson().fromJson(
                        sharedPreferenceManager.appConfigJson,
                        AppConfigResponse::class.java
                    )
                if (appConfig?.data?.isChallengeStart == true) {
                    binding.flFreeTrial.visibility = View.GONE
                    binding.trialExpiredLayout.trialExpiredLayout.visibility = View.GONE
                    binding.llFreeTrailExpired.visibility = View.GONE
                    getChallengeStatus()
                } else {
                    hideChallengeLayout()
                }
            }
        } catch (e: Exception) {
            Log.e("AppConfig", "Failed to parse app config from SharedPreferences", e)
        }
    }

    private fun setChallengeLayout(dates: ChallengeDateData) {
        //Register Challenge
        binding.layoutRegisterChallenge.imgInfoChallege.setOnClickListener {
            showChallengeInfoBottomSheet(this@HomeNewActivity)
        }
        binding.layoutRegisterChallenge.btnJoin.setOnClickListener {
            lifecycleScope.launch { joinChallenge() }
        }

        //Unlock challenge
        binding.layoutUnlockChallenge.imgInfoChallege.setOnClickListener {
            showChallengeInfoBottomSheet(this@HomeNewActivity)
        }

        //Challenge Complete Checklist
        binding.layoutChallengeToCompleteChecklist.imgInfoChallege.setOnClickListener {
            showChallengeInfoBottomSheet(this@HomeNewActivity)
        }
        binding.layoutChallengeToCompleteChecklist.btnCompleteChecklist.setOnClickListener {
            myHealthFragmentSelected()

            AnalyticsLogger.logEvent(
                this@HomeNewActivity,
                AnalyticsEvent.Chl_CompleteChecklist
            )
        }

        //Challenge CountDownDays
        binding.layoutChallengeCountDownDays.imgInfoChallege.setOnClickListener {
            showChallengeInfoBottomSheet(this@HomeNewActivity)
        }
        binding.layoutChallengeCountDownDays.btnViewChallenge.setOnClickListener {
            startActivity(Intent(this@HomeNewActivity, ChallengeEmptyActivity::class.java))
        }

        //Challenge Daily Score
        binding.layoutChallengeDailyScore.dailyScoreChallengeCard.visibility =
            View.VISIBLE
        binding.layoutChallengeDailyScore.tvResync.setOnClickListener {
            it.disableViewForSeconds()
            fetchHealthDataFromHealthConnect()
        }
        binding.layoutChallengeDailyScore.imgForwardChallenge.setOnClickListener {
            startActivity(
                Intent(this@HomeNewActivity, ChallengeActivity::class.java)
                    .putExtra("SYNC_STATUS", syncStatus)
            )

            AnalyticsLogger.logEvent(
                this@HomeNewActivity,
                AnalyticsEvent.Chl_DailyScoreCard_Tap
            )

        }

        //challenge completed
        binding.layoutChallengeCompleted.imgScoreChallenge.setOnClickListener {
            // start Challenge Activity here
            startActivity(
                Intent(this@HomeNewActivity, ChallengeActivity::class.java)
                    .putExtra("SYNC_STATUS", syncStatus)
            )
        }
        binding.layoutChallengeCompleted.btnExplorePlans.setOnClickListener {
            startActivity(
                Intent(
                    this@HomeNewActivity,
                    PurchasePlansActivity::class.java
                )
            )
        }
    }

    private fun getDailyTasks(date: String) {
        // AppLoader.show(this)
        apiService.dailyTask(sharedPreferenceManager.accessToken, date)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody?>, response: Response<ResponseBody?>
                ) {
                    //AppLoader.hide(this@HomeNewActivity)

                    getDailyScore(date)
                }

                override fun onFailure(
                    call: Call<ResponseBody?>, t: Throwable
                ) {
                    // AppLoader.hide(this@HomeNewActivity)
                    handleNoInternetView(t)
                }

            })
    }

    private fun getDailyScore(date: String) {
        //  AppLoader.show(this)
        apiService.dailyScore(sharedPreferenceManager.accessToken, date)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody?>, response: Response<ResponseBody?>
                ) {
                    //AppLoader.hide(this@HomeNewActivity)
                    if (response.isSuccessful && response.body() != null) {
                        val gson = Gson()
                        val jsonResponse = response.body()?.string()
                        val responseObj =
                            gson.fromJson(jsonResponse, DailyScoreResponse::class.java)
                        val scoreData = responseObj.data
                        binding.layoutChallengeDailyScore.apply {
                            tvPoints.text = scoreData.totalScore.toString()
                            scoreSeekBar.progress = scoreData.totalScore.takeIf { it != 0 } ?: 2
                            setSeekBarProgressColor(
                                scoreSeekBar, getColorCode(scoreData.performance)
                            )
                            imgScoreColor.imageTintList = ColorStateList.valueOf(
                                Color.parseColor(
                                    getColorCode(
                                        scoreData.performance
                                    )
                                )
                            )
                            tvScoreLabel.text = scoreData.performance
                            tvMessage.text = scoreData.message
                            binding.layoutChallengeDailyScore.dailyScoreChallengeCard.visibility =
                                View.VISIBLE
                        }
                    } else {
                        showCustomToast("Something went wrong!", false)
                    }
                }

                override fun onFailure(
                    call: Call<ResponseBody?>, t: Throwable
                ) {
                    // AppLoader.hide(this@HomeNewActivity)
                    handleNoInternetView(t)
                }

            })
    }

    private fun showGoToSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Notifications")
            .setMessage("Notifications are disabled. Please enable them from Settings.")
            .setCancelable(false)
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isNotificationPermanentlyDenied(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
    }


    private fun showCompactSyncView() {
        // isSyncing.value = true
        syncStatus = true
        binding.compactSyncIndicator.apply {
            visibility = View.VISIBLE
            alpha = 0f
            scaleX = 0.6f
            scaleY = 0.6f
            animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(400)
                .setInterpolator(OvershootInterpolator(1.5f))
                .start()
        }
        startHeartPulse(binding.compactHeartIcon, false)
    }

    private fun onSyncComplete() {
        // 1. Define colors for Success State
        //isSyncing.value = false
        syncStatus = false
        val colorGreen = ContextCompat.getColor(this, R.color.color_green)
        val colorStateList = ColorStateList.valueOf(colorGreen)
        val colorRed = ContextCompat.getColor(this, R.color.red)
        val colorStateListRed = ColorStateList.valueOf(colorRed)


        // --- Compact View Completion ---
        compactHeartAnimator?.cancel()

        binding.compactSyncIndicator.apply {
            visibility = View.VISIBLE
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
        }

        binding.compactRotatingArc.visibility = View.GONE
        binding.compactHeartIcon.apply {
            imageTintList = colorStateList
            scaleX = 1f
            scaleY = 1f
        }

        // 3. Auto-hide with Shrink animation after 2.5 seconds
        binding.root.postDelayed({
            binding.compactSyncIndicator.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setDuration(400)
                .withEndAction {
                    binding.compactSyncIndicator.visibility = View.GONE
                    binding.compactRotatingArc.visibility = View.VISIBLE
                    binding.compactHeartIcon.apply {
                        imageTintList = colorStateListRed
                        scaleX = 0f
                        scaleY = 0f
                    }
                }
                .start()
        }, 2500)
    }

    private fun onSyncCompleteActions() {
        if (sharedPreferenceManager.isNewUser) {
            syncStatus = false
            updateSync(isLoading = false, isCompleted = true)
            sharedPreferenceManager.isNewUser = false
        } else {
            onSyncComplete()
        }
        updateResyncTextView(false)
        SyncManager.completeHealthSync(this)
    }

    private fun updateResyncTextView(isSyncing: Boolean) {
        binding.layoutChallengeDailyScore.tvResync.isEnabled = !isSyncing
        val tvResync = binding.layoutChallengeDailyScore.tvResync

        // Prevent updating detached view
        if (!tvResync.isAttachedToWindow) return

        val color = if (isSyncing)
            R.color.gray_past_price
        else
            R.color.red

        val background = if (isSyncing)
            R.drawable.rounded_corner_article_gray
        else
            R.drawable.rounded_red_border

        tvResync.setTextColor(ContextCompat.getColor(this, color))
        tvResync.compoundDrawableTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, color))
        tvResync.background = ContextCompat.getDrawable(this, background)
    }

}
