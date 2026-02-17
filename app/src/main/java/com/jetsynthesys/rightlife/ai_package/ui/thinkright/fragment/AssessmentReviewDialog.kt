package com.jetsynthesys.rightlife.ai_package.ui.thinkright.fragment

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.R

class AssessmentReviewDialog : BottomSheetDialogFragment(){
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_assessment_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
        val  yourTextView = view.findViewById<TextView>(R.id.tv_assessment_info)
        val  yourTextView1 = view.findViewById<TextView>(R.id.tv_assessment_info1)
        val fullText = "RightLife includes six clinically validated assessments to help you track your mental health and sleep quality. These assessments provide insights into depression, anxiety, stress, anger, happiness, and sleep disorders."
        val fullText1 = "Each assessment is scientifically backed, quick to complete, and helps you understand patterns in your well-being over time"

        val spannableString = SpannableString(fullText)
        val spannableString1 = SpannableString(fullText1)

        val boldWords = listOf(
            "mental health and sleep quality",
            "depression",
            "anxiety",
            "stress",
            "anger",
            "happiness",
            "sleep disorders"
        )
        val boldWords1 = listOf(
            "scientifically backed",

        )

        boldWords.forEach { word ->
            val start = fullText.indexOf(word)
            if (start != -1) {
                spannableString.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        boldWords1.forEach { word ->
            val start = fullText1.indexOf(word)  // ✅ Correct - fullText1
            if (start != -1) {
                spannableString1.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        yourTextView.text = spannableString
        yourTextView1.text = spannableString1
        val tvPhq9Info = view.findViewById<TextView>(R.id.tvPhq9Info)
        val tvPhq9Info1 = view.findViewById<TextView>(R.id.tvPhq9Info1)
        val tvPhq9Info2 = view.findViewById<TextView>(R.id.tvPhq9Info2)
        val tvPhq9Info3 = view.findViewById<TextView>(R.id.tvPhq9Info3)
        val tvPhq9Info4 = view.findViewById<TextView>(R.id.tvPhq9Info4)
        val tvPhq9Info5 = view.findViewById<TextView>(R.id.tvPhq9Info5)
        val tvPhq9Info6 = view.findViewById<TextView>(R.id.tvPhq9Info6)
        val fullText3 = "The Patient Health Questionnaire-9 (PHQ-9) is a widely used screening tool for depression. It assesses the severity of symptoms based on how often they have been experienced over the past two weeks.\n\n• Total Score Range: 0–27\n\n• Categories: Minimal (0–4), Mild (5–9), Moderate (10–14), Moderately Severe (15–19), Severe (20–27)\n\n• Why it matters: Higher scores may indicate a need for further evaluation or support.\n\n Tip: Regularly tracking your PHQ-9 score helps you identify trends and take proactive steps."
        val fullText9 = "Check your trends: Taking these assessments around twice a month helps you track improvements or identify concerns.\n\n" +
                "Take action: RightLife provides personalized insights to support better mental health and sleep.\n\n" +
                "Seek professional support: If your scores consistently indicate moderate to severe symptoms, consider consulting a healthcare provider.\n\n" +
                "\n\n" +
                "These assessments are designed to empower you with meaningful insights about your health. Use them regularly and take proactive steps towards better mental and physical well-being."
        val fullText8 = "The RightLife Sleep Audit is a custom assessment designed to evaluate four key sleep concerns:\n\n" +
                "Excessive Daytime Sleepiness (EDS) – Feeling unusually tired during the day\n\n" +
                "Insomnia – Difficulty falling or staying asleep\n\n" +
                "Sleep Apnea – Signs of disrupted breathing during sleep\n\n" +
                "Tiredness – Waking up feeling unrested\n\n" +
                "Why it matters: Poor sleep quality impacts physical health, mental health, and cognitive function.\n\n" +
                "\n\n" +
                "Tip: If your Sleep Audit highlights sleep issues, consider adjusting your sleep schedule, bedroom environment, or lifestyle habits"
        val fullText7 = "The Oxford Happiness Questionnaire measures overall happiness and life satisfaction.\n\n" +
                "Total Score Range: 0-6 (higher scores = greater happiness)\n\n" +
                "Why it matters: Tracking your happiness over time helps you recognize positive patterns and areas for improvement.\n\n" +
                "\n\n" +
                "Tip:  If your happiness score fluctuates, consider reflecting on daily habits, gratitude practices, or activities that bring joy."
        val fullText6 = "The Clinical Anger Scale evaluates the intensity and frequency of anger in different situations.\n\n" +
                "Total Score Range: 0-63\n\n" +
                "Why it matters: Chronic anger can affect mental and physical health, leading to stress-related issues like high blood pressure and sleep disturbances.\n" +
                "\n\n" +
                "Tip: A high score suggests that anger may be impacting daily life. Consider relaxation techniques, exercise, or therapy to manage stress responses."
        val fullText5 = "The DASS-21 is a comprehensive mental health screening that assesses three areas:\n\n" +
                "Depression\n\n" +
                "Anxiety\n\n" +
                "Stress\n\n" +
                "Each category has its own score range and interpretation.\n\n" +
                "Why it matters: Unlike PHQ-9 or GAD-7, DASS-21 provides a broader view of mental health by examining emotional distress in multiple areas.\n" +
                "\n\n" +
                "Tip: Use DASS-21 when you want a more detailed analysis of your mental well-being."
        val fullText4 = "The Generalized Anxiety Disorder-7 (GAD-7) assessment helps evaluate symptoms of generalized anxiety disorder (GAD).\n\n" +
                "Total Score Range: 0-21\n\n" +
                "Categories: Minimal (0-4), Mild (5-9), Moderate (10-14), Severe (15-21)\n\n" +
                "Why it matters: Consistently high scores may indicate persistent anxiety that can impact daily life.\n\n" +
                "\n" +
                "Tip: If your anxiety levels fluctuate, consider pairing this assessment with breathing practices and journaling, right here on the app."

        val spannableString3 = SpannableString(fullText3)
        val spannableString4 = SpannableString(fullText4)
        val spannableString5 = SpannableString(fullText5)
        val spannableString6 = SpannableString(fullText6)
        val spannableString7 = SpannableString(fullText7)
        val spannableString8 = SpannableString(fullText8)
        val spannableString9 = SpannableString(fullText9)

        val boldWords3 = listOf(
            "Patient Health Questionnaire-9 (PHQ-9)",
            "depression.",
            "Total Score Range:",
            "Categories:",
            "Why it matters:",
            "Tip:"
        )
        val boldWords4 = listOf(
            "Generalized Anxiety Disorder-7 (GAD-7)",
            "generalized anxiety disorder (GAD).",
            "Total Score Range:",
            "Categories:",
            "Why it matters:",
            "Tip:"
        )
        val boldWords5 = listOf(
            "Depression",
            "Anxiety",
            "Stress",
            "a broader view:",
            "Why it matters:",
            "Tip:"
        )
        val boldWords6 = listOf(
            "Total Score Range: 0-63",
            "Why it matters: ",
            "Tip:"
        )
        val boldWords7 = listOf(
            "Total Score Range:",
            "Why it matters: ",
            "Tip:"
        )
        val boldWords8 = listOf(
            "Excessive Daytime Sleepiness (EDS) –",
            "Insomnia – ",
            "Sleep Apnea – ",
            "Tiredness –",
            "Why it matters:",
            "Tip:"
        )
        val boldWords9 = listOf(
            "Check your trends: ",
            "Take action: ",
            "Seek professional support:  ",
            "designed to empower you ",
            "better mental and physical well-being."

        )

        boldWords3.forEach { word ->
            val start = fullText3.indexOf(word)
            if (start != -1) {
                spannableString3.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        boldWords4.forEach { word ->
            val start = fullText4.indexOf(word)
            if (start != -1) {
                spannableString4.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        boldWords5.forEach { word ->
            val start = fullText5.indexOf(word)
            if (start != -1) {
                spannableString5.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        boldWords6.forEach { word ->
            val start = fullText6.indexOf(word)
            if (start != -1) {
                spannableString6.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        boldWords7.forEach { word ->
            val start = fullText7.indexOf(word)
            if (start != -1) {
                spannableString7.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        boldWords8.forEach { word ->
            val start = fullText8.indexOf(word)
            if (start != -1) {
                spannableString8.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        boldWords9.forEach { word ->
            val start = fullText9.indexOf(word)
            if (start != -1) {
                spannableString9.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        tvPhq9Info.text = spannableString3
        tvPhq9Info1.text = spannableString4
        tvPhq9Info2.text = spannableString5
        tvPhq9Info3.text = spannableString6
        tvPhq9Info4.text = spannableString7
        tvPhq9Info5.text = spannableString8
        tvPhq9Info6.text = spannableString9
        val closeButton = view.findViewById<ImageView>(R.id.btn_close)
        val got_it = view.findViewById<Button>(R.id.got_it)
        got_it.setOnClickListener{
            dismiss()
        }
        closeButton.setOnClickListener { dismiss() }
    }

    companion object {
        fun newInstance(): AssessmentReviewDialog {
            return AssessmentReviewDialog()
        }
    }
}