package com.jetsynthesys.rightlife.ui.profile_new

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.apimodel.UploadImage
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse
import com.jetsynthesys.rightlife.apimodel.userdata.Userdata
import com.jetsynthesys.rightlife.databinding.ActivityProfileNewBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetAgeSelectionBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetBodyFatBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetGenderSelectionBinding
import com.jetsynthesys.rightlife.databinding.DialogOtpVerificationBinding
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.customviews.HeightPickerBottomSheet
import com.jetsynthesys.rightlife.ui.customviews.WeightPickerBottomSheet
import com.jetsynthesys.rightlife.ui.new_design.BodyFatAdapter
import com.jetsynthesys.rightlife.ui.new_design.pojo.BodyFat
import com.jetsynthesys.rightlife.ui.profile_new.pojo.OtpEmailRequest
import com.jetsynthesys.rightlife.ui.profile_new.pojo.OtpRequest
import com.jetsynthesys.rightlife.ui.profile_new.pojo.PreSignedUrlData
import com.jetsynthesys.rightlife.ui.profile_new.pojo.PreSignedUrlResponse
import com.jetsynthesys.rightlife.ui.profile_new.pojo.VerifyOtpEmailRequest
import com.jetsynthesys.rightlife.ui.profile_new.pojo.VerifyOtpRequest
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.DecimalDigitsInputFilter
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import com.jetsynthesys.rightlife.ui.utility.isValidIndianMobile
import com.shawnlin.numberpicker.NumberPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class ProfileNewActivity : BaseActivity() {
    private var selectedWeight = "75.0 kg"
    private var selectedHeight = "5.8"

    private lateinit var binding: ActivityProfileNewBinding
    private val CAMERA_REQUEST: Int = 100
    private val PICK_IMAGE_REQUEST: Int = 101
    private var cameraImageUri: Uri? = null
    private var dialogOtp: Dialog? = null
    private lateinit var userData: Userdata
    private lateinit var userDataResponse: UserProfileResponse
    private var preSignedUrlData: PreSignedUrlData? = null
    private lateinit var colorStateListSelected: ColorStateList
    private lateinit var colorStateListNonSelected: ColorStateList
    private lateinit var bindingDialog: DialogOtpVerificationBinding

    // Activity Result Launcher to get image
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            try {
                uri?.let {
                    binding.ivProfileImage.setImageURI(it)
                    binding.ivProfileImage.visibility = VISIBLE
                    binding.tvProfileLetter.visibility = GONE
                    getUrlFromURI(it)
                } ?: run {
                    showToast("No image selected")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error loading image")
            }
        }

    // Camera launcher
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && cameraImageUri != null) {
                binding.ivProfileImage.setImageURI(cameraImageUri)
                binding.ivProfileImage.visibility = VISIBLE
                binding.tvProfileLetter.visibility = GONE
                getUrlFromURI(cameraImageUri!!)
            }
        }

    private fun openWeightPicker(initialWeight: Double, unit: String) {
        val bottomSheet = WeightPickerBottomSheet.newInstance(
            initialWeight,
            unit,
            binding.tvGender.text.toString()
        )
        bottomSheet.setOnWeightSelectedListener { weight, unit ->
            selectedWeight = String.format("%.1f %s", weight, unit)
            binding.tvWeight.text = "$selectedWeight"
        }
        bottomSheet.show(supportFragmentManager, "WeightPicker")
    }

    private fun openHeightPicker(unit: String) {
        val bottomSheet = HeightPickerBottomSheet.newInstance(
            unit = unit,
            gender = binding.tvGender.text.toString()
        )
        bottomSheet.setOnHeightSelectedListener { height, unit ->
            selectedHeight = height
            if (unit == "ft") {
                selectedHeight = "$height $unit"
                val height = height.toString().split(".")
                binding.tvHeight.text = "${height[0]} ft ${height[1]} in"
            } else {
                selectedHeight = "$height $unit"
                binding.tvHeight.text = "${height}"

            }
            //binding.edtHeight.setText("${selectedHeight}")
        }
        bottomSheet.show(supportFragmentManager, "HeightPicker")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileNewBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        colorStateListSelected = ContextCompat.getColorStateList(this, R.color.menuselected)!!
        colorStateListNonSelected = ContextCompat.getColorStateList(this, R.color.rightlife)!!

        userDataResponse = sharedPreferenceManager.userProfile
        userData = userDataResponse.userdata
        setUserData(userData)

        // Non-editable field click listeners
        binding.llAge.setOnClickListener {
            it.disableViewForSeconds()
            showAgeSelectionBottomSheet()
        }
        binding.arrowAge.setOnClickListener {
            it.disableViewForSeconds()
            showAgeSelectionBottomSheet()
        }

        binding.tvGender.setOnClickListener {
            it.disableViewForSeconds()
            showGenderSelectionBottomSheet()
        }
        binding.arrowGender.setOnClickListener {
            it.disableViewForSeconds()
            showGenderSelectionBottomSheet()
        }

        binding.tvHeight.setOnClickListener {
            it.disableViewForSeconds()
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    val unit =
                        if (binding.tvHeight.text.toString().trim().contains("ft")) "ft" else "cm"
                    openHeightPicker(unit)
                }
            }
        }


        binding.arrowHeight.setOnClickListener {
            it.disableViewForSeconds()
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    val unit =
                        if (binding.tvHeight.text.toString().trim().contains("ft")) "ft" else "cm"
                    openHeightPicker(unit)
                }
            }
        }

        binding.tvWeight.setOnClickListener {
            it.disableViewForSeconds()
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    val weightText = binding.tvWeight.text?.toString()?.trim().orEmpty()
                    val gender = binding.tvGender.text?.toString()?.trim()

                    val (value, unit) = if (weightText.isNotEmpty()) {
                        val parts = weightText.split(" ")
                        parts.getOrNull(0)?.toDoubleOrNull()
                        val weightUnit = parts.getOrNull(1) ?: "kg"

                        //Pair(weight ?: if (gender == "Male" || gender == "M") 75.0 else 55.0, weightUnit)
                        val defaultWeight = when {
                            gender == "Male" || gender == "M" ->
                                if (weightUnit == "kg") 75.0 else 165.0

                            else ->
                                if (weightUnit == "kg") 55.0 else 121.0
                        }

                        Pair(defaultWeight, weightUnit)
                    } else {
                        Pair(if (gender == "Male" || gender == "M") 75.0 else 55.0, "kg")
                    }

                    openWeightPicker(value, unit)

                }
            }
        }
        binding.arrowWeight.setOnClickListener {
            it.disableViewForSeconds()
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    val weightText = binding.tvWeight.text?.toString()?.trim().orEmpty()
                    val gender = binding.tvGender.text?.toString()?.trim()

                    val (value, unit) = if (weightText.isNotEmpty()) {
                        val parts = weightText.split(" ")
                        parts.getOrNull(0)?.toDoubleOrNull()
                        val weightUnit = parts.getOrNull(1) ?: "kg"

                        //Pair(weight ?: if (gender == "Male" || gender == "M") 75.0 else 55.0, weightUnit)
                        val defaultWeight = when {
                            gender == "Male" || gender == "M" ->
                                if (weightUnit == "kg") 75.0 else 165.0

                            else ->
                                if (weightUnit == "kg") 55.0 else 121.0
                        }

                        Pair(defaultWeight, weightUnit)
                    } else {
                        Pair(if (gender == "Male" || gender == "M") 75.0 else 55.0, "kg")
                    }

                    openWeightPicker(value, unit)
                }
            }
        }

        binding.tvBodyFat.setOnClickListener {
            it.disableViewForSeconds()
            showBodyFatBottomSheet()
        }
        binding.arrowBodyFat.setOnClickListener {
            it.disableViewForSeconds()
            showBodyFatBottomSheet()
        }

        binding.arrowDeleteAccount.setOnClickListener {
            it.disableViewForSeconds()
            startActivity(Intent(this, DeleteAccountSelectionActivity::class.java))
        }
        binding.llDeleteAccount.setOnClickListener {
            it.disableViewForSeconds()
            startActivity(Intent(this, DeleteAccountSelectionActivity::class.java))
        }

        binding.ivEditProfile.setOnClickListener {
            it.disableViewForSeconds()
            showImagePickerDialog()
        }

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (isValidGoogleEmail(p0.toString())) {
                    binding.tvEmailError.visibility = GONE
                    binding.btnVerifyEmail.isEnabled = true
                    binding.btnVerifyEmail.backgroundTintList = colorStateListSelected
                } else {
                    binding.btnVerifyEmail.isEnabled = false
                    binding.btnVerifyEmail.backgroundTintList = colorStateListNonSelected
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        binding.etMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (isValidIndianMobile(p0.toString())) {
                    binding.btnVerify.isEnabled = true
                    binding.btnVerify.backgroundTintList = colorStateListSelected
                } else {
                    binding.btnVerify.isEnabled = false
                    binding.btnVerify.backgroundTintList = colorStateListNonSelected
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        binding.btnVerify.setOnClickListener {
            it.disableViewForSeconds()
            val mobileNumber = binding.etMobile.text.toString()
            if (mobileNumber.isEmpty()) {
                showToast("Please enter 10 digit mobile number")
            } else if (mobileNumber.length != 10) {
                showToast("Please enter correct mobile number")
            } else {
                generateOtp("+91$mobileNumber")
            }
        }

        binding.btnVerifyEmail.setOnClickListener {
            it.disableViewForSeconds()
            if (isValidGoogleEmail(binding.etEmail.text.toString())) {
                generateEmailOTP(binding.etEmail.text.toString())
            } else {
                binding.tvEmailError.visibility = VISIBLE
            }
        }


        binding.btnSave.setOnClickListener {
            it.disableViewForSeconds()
            saveData()
        }
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        binding.etFirstName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, count: Int) {
                if (validateUsername(p0.toString())) {

                } else {
                    if (p0.toString().isNullOrBlank()) {
                        //Toast.makeText(this@ProfileNewActivity, "Invalid username", Toast.LENGTH_SHORT).show()
                    } else {
                        showCustomToast("Invalid First Name")
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        binding.etLastName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, count: Int) {
                if (validateUsername(p0.toString())) {

                } else {
                    if (p0.toString().isNullOrBlank()) {
                        //Toast.makeText(this@ProfileNewActivity, "Invalid username", Toast.LENGTH_SHORT).show()
                    } else {
                        showCustomToast("Invalid Last Name")
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

    private fun validateUsername(username: String): Boolean {
        if (username.isEmpty()) {
            return false
        }
        // Check if the username only contains alphabetic characters
        val regex = "^[A-Za-z]+$".toRegex()

        return when {
            !username.matches(regex) -> false
            else -> true
        }
    }

    private fun setUserData(userData: Userdata) {

        if (userData.firstName != null) binding.etFirstName.setText(userData.firstName)
        if (userData.lastName != null) binding.etLastName.setText(userData.lastName)
        if (userData.email != null) binding.etEmail.setText(userData.email)/*if (userData.phoneNumber != null)
            binding.etMobile.setText(userData.phoneNumber)*/

        userData.phoneNumber?.let { number ->
            // Remove all non-digit characters (in case number has spaces or symbols)
            val digitsOnly = number.filter { it.isDigit() }

            // Take the last 10 digits safely (works even if number is shorter)
            val lastTenDigits = if (digitsOnly.length >= 10) digitsOnly.takeLast(10) else digitsOnly

            binding.etMobile.setText(lastTenDigits)
        }
        if ("VERIFIED".equals(userData.newPhoneStatus, ignoreCase = false)) {
            binding.etMobile.isEnabled = false
            binding.btnVerify.visibility = GONE
            setEndDrawable(binding.etMobile)
        } else {
            binding.btnVerify.text = "Verify"
            binding.etMobile.isEnabled = true
        }

        if ("VERIFIED".equals(userData.newEmailStatus, ignoreCase = false)) {
            binding.etEmail.isEnabled = false
            binding.btnVerifyEmail.visibility = GONE
            setEndDrawable(binding.etEmail)
        } else {
            binding.btnVerifyEmail.text = "Verify"
            binding.etEmail.isEnabled = true
        }

        if (userData.age != null) binding.tvAge.text = userData.age.toString() + " years"
        if (userData.gender == "M") binding.tvGender.text = "Male"
        else binding.tvGender.text = "Female"

        if (userData.weight != null) binding.tvWeight.text =
            "${userData.weight} ${userData.weightUnit.lowercase(Locale.getDefault())}"

        if (userData.profilePicture.isNullOrEmpty()) {/*if (userData.firstName.isNotEmpty())
                binding.tvProfileLetter.text = userData.firstName.first().toString()
            else
                binding.tvProfileLetter.text = "R"*/
            binding.ivProfileImage.visibility = VISIBLE
            binding.tvProfileLetter.visibility = GONE
        } else {
            binding.ivProfileImage.visibility = VISIBLE
            binding.tvProfileLetter.visibility = GONE
            Glide.with(this).load(ApiClient.CDN_URL_QA + userData.profilePicture)
                .placeholder(R.drawable.rl_profile).error(R.drawable.rl_profile)
                .into(binding.ivProfileImage)
        }

        if (userData.height != null) if (userData.heightUnit == "FT_AND_INCHES") {
            val height = userData.height.toString().split(".")
            binding.tvHeight.text = "${height[0]} ft ${height[1]} in"
        } else {
            binding.tvHeight.text = "${userData.height.toInt()} cm"
        }

        if (userData.bodyFat != null && userData.bodyFat.isNotEmpty()) {
            binding.tvBodyFat.text = "${userData.bodyFat} %"
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        cameraImageUri = FileProvider.getUriForFile(
            this, "${packageName}.fileprovider", photoFile
        )
        cameraImageUri?.let { uri ->
            cameraLauncher.launch(uri)
        }
    }

    private fun getFileNameAndSize(context: Context, uri: Uri): Pair<String, Long>? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        returnCursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (it.moveToFirst()) {
                val name = it.getString(nameIndex)
                val size = it.getLong(sizeIndex)
                return Pair(name, size)
            }
        }
        return null
    }

    private fun getUrlFromURI(uri: Uri) {
        val uploadImage = UploadImage()
        uploadImage.isPublic = false
        uploadImage.fileType = "USER_FILES"
        if (uri != null) {
            val (fileName, fileSize) = getFileNameAndSize(this, uri) ?: return
            uploadImage.fileSize = fileSize
            uploadImage.fileName = fileName
        }
        uriToFile(uri)?.let { getPreSignedUrl(uploadImage, it) }
    }


    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", ".jpg", storageDir
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showAgeSelectionBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetAgeSelectionBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        bottomSheetDialog.setOnShowListener { dialog ->
            val bottomSheet =
                (dialog as BottomSheetDialog)
                    .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    ?: return@setOnShowListener

            val behavior = BottomSheetBehavior.from(bottomSheet)

            behavior.state = BottomSheetBehavior.STATE_COLLAPSED  // Start collapsed
            behavior.skipCollapsed = false                        // Allow collapsed state
            behavior.isFitToContents = false                      // Needed to control peekHeight
            behavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
            behavior.isDraggable = false                          // Prevent expanding via swipe
        }


        val years = arrayOf(
            "13 years",
            "14 years",
            "15 years",
            "16 years",
            "17 years",
            "18 years",
            "19 years",
            "20 years",
            "21 years",
            "22 years",
            "23 years",
            "24 years",
            "25 years",
            "26 years",
            "27 years",
            "28 years",
            "29 years",
            "30 years",
            "31 years",
            "32 years",
            "33 years",
            "34 years",
            "35 years",
            "36 years",
            "37 years",
            "38 years",
            "39 years",
            "40 years",
            "41 years",
            "42 years",
            "43 years",
            "44 years",
            "45 years",
            "46 years",
            "47 years",
            "48 years",
            "49 years",
            "50 years",
            "51 years",
            "52 years",
            "53 years",
            "54 years",
            "55 years",
            "56 years",
            "57 years",
            "58 years",
            "59 years",
            "60 years",
            "61 years",
            "62 years",
            "63 years",
            "64 years",
            "65 years",
            "66 years",
            "67 years",
            "68 years",
            "69 years",
            "70 years",
            "71 years",
            "72 years",
            "73 years",
            "74 years",
            "75 years",
            "76 years",
            "77 years",
            "78 years",
            "79 years",
            "80 years",
        )

        val selectedAgeArray = binding.tvAge.text.toString().split(" ")
        val selectedAgeFromUi =
            if (selectedAgeArray.isNotEmpty() && selectedAgeArray[0].toInt() >= 13) {
                binding.tvAge.text.toString()
            } else ""

        val value1 = if (selectedAgeFromUi.isNotEmpty()) years.indexOf(selectedAgeFromUi) + 1
        else 15

        dialogBinding.numberPicker.apply {
            minValue = 1
            maxValue = years.size
            displayedValues = years
            value = value1
            wheelItemCount = 7
            typeface =
                ResourcesCompat.getFont(this@ProfileNewActivity, R.font.dmsans_regular)
        }

        var selectedAge =
            if (selectedAgeFromUi.isNotEmpty()) years[years.indexOf(selectedAgeFromUi)]
            else years[14]

        dialogBinding.numberPicker.setOnScrollListener { view, scrollState ->
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                selectedAge = years[view.value - 1]
                binding.tvAge.text = selectedAge
            }
        }

        var startY = 0f
        var isScrolling = false

        dialogBinding.numberPicker.setOnTouchListener { v, event ->
            val picker = v as NumberPicker

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.y
                    isScrolling = false
                }

                MotionEvent.ACTION_MOVE -> {
                    // If finger moved enough vertically, mark as scrolling
                    if (abs(event.y - startY) > picker.height / 20) {
                        isScrolling = true
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (!isScrolling) {
                        val height = picker.height
                        val visibleItems = 5
                        val rowHeight = height / visibleItems
                        val centerY = height / 2
                        val tolerance = rowHeight / 2

                        // Only dismiss if tapped on center row
                        if (event.y > centerY - tolerance && event.y < centerY + tolerance) {
                            bottomSheetDialog.dismiss()
                            return@setOnTouchListener true
                        }
                    }
                }
            }
            false
        }


        dialogBinding.btnConfirm.setOnClickListener {
            bottomSheetDialog.dismiss()
            binding.tvAge.text = selectedAge
        }

        bottomSheetDialog.show()
    }

    private fun showGenderSelectionBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetGenderSelectionBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.llMale.setOnClickListener {
            bottomSheetDialog.dismiss()
            binding.tvGender.text = dialogBinding.tvMale.text
        }

        dialogBinding.llFemale.setOnClickListener {
            bottomSheetDialog.dismiss()
            binding.tvGender.text = dialogBinding.tvFemale.text
        }

        bottomSheetDialog.show()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST
            )
        } else {
            openCamera()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun generateEmailOTP(email: String) {
        val call = apiService.generateEmailOtp(
            sharedPreferenceManager.accessToken, OtpEmailRequest(email)
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    showCustomToast("OTP sent to your GmailId", true)
                    if (dialogOtp != null && dialogOtp?.isShowing == true) {
                        dialogOtp?.dismiss()
                    }
                    showOtpDialog(this@ProfileNewActivity, email, false)
                } else {
                    val errorBody = response.errorBody()?.string()

                    val message = try {
                        val json = JSONObject(errorBody ?: "")
                        json.optString(
                            "displayMessage", json.optString("errorMessage", "Something went wrong")
                        )
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    showCustomToast(message)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun generateOtp(mobileNumber: String) {
        val call = apiService.generateOtpForPhoneNumber(
            sharedPreferenceManager.accessToken, OtpRequest(mobileNumber)
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    showCustomToast("OTP sent to your mobile number", true)
                    if (dialogOtp != null && dialogOtp?.isShowing == true) {
                        dialogOtp?.dismiss()
                    }
                    showOtpDialog(this@ProfileNewActivity, mobileNumber)
                } else {
                    val errorBody = response.errorBody()?.string()

                    val message = try {
                        val json = JSONObject(errorBody ?: "")
                        json.optString(
                            "displayMessage", json.optString("errorMessage", "Something went wrong")
                        )
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    showCustomToast(message)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun showOtpDialog(
        activity: Activity, mobileNumberEmail: String, isFromMobile: Boolean = true
    ) {
        dialogOtp = Dialog(activity)
        val binding = DialogOtpVerificationBinding.inflate(LayoutInflater.from(activity))
        dialogOtp?.setContentView(binding.root)
        dialogOtp?.setCancelable(false)
        dialogOtp?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val otpFields = listOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )

        bindingDialog = binding

        startSmsListener()

        // Move focus automatically
        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < otpFields.size - 1) {
                        otpFields[index + 1].requestFocus()
                    } else if (s?.length == 0 && index > 0) {
                        otpFields.forEach { editText ->
                            editText.setBackgroundResource(R.drawable.bg_otp_default)
                        }
                        otpFields[index - 1].requestFocus()
                    }
                    val otp = otpFields.joinToString("") { it.text.toString().trim() }
                    if (otp.length == 6) {
                        bindingDialog.btnVerify.isEnabled = true
                        binding.btnVerify.backgroundTintList = colorStateListSelected
                    } else {
                        bindingDialog.btnVerify.isEnabled = false
                        binding.btnVerify.backgroundTintList = colorStateListNonSelected
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        // Countdown Timer
        val timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountdown.text = "${millisUntilFinished / 1000}"
                binding.tvResendCounter.text = "(${millisUntilFinished / 1000})s"
            }

            override fun onFinish() {
                binding.tvCountdown.visibility = GONE
                binding.tvResendCounter.visibility = GONE
                binding.tvResend.setTextColor(colorStateListSelected)
                binding.tvResend.isEnabled = true
            }
        }
        timer.start()

        // Cancel Button
        binding.ivClose.setOnClickListener {
            timer.cancel()
            dialogOtp?.dismiss()
        }

        binding.tvChange.setOnClickListener {
            timer.cancel()
            dialogOtp?.dismiss()
        }

        binding.tvResend.setOnClickListener {
            it.disableViewForSeconds()
            timer.cancel()
            binding.tvResend.setTextColor(colorStateListNonSelected)
            binding.tvResend.isEnabled = false
            if (isFromMobile) generateOtp(mobileNumberEmail)
            else generateEmailOTP(mobileNumberEmail)
        }

        binding.tvPhone.text = mobileNumberEmail

        // Verify Button
        binding.btnVerify.setOnClickListener {
            val otp = otpFields.joinToString("") { it.text.toString().trim() }
            if (otp.length == 6) {
                if (isFromMobile) verifyOtp(mobileNumberEmail, otp, binding, timer)
                else verifyEmailOtp(mobileNumberEmail, otp, binding, timer)
            } else {
                showCustomToast("Enter all 6 digits")
            }
        }

        dialogOtp?.show()
    }

    private fun verifyEmailOtp(
        email: String,
        otp: String,
        bindingDialog: DialogOtpVerificationBinding,
        timer: CountDownTimer
    ) {

        val otpFields = listOf(
            bindingDialog.etOtp1,
            bindingDialog.etOtp2,
            bindingDialog.etOtp3,
            bindingDialog.etOtp4,
            bindingDialog.etOtp5,
            bindingDialog.etOtp6
        )

        val call = apiService.verifyOtpForEmail(
            sharedPreferenceManager.accessToken, VerifyOtpEmailRequest(
                email = email, otp = otp
            )
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    timer.cancel()
                    bindingDialog.tvResult.visibility = GONE
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialogOtp?.dismiss()
                    }, 500)

                    userData.newEmailStatus = "VERIFIED"

                    binding.etEmail.isEnabled = false
                    binding.btnVerifyEmail.visibility = GONE
                    setEndDrawable(binding.etEmail)
                    userData.email = binding.etEmail.text.toString()

                    userDataResponse.userdata = userData
                    sharedPreferenceManager.saveUserProfile(userDataResponse)
                    otpFields.forEach { editText ->
                        editText.setBackgroundResource(R.drawable.bg_otp_success)
                    }

                } else {
                    otpFields.forEach { editText ->
                        editText.setBackgroundResource(R.drawable.bg_otp_error)
                    }
                    bindingDialog.tvResult.text = "Incorrect OTP"
                    bindingDialog.tvResult.setTextColor(getColor(R.color.menuselected))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleNoInternetView(t)
                bindingDialog.tvResult.text = "Incorrect OTP"
                bindingDialog.tvResult.setTextColor(getColor(R.color.menuselected))
            }

        })
    }

    private fun verifyOtp(
        mobileNumber: String,
        otp: String,
        bindingDialog: DialogOtpVerificationBinding,
        timer: CountDownTimer
    ) {

        val otpFields = listOf(
            bindingDialog.etOtp1,
            bindingDialog.etOtp2,
            bindingDialog.etOtp3,
            bindingDialog.etOtp4,
            bindingDialog.etOtp5,
            bindingDialog.etOtp6
        )

        val call = apiService.verifyOtpForPhoneNumber(
            sharedPreferenceManager.accessToken, VerifyOtpRequest(
                phoneNumber = mobileNumber, otp = otp
            )
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    timer.cancel()
                    bindingDialog.tvResult.visibility = GONE
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialogOtp?.dismiss()
                    }, 500)

                    userData.newPhoneStatus = "VERIFIED"

                    binding.etMobile.isEnabled = false
                    binding.btnVerify.visibility = GONE
                    setEndDrawable(binding.etMobile)

                        otpFields.forEach { editText ->
                            editText.setBackgroundResource(R.drawable.bg_otp_success)
                        }

                } else {
                    bindingDialog.tvResult.text = "Incorrect OTP"
                    bindingDialog.tvResult.setTextColor(getColor(R.color.menuselected))
                    otpFields.forEach { editText ->
                        editText.setBackgroundResource(R.drawable.bg_otp_error)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleNoInternetView(t)
                bindingDialog.tvResult.text = "Incorrect OTP"
                bindingDialog.tvResult.setTextColor(getColor(R.color.menuselected))
            }

        })
    }

    private fun uriToFile(uri: Uri): File? {
        val contentResolver = contentResolver
        val fileName = getFileName(uri) ?: "temp_image_file"
        val tempFile = File(cacheDir, fileName)

        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val returnCursor = contentResolver.query(uri, null, null, null, null)
        returnCursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && it.moveToFirst()) {
                name = it.getString(nameIndex)
            }
        }
        return name
    }


    private fun saveData() {
        val firstName = binding.etFirstName.text.toString()
        val lastName = binding.etLastName.text.toString()
        val email = binding.etEmail.text.toString()
        val mobileNumber = binding.etMobile.text.toString()
        val age = binding.tvAge.text.toString()
        val height = binding.tvHeight.text.toString()
        val weight = binding.tvWeight.text.toString()
        val gender = binding.tvGender.text.toString()
        val bodyFat = binding.tvBodyFat.text.toString()?.replace("%", "")?.trim() ?: ""
        if (firstName.isEmpty() || age.isEmpty() || gender.isEmpty() || height.isEmpty() || weight.isEmpty() || bodyFat.isEmpty()) {
            showCustomToast("Please fill all required fields before proceeding.")
        } else if (!validateUsername(firstName)) {
            showCustomToast("Please enter valid First Name")
        } else if (!validateUsername(firstName)) {
            showCustomToast("Please enter valid Last Name")
        } else if (email.isNotEmpty() && !email.matches(Utils.emailPattern.toRegex())) {
            showCustomToast("Invalid Email format")
        } else if (age.split(" ")[0].toInt() !in 13..80) showCustomToast("Face Scan is available only for users aged 13–80.")
        else {
            // ✅ Compare old and new phone number
            val oldPhone = userData.phoneNumber?.filter { it.isDigit() } ?: ""
            val newPhone = mobileNumber.filter { it.isDigit() }
            userData.firstName = firstName
            userData.lastName = lastName
            userData.email = email
            userData.phoneNumber = "+91$mobileNumber"
            val ageArray = age.split(" ")
            userData.age = ageArray[0].toInt()
            if (gender.equals("Male", ignoreCase = true)) {
                userData.gender = "M"
            } else {
                userData.gender = "F"
            }
            if (weight.isNotEmpty()) {
                val w = weight.split(" ")
                userData.weight = w[0].toDouble()
                if ("kgs".equals(w[1], ignoreCase = true) || "kg".equals(w[1], ignoreCase = true)) {
                    userData.weightUnit = "KG"
                } else {
                    userData.weightUnit = "LBS"
                }
            }

            if (height.isNotEmpty()) {
                val h = height.split(" ")
                if (h.size == 2) {
                    userData.height = h[0].toDouble()
                    userData.heightUnit = "CM"
                } else {
                    userData.height = "${h[0]}.${h[2]}".toDouble()
                    userData.heightUnit = "FT_AND_INCHES"
                }
            }

            userData.bodyFat = bodyFat

            if (preSignedUrlData != null) {
                userData.profilePicture = preSignedUrlData?.file?.url
            }
            // ✅ Set newPhoneStatus if phone number changed
            if (oldPhone != newPhone) {
                userData.newPhoneStatus = ""
            }
            updateUserData(userData)
            updateChecklistStatus()
            var productId = ""
            sharedPreferenceManager.userProfile.subscription.forEach { subscription ->
                if (subscription.status) {
                    productId = subscription.productId
                }
            }
            AnalyticsLogger.logEvent(
                this, AnalyticsEvent.CHECKLIST_PROFILE_COMPLETE, mapOf(
                    AnalyticsParam.TIME_TO_COMPLETE to "",
                    AnalyticsParam.WEIGHT to userData.weight,
                    AnalyticsParam.HEIGHT to userData.height
                )
            )
        }
    }

    private fun updateChecklistStatus() {
        CommonAPICall.updateChecklistStatus(this, "profile", AppConstants.CHECKLIST_COMPLETED)
    }

    private fun updateUserData(userdata: Userdata) {
        val call: Call<ResponseBody> =
            apiService.updateUser(sharedPreferenceManager.accessToken, userdata)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    setResult(RESULT_OK)
                    userDataResponse.userdata = userdata
                    sharedPreferenceManager.saveUserProfile(userDataResponse)
                    getUserDetails()
                    showCustomToast("Profile Updated Successfully", true)
                    finish()
                } else {
                    showToast("Server Error: " + response.code())
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun getPreSignedUrl(uploadImage: UploadImage, file: File) {
        val call: Call<PreSignedUrlResponse> =
            apiService.getPreSignedUrl(sharedPreferenceManager.accessToken, uploadImage)

        call.enqueue(object : Callback<PreSignedUrlResponse?> {
            override fun onResponse(
                call: Call<PreSignedUrlResponse?>, response: Response<PreSignedUrlResponse?>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.data?.let { preSignedUrlData = it }
                    response.body()?.data?.url?.let {
                        CommonAPICall.uploadImageToPreSignedUrl(
                            this@ProfileNewActivity, file, it
                        ) { success ->
                            if (success) {
                                showToast("Image uploaded successfully!")
                            } else {
                                showToast("Upload failed!")
                            }
                        }
                    }
                } else {
                    showToast("Server Error: " + response.code())
                }
            }

            override fun onFailure(call: Call<PreSignedUrlResponse?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        AlertDialog.Builder(this).setTitle("Upload Profile Picture")
            .setItems(options) { dialog, which ->
                when (options[which]) {
                    "Take Photo" -> checkPermissions()
                    "Choose from Gallery" -> openGallery()
                    "Cancel" -> dialog.dismiss()
                }
            }.show()
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*") // Open gallery to pick image
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
                    sharedPreferenceManager.saveUserId(ResponseObj.userdata.id)
                    ResponseObj.userdata.bodyFat = ResponseObj.bodyFat
                    sharedPreferenceManager.saveUserProfile(ResponseObj)

                    sharedPreferenceManager.setAIReportGeneratedView(ResponseObj.reportView)

                    userDataResponse = sharedPreferenceManager.userProfile
                    userData = userDataResponse.userdata
                    setUserData(userData)
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }

            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun setEndDrawable(
        editText: EditText,
        @DrawableRes drawableRes: Int? = R.drawable.icon_verified,
        paddingDp: Int = 8
    ) {
        val drawable = drawableRes?.let {
            ContextCompat.getDrawable(editText.context, it)
        }

        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null, null, drawable, null
        )

        editText.compoundDrawablePadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, paddingDp.toFloat(), editText.resources.displayMetrics
        ).toInt()
    }

    private fun isValidGoogleEmail(email: String): Boolean {
        val trimmed = email.trim()
        //only email validation
        // return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()


        //for gmail validation
        val regex = Regex("^[A-Za-z0-9._%+-]+@(gmail\\.com|googlemail\\.com)$")
        return regex.matches(trimmed)
    }

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
            Log.d("TAG", "onReceive called with action=${intent?.action}")
            if (SmsRetriever.SMS_RETRIEVED_ACTION != intent?.action) return

            val extras = intent.extras ?: return
            val status = extras.get(SmsRetriever.EXTRA_STATUS) as? Status ?: return
            Log.d("TAG", "SmsRetriever status: ${status.statusCode}")

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val consentIntent =
                        extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                    try {
                        consentIntent?.let { smsConsentLauncher.launch(it) }
                    } catch (e: Exception) {
                        Log.e("TAG", "Error starting SMS consent activity", e)
                    }
                }

                CommonStatusCodes.TIMEOUT -> {
                    Log.e("TAG", "SMS Retriever timeout")
                }
            }
        }
    }

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

    private fun fillOtpFromSms(otp: String) {
        if (otp.length == 6) {
            bindingDialog.etOtp1.setText(otp[0].toString())
            bindingDialog.etOtp2.setText(otp[1].toString())
            bindingDialog.etOtp3.setText(otp[2].toString())
            bindingDialog.etOtp4.setText(otp[3].toString())
            bindingDialog.etOtp5.setText(otp[4].toString())
            bindingDialog.etOtp6.setText(otp[5].toString())
        }
    }

    private fun showBodyFatBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetBodyFatBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        // Expand fully on show
        bottomSheetDialog.setOnShowListener { dialog ->
            val bottomSheet =
                (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)

            //behavior.state = BottomSheetBehavior.STATE_EXPANDED   // Open full height
            behavior.skipCollapsed = true                          // No collapsed state
            behavior.isFitToContents = true                        // Fit to content
            behavior.isDraggable = true                           // Optional: disable swipe down
        }

        val bodyFat = binding.tvBodyFat.text.toString()?.replace("%", "")
            ?.trim()
            ?.toDoubleOrNull()
            ?: 0.0
        // set available value
        if (bodyFat >= 5) {
            dialogBinding.btnContinue.isEnabled = true
            dialogBinding.btnContinue.backgroundTintList = colorStateListSelected
            dialogBinding.edtBodyFat.setText(average(bodyFat.toString()).toString())
            dialogBinding.edtBodyFat.setSelection(dialogBinding.edtBodyFat.text.length)
            dialogBinding.iconMinus.visibility = VISIBLE
            dialogBinding.iconPlus.visibility = VISIBLE
            dialogBinding.tvPercent.visibility = VISIBLE
        }

        val adapter =
            BodyFatAdapter(this, getBodyFatList(binding.tvGender.text.toString())) { bodyFat ->
                dialogBinding.btnContinue.isEnabled = true
                dialogBinding.btnContinue.backgroundTintList = colorStateListSelected
                dialogBinding.edtBodyFat.setText(average(bodyFat.bodyFatNumber).toString())
                dialogBinding.edtBodyFat.setSelection(dialogBinding.edtBodyFat.text.length)
                dialogBinding.iconMinus.visibility = VISIBLE
                dialogBinding.iconPlus.visibility = VISIBLE
                dialogBinding.tvPercent.visibility = VISIBLE
            }

        val gridLayoutManager = GridLayoutManager(this, 2)
        dialogBinding.rvBodyFat.setLayoutManager(gridLayoutManager)
        dialogBinding.rvBodyFat.adapter = adapter

        setSelection(binding.tvGender.text.toString(), bodyFat, adapter)

        dialogBinding.edtBodyFat.filters = arrayOf(DecimalDigitsInputFilter())

        dialogBinding.iconMinus.setOnClickListener {
            var fatValue = dialogBinding.edtBodyFat.text.toString().toDouble()
            if (fatValue > 5) {
                fatValue = dialogBinding.edtBodyFat.text.toString().toDouble() - 0.5
            }
            dialogBinding.edtBodyFat.setText(fatValue.toString())
            dialogBinding.edtBodyFat.setSelection(dialogBinding.edtBodyFat.text.length)
            dialogBinding.edtBodyFat.requestFocus()
        }

        dialogBinding.iconPlus.setOnClickListener {
            var fatValue = dialogBinding.edtBodyFat.text.toString().toDouble()
            if (fatValue < 60) {
                fatValue = dialogBinding.edtBodyFat.text.toString().toDouble() + 0.5
            }
            dialogBinding.edtBodyFat.setText(fatValue.toString())
            dialogBinding.edtBodyFat.setSelection(dialogBinding.edtBodyFat.text.length)
            dialogBinding.edtBodyFat.requestFocus()
        }

        dialogBinding.edtBodyFat.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.length?.let {
                    if (it > 0) {
                        dialogBinding.iconMinus.visibility = VISIBLE
                        dialogBinding.iconPlus.visibility = VISIBLE
                        dialogBinding.tvPercent.visibility = VISIBLE
                        dialogBinding.btnContinue.isEnabled = true
                        dialogBinding.btnContinue.backgroundTintList = colorStateListSelected
                        setSelection(
                            binding.tvGender.text.toString(),
                            s.toString().toDouble(),
                            adapter
                        )
                    } else {
                        dialogBinding.iconMinus.visibility = GONE
                        dialogBinding.iconPlus.visibility = GONE
                        dialogBinding.tvPercent.visibility = GONE
                        dialogBinding.btnContinue.isEnabled = false
                        dialogBinding.btnContinue.backgroundTintList = colorStateListNonSelected
                        adapter.clearSelection()
                    }
                }
            }
        })

        dialogBinding.btnContinue.setOnClickListener {
            if (dialogBinding.edtBodyFat.text.toString().toDouble() in 5.0..60.0) {
                binding.tvBodyFat.text = "${dialogBinding.edtBodyFat.text} %"
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(
                    this,
                    "Please select fat between 5% to 60%",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        bottomSheetDialog.show()
    }

    private fun getBodyFatList(gender: String): ArrayList<BodyFat> {
        val bodyFatList = ArrayList<BodyFat>()
        if (gender == "Male") {
            bodyFatList.add(BodyFat(R.drawable.img_male_fat1, "5-14%"))
            bodyFatList.add(BodyFat(R.drawable.img_male_fat2, "15-24%"))
            bodyFatList.add(BodyFat(R.drawable.img_male_fat3, "25-33%"))
            bodyFatList.add(BodyFat(R.drawable.img_male_fat4, "34+%"))
        } else {
            bodyFatList.add(BodyFat(R.drawable.img_female_fat1, "10-19%"))
            bodyFatList.add(BodyFat(R.drawable.img_female_fat2, "20-29%"))
            bodyFatList.add(BodyFat(R.drawable.img_female_fat3, "30-44%"))
            bodyFatList.add(BodyFat(R.drawable.img_female_fat4, "45+%"))
        }

        return bodyFatList
    }

    private fun average(input: String): Double {
        val regex = "(\\d+)-(\\d+)".toRegex()

        val matchResult = regex.find(input)
        if (matchResult != null) {
            val num1 = matchResult.groupValues[1].toDouble() // Extracts 5
            val num2 = matchResult.groupValues[2].toDouble() // Extracts 14
            return (num1 + num2) / 2
        } else {
            return input.substring(0, 2).toDouble()
        }

    }

    private fun setSelection(gender: String, bodyFat: Double, adapter: BodyFatAdapter) {
        if (gender == "Male") {
            if (bodyFat in 5.0..14.9)
                adapter.setSelected(0)
            else if (bodyFat in 14.0..24.9)
                adapter.setSelected(1)
            else if (bodyFat in 25.0..33.9)
                adapter.setSelected(2)
            else if (bodyFat >= 34)
                adapter.setSelected(3)
        } else {
            if (bodyFat in 10.0..19.9)
                adapter.setSelected(0)
            else if (bodyFat in 20.0..29.9)
                adapter.setSelected(1)
            else if (bodyFat in 30.0..44.9)
                adapter.setSelected(2)
            else if (bodyFat >= 45)
                adapter.setSelected(3)
        }
    }
}
