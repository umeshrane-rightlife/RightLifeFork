package com.jetsynthesys.rightlife.ui.breathwork


import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityBreathworkPracticeBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetBreathworkCompleteBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetEarlyFinishBinding
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.breathwork.pojo.BreathingData
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import java.time.Instant
import java.time.format.DateTimeFormatter

class BreathworkPracticeActivity : BaseActivity() {
    var breathInText = ""
    var holdText = ""
    var breathOutText = ""
    var isLeftActive = false
    var breathLeftText = ""
    var breathRightText = ""
    var alternateHoldText = ""

    private lateinit var binding: ActivityBreathworkPracticeBinding
    var lastClickTime = 0L

    private var totalSets = 3
    private var currentSet = 1
    private var sessionDurationSeconds = 135 // Default session duration
    private var countDownTimer: CountDownTimer? = null
    private var preparationCountdownTimer: CountDownTimer? = null
    private var breathingData: BreathingData? = null
    private var inhaleTime: Long = 0
    private var exhaleTime: Long = 0
    private var holdTime: Long = 0
    private var startDate = ""
    private var startTime: Long = 0
    private var endTime: Long = 0


    // Sound-related properties
    private var inhaleSound: MediaPlayer? = null
    private var exhaleSound: MediaPlayer? = null
    private var holdSound: MediaPlayer? = null
    private var isSoundEnabled = true
    private var isPreparationPhase = true  // Starts as true
    private var isHapticFeedBack = false
    private var sessionTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreathworkPracticeBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        startTime = System.currentTimeMillis()

        // Retrieve the selected breathing practice from the intent
        breathingData = intent.getSerializableExtra("BREATHWORK") as BreathingData
        val sessionCount = intent.getIntExtra("sessionCount", 3)
        totalSets = sessionCount
        isHapticFeedBack = intent.getBooleanExtra("HAPTIC_FEEDBACK", false)
        // Safely convert to Long, defaulting to 0L if null, then multiply
        inhaleTime = (breathingData?.breathInhaleTime?.toLong() ?: 0L) * 1000
        exhaleTime = (breathingData?.breathExhaleTime?.toLong() ?: 0L) * 1000
        holdTime = (breathingData?.breathHoldTime?.toLong() ?: 0L) * 1000

        // Initialize sound cues
        initializeSoundCues()

        // Calculate session duration based on the selected practice
        val cycleDuration = inhaleTime +
                holdTime +
                exhaleTime +
                (if (breathingData?.title == "Box Breathing") holdTime else 0L)
        sessionDurationSeconds = (totalSets * cycleDuration / 1000).toInt()

        // Set initial values
        binding.setIndicator.text = "Set $currentSet/${totalSets / 4}"
        updateSessionTimer(sessionDurationSeconds * 1000L)

