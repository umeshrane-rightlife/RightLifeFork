package com.jetsynthesys.rightlife.ui.new_design

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.apimodel.SignupOtpRequest
import com.jetsynthesys.rightlife.apimodel.SubmitLoginOtpRequest
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse
import com.jetsynthesys.rightlife.databinding.ActivityMobileLoginBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetSwitchAccountBinding
import com.jetsynthesys.rightlife.setHintSize
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.ui.ActivityUtils
import com.jetsynthesys.rightlife.ui.new_design.pojo.GoogleLoginTokenResponse
import com.jetsynthesys.rightlife.ui.new_design.pojo.LoggedInUser
import com.jetsynthesys.rightlife.ui.profile_new.pojo.OtpRequest
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.AppSignatureHelper
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceConstants
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.jetsynthesys.rightlife.ui.utility.isValidIndianMobile
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MobileLoginActivity : BaseActivity() {

    private lateinit var colorStateListSelected: ColorStateList
    private lateinit var colorStateListNonSelected: ColorStateList

    // Binding variable
    private lateinit var binding: ActivityMobileLoginBinding

    // OTP / timer
    private var timer: CountDownTimer? = null
    private var failedOtpAttempts = 0
    private var lockedUntil: Long = 0L
    private val otpLength = 6

    // State
    private var phoneNumber: String = ""
    private var isNewUser = false

    // SMS Consent Launcher
    private val smsConsentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
            message?.let {
                val otp = Regex("\\b(\\d{6})\\b").find(it)?.value
                if (!otp.isNullOrEmpty()) {
                    fillOtpFromSms(otp)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMobileLoginBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        startSmsListener()
        // ADD THIS
        val appSignatureHelper = AppSignatureHelper(this)
        val hashes = appSignatureHelper.getAppSignatures()
        Log.d(TAG, "=== Your App Hash ===")
        hashes.forEach { hash ->
            Log.d(TAG, "App Hash: $hash")
        }
        Log.d(TAG, "SMS Hash: s11OcVsGU+c")
        Log.d(TAG, "Hashes Match: ${hashes.contains("s11OcVsGU+c")}")

        colorStateListSelected = ContextCompat.getColorStateList(this, R.color.menuselected)!!
        colorStateListNonSelected = ContextCompat.getColorStateList(this, R.color.rightlife)!!
        setupPhoneValidation()
        setupListeners()
        setupOtpInputs()

        binding.etPhone.setHintSize("Enter your mobile number here", 14)

        binding.layoutOtpScreen.visibility = View.GONE
        binding.btnGetOtp.isEnabled = false
        binding.btnGetOtp.backgroundTintList = colorStateListNonSelected
        binding.btnVerifyOtp.isEnabled = false
        binding.btnVerifyOtp.backgroundTintList = colorStateListNonSelected
        binding.tvValidationError.visibility = View.INVISIBLE
        binding.tvOtpError.visibility = View.GONE

        AnalyticsLogger.logEvent(
            AnalyticsEvent.LOGIN_SCREEN_VISIT,
            mapOf(AnalyticsParam.TIMESTAMP to System.currentTimeMillis())
        )
    }

    // -------------------------------------------------------------------
    // PHONE ENTRY
    // -------------------------------------------------------------------

    private fun setupPhoneValidation() {
        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val valid = isValidIndianMobile(s.toString())
                binding.btnGetOtp.isEnabled = valid
                if (valid) {
                    binding.tvValidationError.visibility = View.INVISIBLE
                    binding.btnGetOtp.isEnabled = true

                    binding.btnGetOtp.backgroundTintList =
                        if (valid) colorStateListSelected else colorStateListNonSelected
                } else {
                    if (s?.length == 10)
                        binding.tvValidationError.visibility = View.VISIBLE
                    else
                        binding.tvValidationError.visibility = View.INVISIBLE
                    binding.tvValidationError.text = "Enter a valid phone number."
                    binding.btnGetOtp.isEnabled = false
                }
                binding.btnGetOtp.backgroundTintList =
                    if (valid) colorStateListSelected else colorStateListNonSelected
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            if (binding.layoutOtpScreen.isVisible) {
                showPhoneEntry()
            } else {
                finish()
            }
        }

        binding.btnGetOtp.setOnClickListener {
            phoneNumber = binding.etPhone.text.toString().trim()

            if (!isValidIndianMobile(phoneNumber)) {
                binding.tvValidationError.visibility = View.VISIBLE
                binding.tvValidationError.text = "Enter a valid 10-digit phone number"
                return@setOnClickListener
            }

            AnalyticsLogger.logEvent(
                AnalyticsEvent.GET_OTP_TAP,
                mapOf(
                    AnalyticsParam.PHONE_NUMBER to phoneNumber,
                    AnalyticsParam.DEVICE_NAME to "Android",
                    AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
                )
            )

            //requestOtp(phoneNumber)
            fetchDeviceInfo(phoneNumber)
            //generateOtp("+91$phoneNumber")
            binding.tvMobileNumber.text = phoneNumber
        }




        binding.btnVerifyOtp.setOnClickListener {
            val otp = getEnteredOtp()
            if (otp.length == otpLength) {
                verifyOtp(phoneNumber, otp)
            }

            AnalyticsLogger.logEvent(
                AnalyticsEvent.VERIFY_OTP_TAP,
                mapOf(
                    AnalyticsParam.PHONE_NUMBER to phoneNumber,
                    AnalyticsParam.DEVICE_NAME to "Android",
                    AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
                )
            )
        }

        binding.tvResend.setOnClickListener {
            if (binding.tvResend.isClickable && binding.tvResend.text.toString()
                    .startsWith("Resend")
            ) {
                AnalyticsLogger.logEvent(
                    AnalyticsEvent.RESEND_OTP_TAP,
                    mapOf(
                        AnalyticsParam.PHONE_NUMBER to phoneNumber,
                        AnalyticsParam.DEVICE_NAME to "Android",
                        AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
                    )
                )
                requestOtp(phoneNumber)
            }
        }

        binding.changeNumber.setOnClickListener {
            showPhoneEntry()
        }
    }

    private fun showPhoneEntry() {
        binding.layoutPhoneEntry.visibility = View.VISIBLE
        binding.layoutOtpScreen.visibility = View.GONE
        binding.tvOtpError.visibility = View.GONE
        clearOtpBoxes()
        timer?.cancel()
        binding.tvResendCounter.visibility = View.GONE
        binding.tvValidationError.visibility = View.GONE
    }

    // -------------------------------------------------------------------
    // OTP INPUT HANDLING (6 BOXES)
    // -------------------------------------------------------------------

    private fun setupOtpInputs() {
        val boxes = listOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )

        boxes.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (index < boxes.size - 1) {
                            boxes[index + 1].requestFocus()
                        } else {
                            editText.clearFocus()
                            hideKeyboard(editText)
                        }
                    }
                    validateOtpReady()
                    binding.tvOtpError.visibility = View.GONE
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editText.text.isEmpty() && index > 0) {
                        boxes[index - 1].requestFocus()
                        boxes[index - 1].setText("")
                    }
                    setOtpBoxesBackground(R.drawable.bg_otp_default)
                }
                false
            }
        }
    }

    private fun setOtpBoxesBackground(drawableResource: Int) {
        val boxes = listOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )

        boxes.forEach { editText ->
            editText.setBackgroundResource(drawableResource)
        }
    }

    private fun clearOtpBoxes() {
        binding.etOtp1.text.clear()
        binding.etOtp2.text.clear()
        binding.etOtp3.text.clear()
        binding.etOtp4.text.clear()
        binding.etOtp5.text.clear()
        binding.etOtp6.text.clear()
        binding.btnVerifyOtp.isEnabled = false
        binding.btnVerifyOtp.backgroundTintList = colorStateListNonSelected
    }

    private fun getEnteredOtp(): String {
        return binding.etOtp1.text.toString() +
                binding.etOtp2.text.toString() +
                binding.etOtp3.text.toString() +
                binding.etOtp4.text.toString() +
                binding.etOtp5.text.toString() +
                binding.etOtp6.text.toString()
    }

    private fun validateOtpReady() {
        binding.btnVerifyOtp.isEnabled = getEnteredOtp().length == otpLength
        binding.btnVerifyOtp.backgroundTintList =
            if (getEnteredOtp().length == otpLength) colorStateListSelected else colorStateListNonSelected
    }

    private fun fillOtpFromSms(otp: String) {
        if (otp.length == otpLength) {
            binding.etOtp1.setText(otp[0].toString())
            binding.etOtp2.setText(otp[1].toString())
            binding.etOtp3.setText(otp[2].toString())
            binding.etOtp4.setText(otp[3].toString())
            binding.etOtp5.setText(otp[4].toString())
            binding.etOtp6.setText(otp[5].toString())
            validateOtpReady()
        }
    }

    // -------------------------------------------------------------------
    // OTP FLOW (REQUEST / VERIFY) - API INTEGRATION
    // -------------------------------------------------------------------

    private fun requestOtp(phone: String) {
        if (phone.isBlank()) return

        failedOtpAttempts = 0
        lockedUntil = 0L
        binding.tvOtpError.visibility = View.GONE

        // Show loading
        //btnGetOtp.isEnabled = false
        //btnGetOtp.text = "Sending..."

        val signupOtpRequest = SignupOtpRequest("+91$phone")

        val call = apiService.generateOtpSignup(signupOtpRequest)

        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>
            ) {
                binding.btnGetOtp.isEnabled = true
                binding.btnGetOtp.text = "Get OTP"

                if (response.isSuccessful && response.body() != null) {
                    try {
                        val responseString = response.body()?.string()
                        Log.d(TAG, "OTP Request Response: $responseString")

                        val jsonObject = JSONObject(responseString ?: "{}")
                        val statusCode = jsonObject.optInt("statusCode", 0)

                        if (statusCode == 200) {
                            Utils.showNewDesignToast(
                                this@MobileLoginActivity,
                                "OTP sent successfully",
                                true
                            )

                            showOtpScreen()

                            startTimer()

                            /*  AnalyticsLogger.logEvent(
                                      AnalyticsEvent.OTP_SENT_SUCCESS,
                                      mapOf(AnalyticsParam.TIMESTAMP to System.currentTimeMillis())
                              )*/
                        } else if (statusCode == 429) {
                            binding.tvOtpError.text = "Too many attempts. Try again later."
                            binding.tvOtpError.visibility = View.VISIBLE
                        } else if (statusCode == 400) {
                            binding.tvOtpError.text = "Incorrect OTP"
                            binding.tvOtpError.visibility = View.VISIBLE
                        } else {
                            val displayMessage =
                                jsonObject.optString("displayMessage", "Failed to send OTP")
                            binding.tvValidationError.text = displayMessage
                            binding.tvValidationError.visibility = View.VISIBLE
                            Utils.showNewDesignToast(
                                this@MobileLoginActivity,
                                displayMessage,
                                false
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing OTP response", e)
                        binding.tvValidationError.text = "Failed to send OTP. Please try again."
                        binding.tvValidationError.visibility = View.VISIBLE
                        Utils.showNewDesignToast(
                            this@MobileLoginActivity,
                            "Failed to send OTP",
                            false
                        )
                    }
                } else {
                    binding.tvValidationError.text = "Failed to send OTP. Please try again."
                    binding.tvValidationError.visibility = View.VISIBLE
                    Utils.showNewDesignToast(this@MobileLoginActivity, "Failed to send OTP", false)
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                binding.btnGetOtp.isEnabled = true
                binding.btnGetOtp.text = "Get OTP"
                binding.tvValidationError.text = "Network error. Please check your connection."
                binding.tvValidationError.visibility = View.VISIBLE
                handleNoInternetView(t)
            }

        })
    }

    private fun showOtpScreen() {
        binding.layoutPhoneEntry.visibility = View.GONE
        binding.layoutOtpScreen.visibility = View.VISIBLE
        clearOtpBoxes()
        binding.tvOtpError.visibility = View.GONE

        binding.tvOtpSubtitle.text = "Please enter the 6 digit code sent to"
        binding.etOtp1.requestFocus()
    }

    private fun verifyOtp(phone: String, otp: String) {
        /*  if (System.currentTimeMillis() < lockedUntil) {
              tvOtpError.text = "Too many attempts. Try again later."
              tvOtpError.visibility = View.VISIBLE
              return
          }*/

        // Show loading
        binding.btnVerifyOtp.isEnabled = false
        binding.btnVerifyOtp.text = "Verifying..."

        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
        val deviceId = Utils.getDeviceId(this)

        val submitLoginOtpRequest = SubmitLoginOtpRequest(
            "+91$phone",
            otp,
            deviceId,
            deviceName,
            "android",
            "dummytokenfortest"
        )

        val call = apiService.submitOtpLogin(submitLoginOtpRequest)

        call.enqueue(object : Callback<GoogleLoginTokenResponse> {
            override fun onResponse(
                call: Call<GoogleLoginTokenResponse>,
                response: Response<GoogleLoginTokenResponse>
            ) {
                binding.btnVerifyOtp.isEnabled = true
                binding.btnVerifyOtp.text = "Verify"

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!

                    // Print JSON for debugging
                    val gson = Gson()
                    val jsonResponse = gson.toJson(apiResponse)
                    Log.d(TAG, "Login Response JSON: $jsonResponse")

                    Utils.showNewDesignToast(
                        this@MobileLoginActivity,
                        "Verification Successful",
                        true
                    )

                    // Save access token
                    val accessToken = apiResponse.accessToken
                    if (!accessToken.isNullOrEmpty()) {
                        sharedPreferenceManager.saveAccessToken(accessToken)
                        saveAccessToken(accessToken)
                    }

                    // Check if new user (adjust based on actual response field)
                    isNewUser = apiResponse.isNewUser ?: false

                    AnalyticsLogger.logEvent(
                        AnalyticsEvent.OTP_SUCCESS,
                        mapOf(
                            AnalyticsParam.USER_TYPE to if (isNewUser) "New User" else "Returning User",
                            AnalyticsParam.PHONE_NUMBER to phoneNumber,
                            AnalyticsParam.DEVICE_NAME to "Android",
                            AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
                        )
                    )

                    handleSuccessfulVerification(phone)
                    //Success
                    setOtpBoxesBackground(R.drawable.bg_otp_success)
                } else {
                    //error
                    setOtpBoxesBackground(R.drawable.bg_otp_error)
                    failedOtpAttempts++
                    binding.tvOtpError.text = "Incorrect OTP"
                    binding.tvOtpError.visibility = View.VISIBLE

                    /*if (failedOtpAttempts >= 5) {
                        lockedUntil = System.currentTimeMillis() + (15 * 60 * 1000)
                        tvOtpError.text = "Too many attempts. Try again later."
                    }*/

                    AnalyticsLogger.logEvent(
                        AnalyticsEvent.OTP_FAILED,
                        mapOf(
                            AnalyticsParam.PHONE_NUMBER to phoneNumber,
                            AnalyticsParam.DEVICE_NAME to "Android",
                            AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
                        )
                    )

                    Utils.showNewDesignToast(this@MobileLoginActivity, "Incorrect OTP", false)
                }
            }

            override fun onFailure(call: Call<GoogleLoginTokenResponse?>, t: Throwable) {
                binding.btnVerifyOtp.isEnabled = true
                binding.btnVerifyOtp.text = "Verify"
                binding.tvOtpError.text = "Network error. Please try again."
                binding.tvOtpError.visibility = View.VISIBLE
                handleNoInternetView(t)
            }

        })
    }

    private fun handleSuccessfulVerification(phone: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            val email = "+91$phone" // Using phone as identifier
            var loggedInUser: LoggedInUser? = null

            for (user in sharedPreferenceManager.loggedUserList) {
                if (email == user.email) {
                    loggedInUser = user
                }
            }

            if (!isNewUser || loggedInUser?.isOnboardingComplete == true) {
                // Existing user - go to main screen
                val loggedInUser = LoggedInUser(email = email, isOnboardingComplete = true)
                sharedPreferenceManager.setLoggedInUsers(arrayListOf(loggedInUser))
                ActivityUtils.startRightLifeContextScreenActivity(this@MobileLoginActivity)
            } else {
                // New user - go to username creation
                val intent =
                    Intent(this@MobileLoginActivity, CreateUsernameActivity::class.java).apply {
                        putExtra("EMAIL", "")
                    }

                val loggedInUsers = sharedPreferenceManager.loggedUserList
                loggedInUsers.add(LoggedInUser(email = email))
                sharedPreferenceManager.setLoggedInUsers(loggedInUsers)
                sharedPreferenceManager.email = email

                startActivity(intent)
            }
            finishAffinity()
        }, 1000)
    }

    private fun saveAccessToken(accessToken: String) {
        SharedPreferenceManager.getInstance(this).saveAccessToken(accessToken)
        val sharedPreferences =
            getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(SharedPreferenceConstants.ACCESS_TOKEN, accessToken)
        editor.putBoolean(SharedPreferenceConstants.IS_LOGGED_IN, true)
        editor.apply()
        getUserDetails()
    }

    private fun getUserDetails() {
        val call = apiService.getUserDetais(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())
                    val responseObj = gson.fromJson(jsonResponse, UserProfileResponse::class.java)

                    sharedPreferenceManager.saveUserId(responseObj.userdata.id)
                    responseObj.userdata.bodyFat = responseObj.bodyFat
                    sharedPreferenceManager.saveUserProfile(responseObj)
                    sharedPreferenceManager.setAIReportGeneratedView(responseObj.reportView)

                    var productId = ""
                    sharedPreferenceManager.userProfile?.subscription?.forEach { subscription ->
                        if (subscription.status) {
                            productId = subscription.productId
                        }
                    }

                    AnalyticsLogger.logEvent(
                        AnalyticsEvent.USER_LOGIN,
                        mapOf(
                            AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                            AnalyticsParam.USER_TYPE to if (isNewUser) "New User" else "Returning User",
                            AnalyticsParam.USER_PLAN to productId,
                            AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
                        )
                    )
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    // -------------------------------------------------------------------
    // TIMER (RESEND OTP)
    // -------------------------------------------------------------------

    private fun startTimer() {
        timer?.cancel()
        binding.tvOtpError.visibility = View.GONE
        binding.tvResendCounter.visibility = View.VISIBLE

        timer = object : CountDownTimer(60_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                binding.tvResendCounter.text = "(${sec}s)"
                binding.tvResendCounter.setTextColor(
                    ContextCompat.getColor(
                        this@MobileLoginActivity,
                        R.color.menuselected
                    )
                )
                //tvResend.text = "Resend code in"
                binding.tvResend.setTextColor(colorStateListNonSelected)
                binding.tvResend.isClickable = false
            }

            override fun onFinish() {
                binding.tvResend.text = "Resend OTP"
                binding.tvResend.setTextColor(
                    ContextCompat.getColor(
                        this@MobileLoginActivity,
                        R.color.menuselected
                    )
                )
                binding.tvResend.isClickable = true
                binding.tvResendCounter.visibility = View.INVISIBLE
                binding.tvOtpSent.visibility = View.GONE
            }
        }.start()
    }

    // -------------------------------------------------------------------
    // SMS RETRIEVER (AUTO FILL OTP)
    // -------------------------------------------------------------------

    private fun startSmsListener() {
        val client = SmsRetriever.getClient(this)
        client.startSmsUserConsent(null)

        ContextCompat.registerReceiver(
            this,
            smsReceiver,
            IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive called with action=${intent?.action}")
            if (SmsRetriever.SMS_RETRIEVED_ACTION != intent?.action) return

            val extras = intent.extras ?: return
            val status = extras.get(SmsRetriever.EXTRA_STATUS) as? Status ?: return
            Log.d(TAG, "SmsRetriever status: ${status.statusCode}")

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val consentIntent =
                        extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                    try {
                        consentIntent?.let { smsConsentLauncher.launch(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting SMS consent activity", e)
                    }
                }

                CommonStatusCodes.TIMEOUT -> {
                    Log.e(TAG, "SMS Retriever timeout")
                }
            }
        }
    }

    // -------------------------------------------------------------------
    // UTIL
    // -------------------------------------------------------------------

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        try {
            unregisterReceiver(smsReceiver)
        } catch (_: Exception) {
            // Receiver was not registered or already unregistered
        }
    }

    companion object {
        private const val TAG = "MobileLoginActivity"
    }

    fun generateOtp(mobileNumber: String) {
        val call = apiService.generateOtpForPhoneNumber(
            sharedPreferenceManager.accessToken,
            OtpRequest(mobileNumber)
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    showCustomToast("OTP sent to your mobile number", true)

                } else {
                    showCustomToast("Retry too early")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    // first check device id if already used
    private fun fetchDeviceInfo(phoneNumber: String) {
        var deviceId = Utils.getDeviceId(this@MobileLoginActivity)
        val call = apiService.getDeviceInfoMobile(deviceId, "+91$phoneNumber")

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val responseString = response.body()?.string()

                    try {
                        val jsonObject = JSONObject(responseString)

                        val statusCode = jsonObject.optInt("statusCode")
                        jsonObject.optString("displayMessage")

                        if (statusCode == 200) {
                            requestOtp(phoneNumber)
                        } else if (statusCode == 500) {
                            handleApiError(jsonObject)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@MobileLoginActivity,
                            "Parsing Error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    // Handle HTTP Errors (like 500 Internal Server Error)
                    val responseCode = response.code()

                    if (responseCode == 500) {
                        /*  Toast.makeText(
                              this@ImageSliderActivity,
                              "Server Error: 500 - Internal Server Error",
                              Toast.LENGTH_SHORT
                          ).show()*/
                        showSwitchBottomSheet()
                    } else {
                        // Try to parse errorBody if present
                        val errorBodyString = response.errorBody()?.string()
                        if (!errorBodyString.isNullOrEmpty()) {
                            try {
                                val jsonObject = JSONObject(errorBodyString)
                                handleApiError(jsonObject)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(
                                    this@MobileLoginActivity,
                                    "Error Code: $responseCode",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@MobileLoginActivity,
                                "Error Code: $responseCode - No Error Body",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun handleApiError(jsonObject: JSONObject) {
        val statusCode = jsonObject.optInt("statusCode")
        val displayMessage = jsonObject.optString("displayMessage")
        val errorCode = jsonObject.optString("errorCode")
        val errorMessage = jsonObject.optString("errorMessage")

        Log.e("API_ERROR", "StatusCode: $statusCode, ErrorCode: $errorCode, Message: $errorMessage")

        when (errorCode) {
            "DEVICE_ALREADY_EXISTS" -> {
                Toast.makeText(
                    this@MobileLoginActivity,
                    "Device already registered!",
                    Toast.LENGTH_SHORT
                ).show()
                // You can navigate or perform a specific action here if needed
                showSwitchBottomSheet()
            }

            else -> {
                Toast.makeText(this@MobileLoginActivity, displayMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSwitchBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogBinding = BottomsheetSwitchAccountBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root
        bottomSheetDialog.setContentView(bottomSheetView)

        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.tvTitle.text = "You’re Logged In With Different Account"
        dialogBinding.tvDescription.text =
            "This device is already logged in with a different account. As a result, free services are not available. \n\nPlease log out and sign in with your original account to access free features."

        dialogBinding.ivDialogClose.setImageResource(R.drawable.close_breathwork)

        dialogBinding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnYes.text = "Skip"
        dialogBinding.btnCancel.text = "Switch Account"

        dialogBinding.btnYes.setOnClickListener {
            requestOtp(phoneNumber)
            bottomSheetDialog.dismiss()
        }
        /*dialogBinding.btnYes.setOnClickListener {
            bottomSheetDialog.dismiss()
        }*/
        dialogBinding.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()

        AnalyticsLogger.logEvent(
            AnalyticsEvent.Login_DiffNo_PopUp,
            mapOf(
                AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                AnalyticsParam.USERNAME to sharedPreferenceManager.userName,
                AnalyticsParam.PLATFORM_TYPE to "Android",
                AnalyticsParam.LOGIN_TYPE to "Phone Number",
                AnalyticsParam.DEVICE_NAME to "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
            )
        )
    }


}