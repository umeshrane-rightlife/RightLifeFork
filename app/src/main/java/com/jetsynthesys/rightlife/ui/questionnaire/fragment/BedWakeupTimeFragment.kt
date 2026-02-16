package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.FragmentBedWakeupTimeNewBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireThinkRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.SRQuestionThree
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.SleepTimeAnswer
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import com.shawnlin.numberpicker.NumberPicker
import java.time.Duration
import java.time.LocalTime

class BedWakeupTimeFragment : Fragment() {
    private lateinit var binding: FragmentBedWakeupTimeNewBinding

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): BedWakeupTimeFragment {
            val fragment = BedWakeupTimeFragment()
            val args = Bundle().apply {
                putSerializable("question", question)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            question = it.getSerializable("question") as? Question
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBedWakeupTimeNewBinding.inflate(inflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val minutesArray = Array(60) { i -> String.format("%02d", i) }
        val amPmArray = arrayOf("AM", "PM")
        val updateAction = { updateSleepDuration() }

        binding.apply {
            // Hour Pickers
            bedTimeHourPicker.setupPicker(1, 12, 10, onChanged = updateAction)
            wakeUpHourPicker.setupPicker(1, 12, 6, onChanged = updateAction)

            // Minute Pickers
            bedTimeMinutePicker.setupPicker(0, 59, 0, minutesArray, updateAction)
            wakeUpMinutePicker.setupPicker(0, 59, 0, minutesArray, updateAction)

            // AM/PM Pickers
            bedTimeAmPmPicker.setupPicker(0, 1, 1, amPmArray, updateAction)
            wakeUpAmPmPicker.setupPicker(0, 1, 0, amPmArray, updateAction)
        }


        // initial compute + button state
        updateSleepDuration()

        binding.btnContinue.setOnClickListener {
            binding.btnContinue.disableViewForSeconds()

            // prevent submit if equal times
            if (areTimesEqual(
                    binding.bedTimeHourPicker,
                    binding.bedTimeMinutePicker,
                    if (binding.bedTimeAmPmPicker.value == 0) "AM" else "PM",
                    binding.wakeUpHourPicker,
                    binding.wakeUpMinutePicker,
                    if (binding.wakeUpAmPmPicker.value == 0) "AM" else "PM"
                )
            ) {
                // Optional: button can be disabled, if product requires
                Utils.showNewDesignToast(
                    requireContext(),
                    "Bedtime and Wake time cannot be the same.",
                    false
                )
                return@setOnClickListener
            }

            val sleepTimeAnswer = SleepTimeAnswer().apply {
                bedTime = getSelectedTimeFromTimePicker(
                    binding.bedTimeHourPicker,
                    binding.bedTimeMinutePicker,
                    binding.bedTimeAmPmPicker
                )
                wakeTime = getSelectedTimeFromTimePicker(
                    binding.wakeUpHourPicker,
                    binding.wakeUpMinutePicker,
                    binding.wakeUpAmPmPicker
                )
                sleepDuration = binding.tvSleepDuration.text.toString()
            }
            submit(sleepTimeAnswer)
        }
    }

    private fun updateSleepDuration() {
        val bedtime = getSelectedTime(
            binding.bedTimeHourPicker,
            binding.bedTimeMinutePicker,
            binding.bedTimeAmPmPicker
        )
        val wakeTime = getSelectedTime(
            binding.wakeUpHourPicker,
            binding.wakeUpMinutePicker,
            binding.wakeUpAmPmPicker
        )

        if (bedtime == wakeTime) {
            // Same time → block and inform
            binding.tvSleepDuration.text = "0 hrs 0 mins"
            //binding.btnContinue.isEnabled = false
            return
        }

        val bedtimeLocal = LocalTime.of(bedtime.first, bedtime.second)
        val wakeTimeLocal = LocalTime.of(wakeTime.first, wakeTime.second)

        var duration = Duration.between(bedtimeLocal, wakeTimeLocal)
        if (duration.isNegative) duration = duration.plusHours(24)

        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        binding.tvSleepDuration.text = "${hours} hrs ${minutes} mins"

        // Enable when valid
        binding.btnContinue.isEnabled = true
    }

    private fun areTimesEqual(
        hourPickerA: NumberPicker,
        minutePickerA: NumberPicker,
        amPmA: String,
        hourPickerB: NumberPicker,
        minutePickerB: NumberPicker,
        amPmB: String
    ): Boolean {

        return toMinutesSinceMidnight(hourPickerA, minutePickerA, amPmA) ==
                toMinutesSinceMidnight(hourPickerB, minutePickerB, amPmB)
    }

    private fun toMinutesSinceMidnight(
        hourPicker: NumberPicker,
        minutePicker: NumberPicker,
        amPm: String
    ): Int {

        var hour = hourPicker.value  // 1–12
        val minute = minutePicker.value

        // Convert to 24-hour
        if (hour == 12) hour = 0
        if (amPm.equals("PM", true)) hour += 12

        return hour * 60 + minute
    }


    private fun getSelectedTimeFromTimePicker(
        hourPicker: NumberPicker,
        minutePicker: NumberPicker,
        amPmPicker: NumberPicker
    ): String {
        val hour = hourPicker.value

        val minute = minutePicker.value

        // Convert to 12-hour format
        val formattedHour = if (hour % 12 == 0) 12 else hour % 12

        val amPm = if (amPmPicker.value == 0) "AM" else "PM"

        // Ensure two-digit format
        return String.format("%02d:%02d %s", formattedHour, minute, amPm)
    }


    private fun getSelectedTime(
        hourPicker: NumberPicker,
        minutePicker: NumberPicker,
        amPmPicker: NumberPicker
    ): Pair<Int, Int> {

        var hour = hourPicker.value   // 1–12
        val minute = minutePicker.value
        val isPm = amPmPicker.value == 1  // 0 = AM, 1 = PM

        // Convert to 24-hour format
        if (hour == 12) hour = 0
        if (isPm) hour += 12

        return Pair(hour, minute) // 24-hour safe
    }


    private fun submit(answer: SleepTimeAnswer) {
        val questionThree = SRQuestionThree()
        questionThree.answer = answer
        QuestionnaireThinkRightActivity.questionnaireAnswerRequest.sleepRight?.questionThree =
            questionThree
        QuestionnaireThinkRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireThinkRightActivity.questionnaireAnswerRequest
        )
    }

    private fun NumberPicker.setupPicker(
        min: Int,
        max: Int,
        initialValue: Int,
        displayValues: Array<String>? = null,
        onChanged: () -> Unit
    ) {
        minValue = min
        maxValue = max
        value = initialValue
        if (displayValues != null) {
            this.displayedValues = displayValues
        }
        wheelItemCount = 3 // Assuming this is a property of your custom picker
        typeface = ResourcesCompat.getFont(context, R.font.dmsans_regular)
        setSelectedTypeface(ResourcesCompat.getFont(context, R.font.dmsans_bold))
        setOnValueChangedListener { _, _, _ -> onChanged() }
        isFadingEdgeEnabled = false
    }


}