        // Set click listeners
        binding.backButton.setOnClickListener {
            //onBackPressed()
            if (binding.rlPracticeComplete.isVisible) {
                showCompletedBottomSheet()
                callPostMindFullDataAPI()
            } else
                showDeleteBottomSheet()
        }
        binding.finishEarlyButton.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > 500) { // 500ms debounce
                lastClickTime = currentTime
                showDeleteBottomSheet()
            }

        }

        // Start the breathing session
        //startSessionTimer()
        //startBreathingCycle()
        // Start the preparation countdown
        startPreparationCountdown()
        setBreathingTypeColors()
        setBreathingTexts(breathingData?.title ?: "Custom")
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        if (binding.rlPracticeComplete.isVisible) {
            showCompletedBottomSheet()
            callPostMindFullDataAPI()
        } else
            showDeleteBottomSheet()
    }

    /**
     * Start the preparation countdown (3, 2, 1) before breathing begins
     */
    private fun startPreparationCountdown() {
        isPreparationPhase = true
        binding.circleView.visibility = View.GONE
        binding.circleViewbase.visibility = View.GONE
        // Hide breathing-specific UI elements during countdown
        binding.setIndicator.alpha = 0.1f
        binding.sessionTimer.alpha = 0.1f
        binding.setIndicator.visibility = View.GONE
        binding.sessionTimer.visibility = View.GONE

        // Make the countdown very prominent
        binding.breathingPhase.text = "Get Ready"
        //binding.circleTimer.textSize = 80f
        //binding.circleTimer.text = "3"
        binding.tvPrepareTimer.text = "3"
        binding.rlPrepareMain.visibility = View.VISIBLE
        if (breathingData?.title == "Alternate Nostril Breathing") {
            binding.tvPrepareMessage.text = "Gently close your right nostril with your thumb"
        } else {
            binding.tvPrepareMessage.text = "Relax and Settle In"
        }


        var countdownValue = 4

        // Play initial countdown sound
        playSoundCue(BreathingPhase.COUNTDOWN)

        preparationCountdownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdownValue--
                if (countdownValue > 0) {
                    binding.tvPrepareTimer.text = countdownValue.toString()
                    playSoundCue(BreathingPhase.COUNTDOWN)

                    // Add pulse animation for countdown
                    val pulseAnimation = ScaleAnimation(
                        1.2f, 1.4f, 1.2f, 1.4f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f
                    )
                    pulseAnimation.duration = 300
                    pulseAnimation.repeatMode = Animation.REVERSE
                    pulseAnimation.repeatCount = 1
                    //binding.circleView.startAnimation(pulseAnimation)
                }
            }

            override fun onFinish() {
                // Show "Begin" briefly before starting
                binding.circleTimer.text = ""
                binding.breathingPhase.text = "Let's Start"
                binding.circleView.visibility = View.VISIBLE
                binding.circleViewbase.visibility = View.VISIBLE
                // Final countdown sound or different start sound
                playSoundCue(BreathingPhase.COUNTDOWN)

                // Quick scale animation
                /*val startAnimation = ScaleAnimation(
                    1.4f, 1.0f, 1.4f, 1.0f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f
                )
                startAnimation.duration = 500
                binding.circleView.startAnimation(startAnimation)*/
                binding.rlPrepareMain.visibility = View.GONE
                binding.rlBreathingPracticeMain.visibility = View.VISIBLE
                // Wait a moment then start breathing
                binding.circleView.postDelayed({
                    binding.breathingPhase.text = ""
                    startBreathingSession()
                }, 800)
            }
        }.start()
    }

    private fun startBreathingSession() {
        if (startDate.isEmpty())
            startDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        isPreparationPhase = false

        // Restore UI elements
        binding.setIndicator.alpha = 1.0f
        binding.sessionTimer.alpha = 1.0f
        binding.setIndicator.visibility = View.VISIBLE
        binding.sessionTimer.visibility = View.VISIBLE
        //binding.circleTimer.textSize = 48f // Reset to normal size

        // Start the breathing session
        startSessionTimer()
        startBreathingCycle()
    }

    /**
     * Initialize sound cues for breathing phases
     * You'll need to add these sound files to your res/raw folder:
     * - inhale_sound.mp3 (gentle rising tone)
     * - exhale_sound.mp3 (gentle falling tone)
     * - hold_sound.mp3 (soft ambient tone or silence)
     */
    private fun initializeSoundCues() {
        try {
            // Initialize inhale sound - you can use a default system sound or custom audio file
            inhaleSound = MediaPlayer.create(this, R.raw.inhale_w)
            exhaleSound = MediaPlayer.create(this, R.raw.exhale_w)
            holdSound = MediaPlayer.create(this, R.raw.hold_w)

            // Set volume levels (0.0 to 1.0)
            inhaleSound?.setVolume(0.7f, 0.7f)
            exhaleSound?.setVolume(0.7f, 0.7f)
            holdSound?.setVolume(0.5f, 0.5f)

        } catch (e: Exception) {
            // Handle case where sound files are not found
            e.printStackTrace()
            isSoundEnabled = false
        }
    }

    /**
     * Play sound cue for specific breathing phase
     */
    private fun playSoundCue(phase: BreathingPhase) {
        if (!isSoundEnabled) return

        try {
            when (phase) {
                BreathingPhase.INHALE -> {
                    stopAllSounds()
                    inhaleSound?.start()
                }

                BreathingPhase.EXHALE -> {
                    stopAllSounds()
                    exhaleSound?.start()
                }

                BreathingPhase.HOLD -> {
                    stopAllSounds()
                    holdSound?.start()
                }

                else -> { // No sound for countdown or unrecognized phase
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Stop all currently playing sounds
     */
    private fun stopAllSounds() {
        try {
            inhaleSound?.let {
                if (it.isPlaying) {
                    it.stop()
                    it.prepare()
                }
            }
            exhaleSound?.let {
                if (it.isPlaying) {
                    it.stop()
                    it.prepare()
                }
            }
            holdSound?.let {
                if (it.isPlaying) {
                    it.stop()
                    it.prepare()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun startSessionTimer() {
        // Cancel any existing timer to avoid multiple timers
        sessionTimer?.cancel()

        sessionTimer = object : CountDownTimer(sessionDurationSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateSessionTimer(millisUntilFinished)
            }

            override fun onFinish() {
                binding.sessionTimer.text = "00:00"
            }
        }.start()
    }

    private fun updateSessionTimer(millisUntilFinished: Long) {
        val minutes = (millisUntilFinished / 1000) / 60
        val seconds = (millisUntilFinished / 1000) % 60
        binding.sessionTimer.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun startBreathingCycle() {
        if (currentSet <= totalSets) {
            AnalyticsLogger.logEvent(
                this,
                AnalyticsEvent.BREATHING_SESSION_STARTED, mapOf(
                    AnalyticsParam.BREATHING_TYPE_ID to (breathingData?.id?:""),
                    AnalyticsParam.BREATHING_SESSION_DURATION to (breathingData?.duration?:0)
                )
            )
            //binding.setIndicator.text = "Set ${currentSet/4}/${totalSets/4}"
            // Calculate the current "lap" of 4, starting from 1
            val currentLap = ((currentSet - 1) / 4) + 1
            val totalLaps = totalSets / 4

            binding.setIndicator.text = "Set $currentLap/$totalLaps"

            startBreathIn()
        } else {
            //finish()
            CommonAPICall.postWellnessStreak(this, "breathing")
            //showCompletedBottomSheet()
            showCompletedBottomSheetNew()
            AnalyticsLogger.logEvent(
                this,
                AnalyticsEvent.BREATHING_SESSION_COMPLETED, mapOf(
                    AnalyticsParam.BREATHING_TYPE_ID to (breathingData?.id?:""),
                    AnalyticsParam.BREATHING_SESSION_DURATION to System.currentTimeMillis() - startTime
                )
            )
            logBreathingCompletEvent(breathingData)
        }
    }

    private fun logBreathingCompletEvent(breathingData: BreathingData?) {
        val breathingType = breathingData?.title?.trim() ?: ""

// 🎯 Map breathing type → event name
        val eventName = when (breathingType) {
            "Box Breathing" -> AnalyticsEvent.TR_BoxBreathing_Completed
            "Alternate Nostril Breathing" -> AnalyticsEvent.TR_AlternateNostril_Completed
            "4-7-8 Breathing" -> AnalyticsEvent.TR_478Breathing_Completed
            "Custom" -> AnalyticsEvent.TR_CustomBreathing_Completed
            else -> AnalyticsEvent.TR_CustomBreathing_Completed // fallback
        }

// ✅ Example: log the breathing completion event
        val duration = System.currentTimeMillis() - startTime
        AnalyticsLogger.logEvent(
            this,
            eventName,
            mapOf(
                AnalyticsParam.BREATHING_TYPE_NAME to breathingType,
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                AnalyticsParam.TOTAL_DURATION to duration
            )
        )

    }

    private fun showCompletedBottomSheetNew() {
        callPostMindFullDataAPI()
        binding.rlPracticeComplete.visibility = View.VISIBLE
        binding.rlBreathingPracticeMain.visibility = View.GONE
        binding.btnExit.setOnClickListener {
            showCompletedBottomSheet()
            /*
            if (isHapticFeedBack)
                showCompletedBottomSheet()
            else {
                callPostMindFullDataAPI()
                finish()
            }*/
        }
        binding.btnRepeat.setOnClickListener {
            // Reset for a new session
            currentSet = 1
            binding.rlPracticeComplete.visibility = View.GONE
            startPreparationCountdown()
        }
    }

    private fun startBreathIn() {
        setBreathingTexts(breathingData?.title ?: "Custom")
        binding.breathingPhase.text = breathInText
        playSoundCue(BreathingPhase.INHALE) // Play inhale sound
        animateCircle(1f, 1.5f, inhaleTime)
        // For Alternate Nostril Breathing, show nostril switch instruction 1 second early
        if (breathingData?.title == "Alternate Nostril Breathing" && inhaleTime > 1000) {
            startCountdownWithNostrilSwitch(inhaleTime) {
                if (holdTime > 0) startHold() else startBreathOut()
            }
        } else {
            startCountdown(inhaleTime) {
                if (holdTime > 0) startHold() else startBreathOut()
            }
        }
        /*  startCountdown(inhaleTime) {
              if (holdTime > 0) startHold() else startBreathOut()
          }*/
    }

    private fun startHold() {
        //setBreathingTexts(breathingData?.title ?: "Custom")
        binding.breathingPhase.text = holdText
        playSoundCue(BreathingPhase.HOLD) // Play hold sound
        animateCircle(1.5f, 1.5f, holdTime)
        startCountdown(holdTime) { startBreathOut() }
    }

    private fun startBreathOut() {
        //setBreathingTexts(breathingData?.title ?: "Custom")
        binding.breathingPhase.text = breathOutText
        playSoundCue(BreathingPhase.EXHALE) // Play exhale sound
        animateCircle(1.5f, 1f, exhaleTime)
        startCountdown(exhaleTime) {
            if (breathingData?.title == "Box Breathing") {
                startFinalHold()
            } else {
                currentSet++
                startBreathingCycle()
            }
        }
    }

    private fun startFinalHold() {
        //setBreathingTexts(breathingData?.title ?: "Custom")
        binding.breathingPhase.text = "Hold"
        playSoundCue(BreathingPhase.HOLD) // Play exhale sound
        animateCircle(1f, 1f, holdTime)
        startCountdown(holdTime) {
            currentSet++
            startBreathingCycle()
        }
    }

    private fun startCountdown(duration: Long, onFinish: () -> Unit) {
        var secondsLeft = 0
        binding.circleTimer.text = secondsLeft.toString()
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsLeft++
                binding.circleTimer.text = secondsLeft.toString()
            }

            override fun onFinish() {
                onFinish()
            }
        }.start()
    }

    // New method: Countdown with nostril switch instruction
    private fun startCountdownWithNostrilSwitch(duration: Long, onFinish: () -> Unit) {
        var secondsLeft = 0
        val totalSeconds = (duration / 1000).toInt()
        binding.breathingPhase.text.toString()

        binding.circleTimer.text = secondsLeft.toString()

        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsLeft++
                binding.circleTimer.text = secondsLeft.toString()

                // Show nostril switch instruction 1 second before completion
                if (secondsLeft == totalSeconds - 1) {
                    binding.breathingPhase.text = alternateHoldText
                }
            }

            override fun onFinish() {
                onFinish()
            }
        }.start()
    }

    private fun animateCircle(fromScale: Float, toScale: Float, duration: Long) {
        val scaleAnimation = ScaleAnimation(
            fromScale, toScale, fromScale, toScale,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnimation.duration = duration
        scaleAnimation.fillAfter = true
        binding.circleView.startAnimation(scaleAnimation)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        // Clean up MediaPlayer resources
        releaseSoundResources()
    }


    /**
     * Release all MediaPlayer resources
     */
    private fun releaseSoundResources() {
        try {
            inhaleSound?.release()
            exhaleSound?.release()
            holdSound?.release()

            inhaleSound = null
            exhaleSound = null
            holdSound = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Toggle sound on/off (you can add a UI button for this)
     */
    fun toggleSound() {
        isSoundEnabled = !isSoundEnabled
        if (!isSoundEnabled) {
            stopAllSounds()
        }
    }

    /*show erly finish bottomsheet*/
    private fun showDeleteBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetEarlyFinishBinding.inflate(layoutInflater)
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

        dialogBinding.tvTitle.text = "Leaving early?"
        dialogBinding.tvDescription.text =
            "A few more minutes of breathing practise will make a world of difference."

        dialogBinding.btnCancel.text = "Continue Practise"
        dialogBinding.btnYes.text = "Leave"

        dialogBinding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnYes.setOnClickListener {
            //deleteJournal(journalEntry)
            countDownTimer?.cancel()
            countDownTimer = null
            sessionTimer?.cancel()
            sessionTimer = null
            bottomSheetDialog.dismiss()
            callPostMindFullDataAPI()
            //finish()
            showCompletedBottomSheet()
        }
        bottomSheetDialog.show()
    }


    private fun showCompletedBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetBreathworkCompleteBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)


        // Set up the animation
        val bottomSheetLayout =
            bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet2)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }
        dialogBinding.tvTitle.text = "How do you feel after the exercise?"
        dialogBinding.tvDescription.text =
            "A few more minutes of breathing practise will make a world of difference."

        dialogBinding.btnBetter.text = "better than before"
        dialogBinding.btnSame.text = "same as before"
        dialogBinding.btnWorse.text = "worse than before"

        bottomSheetDialog.setOnDismissListener {
            startActivity(
                Intent(this, BreathworkActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            finish()
        }

        bottomSheetDialog.setOnCancelListener {
            finish()
        }

        dialogBinding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
            finish()
        }

        dialogBinding.btnBetter.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnSame.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        dialogBinding.btnWorse.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun callPostMindFullDataAPI() {
        val endDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        CommonAPICall.postMindFullData(
            this,
            breathingData?.title ?: "Breathing",
            startDate,
            endDate
        )
    }

    /**
     * Enum to define breathing phases for sound cues
     */
    enum class BreathingPhase {
        INHALE,
        EXHALE,
        HOLD,
        COUNTDOWN
    }

    // Add this method to your BreathworkPracticeActivity class

    private fun setBreathingTypeColors() {
        // Get the breathing type from the breathingData
        val breathingType = breathingData?.title?.trim() ?: ""

        // Get the color resource based on breathing type
        val colorResource = when (breathingType) {
            "Box Breathing" -> R.color.box_breathing_card_color
            "Alternate Nostril Breathing" -> R.color.alternate_breathing_card_color
            "4-7-8 Breathing" -> R.color.four_seven_breathing_card_color
            "Custom" -> R.color.custom_breathing_card_color
            else -> R.color.white
        }
        val colorResourceText = when (breathingType) {
            "Box Breathing" -> R.color.box_breathing_card_color_text
            "Alternate Nostril Breathing" -> R.color.alternate_breathing_card_color_text
            "4-7-8 Breathing" -> R.color.four_seven_breathing_card_color_text
            "Custom" -> R.color.custom_breathing_card_color_text
            else -> R.color.alternate_breathing_card_color_text
        }

        // Get the actual color value
        val mainColor = resources.getColor(colorResource, null)

        // Create a lighter version for the base circle (outer circle)
        // This adds transparency to make it lighter
        val baseColor = adjustAlpha(mainColor, 0.3f) // 30% opacity for lighter shade

        // Apply the colors to the views
        binding.circleViewbase.backgroundTintList = ColorStateList.valueOf(baseColor)
        binding.circleView.backgroundTintList = ColorStateList.valueOf(mainColor)

        binding.btnRepeat.backgroundTintList = ColorStateList.valueOf(mainColor)
        binding.btnRepeat.setTextColor(resources.getColor(colorResourceText, null))
    }

    // Helper function to adjust color alpha (transparency)
    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    // Alternative: If you want to lighten the color instead of making it transparent
    private fun lightenColor(color: Int, factor: Float = 0.3f): Int {
        val red = ((Color.red(color) * (1 - factor) + 255 * factor)).toInt()
        val green = ((Color.green(color) * (1 - factor) + 255 * factor)).toInt()
        val blue = ((Color.blue(color) * (1 - factor) + 255 * factor)).toInt()
        return Color.argb(Color.alpha(color), red, green, blue)
    }


    // Function to set texts based on breathing type
    fun setBreathingTexts(breathingType: String) {
        when (breathingType) {
            "Box Breathing" -> {
                breathInText = "Breathe In"
                holdText = "Hold"
                breathOutText = "Breathe Out"
            }

            "Alternate Nostril Breathing" -> {
                if (isLeftActive) {
                    breathInText = "Inhale slowly through your left nostril..."
                    alternateHoldText = "Now close your left nostril."
                    holdText = "Hold"
                    breathOutText = "Exhale through your right nostril.."
                    isLeftActive = false
                } else {
                    breathInText = "Inhale slowly through your right nostril..."
                    alternateHoldText = "Close the right nostril.."
                    holdText = "Hold"
                    breathOutText = "Exhale through your left nostril.."
                    isLeftActive = true
                }
            }

            "4-7-8 Breathing" -> {
                breathInText = "Breathe In"
                holdText = "Hold"
                breathOutText = "Breathe Out"
            }

            "Custom" -> {
                breathInText = "Breathe In"
                holdText = "Hold"
                breathOutText = "Breathe Out"
            }

            else -> {
                breathInText = ""
                holdText = ""
                breathOutText = ""
            }
        }
    }

    /**
     * Pauses all currently playing sounds without stopping or releasing them.
     */
    private fun pauseAllSounds() {
        try {
            if (inhaleSound?.isPlaying == true) {
                inhaleSound?.pause()
            }
            if (exhaleSound?.isPlaying == true) {
                exhaleSound?.pause()
            }
            if (holdSound?.isPlaying == true) {
                holdSound?.pause()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        muteAllSounds(true)
    }

    override fun onResume() {
        super.onResume()
        muteAllSounds(false)

    }

    /**
     * Mutes or unmutes all MediaPlayer instances.
     * @param isMuted True to mute (set volume to 0), false to unmute (set to default volume).
     */
    private fun muteAllSounds(isMuted: Boolean) {
        try {
            val volume = if (isMuted) 0f else 0.7f // 0f for mute, 0.7f for default volume
            inhaleSound?.setVolume(volume, volume)
            exhaleSound?.setVolume(volume, volume)
            holdSound?.setVolume(volume, volume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
