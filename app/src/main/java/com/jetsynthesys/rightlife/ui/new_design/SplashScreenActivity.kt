package com.jetsynthesys.rightlife.ui.new_design

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.VideoView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.messaging.FirebaseMessaging
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.apimodel.appconfig.AppConfigResponse
import com.jetsynthesys.rightlife.databinding.BottomsheetMaintenanceBinding
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.ui.deeplink.DeepLinkTarget.Companion.EXTRA_DEEP_LINK_TARGET
import com.jetsynthesys.rightlife.ui.new_design.pojo.LoggedInUser
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam

class SplashScreenActivity : BaseActivity() {

    private lateinit var videoView: VideoView
    private lateinit var rlview1: RelativeLayout
    private lateinit var imgview2: ImageView
    private val SPLASH_DELAY: Long = 3000 // 3 seconds
    private var appConfig: AppConfigResponse? = null
    private var configCallDone: Boolean = false
    private var isNextActivityStarted = false
    private var deepLink = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContentView(R.layout.activity_splash_screen)
        videoView = findViewById(R.id.videoView)
        rlview1 = findViewById(R.id.rlview1)
        imgview2 = findViewById(R.id.imgview2)
        fetchAppConfig()

        deepLink = extractDeepLinkFromIntent(intent) ?: ""

        /*try {
            if (!sharedPreferenceManager.appConfigJson.isNullOrBlank()) {
                appConfig =
                    com.google.gson.Gson().fromJson(sharedPreferenceManager.appConfigJson, AppConfigResponse::class.java)
                val maintenance = appConfig?.data?.maintenance?.android
                if (maintenance?.enabled == true) {
                    //showMaintenanceBottomSheet(maintenance.message)
                } else {
                    isNextActivityStarted = true
                    startNextActivity()
                }
            }
        } catch (e: Exception) {
            showCustomToast("Please check your internet connection and try again !!", false)
        }
*/
        // Need this Dark Mode selection logic for next phase
        /*val appMode = sharedPreferenceManager.appMode
        if (appMode.equals("System", ignoreCase = true)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else if (appMode.equals("Dark", ignoreCase = true)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }*/
        logCurrentFCMToken()
    }

    private fun extractDeepLinkFromIntent(intent: Intent?): String? {
        if (intent == null) return null

        // 1) If notification click opened via ACTION_VIEW / deep link URI

        intent.dataString?.let { if (it.isNotBlank()) return it }

        val extras = intent.extras ?: return null
        // 2) Direct key from your own notification builder / backend
        extras.getString("deep_link")?.let { if (it.isNotBlank()) return it }

        // 3) Firebase Console / notification payload often prefixes keys like this
        extras.getString("gcm.notification.deep_link")?.let { if (it.isNotBlank()) return it }
        extras.getString("gcm.n.deep_link")?.let { if (it.isNotBlank()) return it }
        return null
    }

    private fun fetchAppConfig() {
        // apiService is available from BaseActivity in your project (as you already use elsewhere)
        val call = apiService.getAppConfig()
        call.enqueue(object : retrofit2.Callback<okhttp3.ResponseBody?> {

            override fun onResponse(
                call: retrofit2.Call<okhttp3.ResponseBody?>,
                response: retrofit2.Response<okhttp3.ResponseBody?>
            ) {
                configCallDone = true

                if (response.isSuccessful && response.body() != null) {
                    try {
                        val json = response.body()?.string() ?: ""
                        appConfig =
                            com.google.gson.Gson().fromJson(json, AppConfigResponse::class.java)

                        // OPTIONAL: store raw json if you want (only if you already have a pref method)
                        sharedPreferenceManager.saveAppConfigJson(json)
                        val maintenance = appConfig?.data?.maintenance?.android
                        if (maintenance?.enabled == true) {
                            showMaintenanceBottomSheet(maintenance.message)
                         } else {
                            appConfig?.data?.forceUpdate?.let { fu ->
                                sharedPreferenceManager.saveForceUpdateConfig(
                                    fu.enabled == true,
                                    fu.minAndroidVersion ?: "",
                                    fu.updateAndroidUrl ?: "",
                                    fu.message ?: ""
                                )
                                if (!isNextActivityStarted)
                                    startNextActivity()
                            }
                        }
                    } catch (e: Exception) {
                        appConfig = null
                        if (sharedPreferenceManager.appConfigJson.isNullOrBlank())
                            showCustomToast(
                                "Please check your internet connection and try again !!",
                                false
                            )
                    }
                } else {
                    appConfig = null
                    if (sharedPreferenceManager.appConfigJson.isNullOrBlank())
                        showCustomToast(
                            "Please check your internet connection and try again !!",
                            false
                        )
                }
            }

            override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody?>, t: Throwable) {
                configCallDone = true
                appConfig = null
                // Don't block splash just because config failed
                // If you want, log it:
                if (sharedPreferenceManager.appConfigJson.isNullOrBlank())
                    showCustomToast("Please check your internet connection and try again !!", false)
                Log.e("SplashConfig", "Config API failed: ${t.message}")
            }
        })
    }


    private fun logCurrentFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result
                //Log.d("FCM_TOKEN", "Current token: $token")
                //Log.d("FCM_TOKEN", "Token length: ${token?.length}")
            } else {
                // Log.e("FCM_TOKEN", "Failed to get token", task.exception)
            }
        }.addOnFailureListener { exception ->
            //Log.e("FCM_TOKEN", "Token retrieval failed: ${exception.message}", exception)
        }
    }

    private fun animateViews() {
        val view1: View = findViewById(R.id.rlview1)
        val view2: View = findViewById(R.id.imgview2)
        // Fade out view1 and fade in view2
        view1.animate()
            .alpha(0.9f) // Fade out
            .setDuration(2000) // Animation duration in ms
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view1.visibility = View.GONE // Hide view1 after animation
                    view2.visibility = View.VISIBLE // Show view2 before animation
                    view2.alpha = 0f // Set initial alpha for fade in

                    view2.animate()
                        .alpha(1f) // Fade in
                        .setDuration(1000) // Animation duration in ms
                        .setListener(null)
                }
            })
    }

    private fun startNextActivity() {
        val authToken = sharedPreferenceManager.accessToken
        // Delay the transition to the next activity to allow the video to end properly
        Handler(Looper.getMainLooper()).postDelayed({
            if (authToken.isEmpty()) {
                AnalyticsLogger.logEvent(
                    AnalyticsEvent.SPLASH_SCREEN_FIRST_OPEN, mapOf(
                        AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
                    )
                )
                val intent = Intent(this, DataControlActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                var email = ""
                email = try {
                    sharedPreferenceManager.userProfile.userdata.email
                } catch (e: NullPointerException) {
                    sharedPreferenceManager.email
                }

                var loggedInUser: LoggedInUser? = null
                for (user in sharedPreferenceManager.loggedUserList) {
                    if (email == user.email) {
                        loggedInUser = user
                    }
                }

                email = try {
                    sharedPreferenceManager.userProfile.userdata.phoneNumber
                } catch (e: NullPointerException) {
                    sharedPreferenceManager.email
                }
                if (loggedInUser == null) {
                    for (user in sharedPreferenceManager.loggedUserList) {
                        if (email == user.email) {
                            loggedInUser = user
                        }
                    }
                }

                AnalyticsLogger.logEvent(
                    this,
                    AnalyticsEvent.SPLASH_SCREEN_OPEN, mapOf(
                        AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                        AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                    )
                )

                if (loggedInUser?.isOnboardingComplete == true) {
                    val intent = Intent(this, HomeNewActivity::class.java)
                    intent.putExtra(EXTRA_DEEP_LINK_TARGET, removeBaseUrl(deepLink))
                    startActivity(intent)
                } else {
                    if (!sharedPreferenceManager.createUserName) {
                        val intent = Intent(this, CreateUsernameActivity::class.java)
                        startActivity(intent)
                    } else if (sharedPreferenceManager.selectedWellnessFocus.isNullOrEmpty()
                        || sharedPreferenceManager.wellnessFocusTopics.isNullOrEmpty()
                        || !sharedPreferenceManager.unLockPower
                        || !sharedPreferenceManager.thirdFiller
                        || !sharedPreferenceManager.interest
                    ) {
                        val intent = Intent(this, WellnessFocusListActivity::class.java)
                        startActivity(intent)
                    } else if (!sharedPreferenceManager.allowPersonalization) {
                        val intent = Intent(this, PersonalisationActivity::class.java)
                        startActivity(intent)
                    } else if (!sharedPreferenceManager.onBoardingQuestion) {
                        val intent = Intent(this, OnboardingQuestionnaireActivity::class.java)
                        startActivity(intent)
                    } else if (!sharedPreferenceManager.enableNotification) {
                        val intent = Intent(this, EnableNotificationActivity::class.java)
                        startActivity(intent)
                    } else if (!sharedPreferenceManager.syncNow) {
                        val intent = Intent(this, SyncNowActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, FreeTrialServiceActivity::class.java)
                        startActivity(intent)
                    }
                }
                finish()

            }
        }, SPLASH_DELAY)

        animateViews()
    }

    fun removeBaseUrl(url: String): String {
        return url
            .replace("https://qa.rightlife.com", "")
            .replace("https://app.rightlife.com", "")
    }

    private fun showMaintenanceBottomSheet(message: String) {
        val bottomSheetDialog = BottomSheetDialog(this)
        // Inflate the BottomSheet layout
        val binding = BottomsheetMaintenanceBinding.inflate(LayoutInflater.from(this))
        val bottomSheetView = binding.root
        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setCanceledOnTouchOutside(false)

        // ✅ Set dim background manually for safety
        bottomSheetDialog.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.dimAmount = 0.7f // 0.0 to 1.0
            window.attributes = layoutParams
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        bottomSheetDialog.show()

        binding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        binding.tvDescription.text = message

        binding.btnOkay.setOnClickListener {
            finishAffinity()
        }
    }


}