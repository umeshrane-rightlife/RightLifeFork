package com.jetsynthesys.rightlife.ui.mindaudit

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityMindAuditDass21ResultBinding
import com.jetsynthesys.rightlife.ui.AppLoader
import com.jetsynthesys.rightlife.ui.YouMayAlsoLikeMindAuditAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MindAuditDass21DetailResultActivity : BaseActivity() {

    private lateinit var binding: ActivityMindAuditDass21ResultBinding
    private var selectedAssessment = "DASS-21"
    private var reportId: String? = null
    private var isFrom: String? = null
    private var isFromThinkRight = false
    private var showType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMindAuditDass21ResultBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        reportId = intent.getStringExtra("REPORT_ID")
        isFrom = intent.getStringExtra("FROM")
        isFromThinkRight = intent.getBooleanExtra("FROM_THINK_RIGHT", false)

        val assessmentHeader = intent.getStringExtra("Assessment") ?: "CAS"
        binding.tvAssessmentTaken.text = "$assessmentHeader Score"
        selectedAssessment = assessmentHeader

        getAssessmentResult(assessmentHeader)
        binding.iconBack.setOnClickListener {
            onBackPressHandle()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressHandle()
            }
        })


        binding.tvAssessmentTaken.text = "$assessmentHeader Score"
        selectedAssessment = assessmentHeader

    }


    private fun onBackPressHandle() {
        finish()
    }


    private fun getAssessmentResult(assessment: String) {
        AppLoader.show(this)
        val call =
            apiService.getMindAuditAssessmentResult(sharedPreferenceManager.accessToken, assessment)
        call.enqueue(object : Callback<MindAuditResultResponse?> {
            override fun onResponse(
                call: Call<MindAuditResultResponse?>,
                response: Response<MindAuditResultResponse?>
            ) {
                AppLoader.hide(this@MindAuditDass21DetailResultActivity)
                if (response.isSuccessful && response.body() != null) {
                    binding.rlMainLayout.visibility = View.VISIBLE
                    try {
                        val resultList = response.body()?.result

                        if (!resultList.isNullOrEmpty()) {
                            // Result exists, show result layout
                            handleAssessmentScore(response)
                        } else {
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(
                        this@MindAuditDass21DetailResultActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<MindAuditResultResponse?>, t: Throwable) {
                AppLoader.hide(this@MindAuditDass21DetailResultActivity)
                handleNoInternetView(t)
            }
        })
    }

    private fun handleAssessmentScore(response: Response<MindAuditResultResponse?>) {
        binding.cardviewMainscore.setOnClickListener(null)
        response.body()?.recommendations?.let { setupListData(it) }

        val assessmentTaken = response.body()!!.result[0].assessmentsTaken[0]
        sharedPreferenceManager.saveUserEmotions(UserEmotions(response.body()!!.result[0].emotionalState))

        // Get which interpretation to show (passed from previous activity)
        showType = intent.getStringExtra("SHOW_INTERPRETATION")?.lowercase() ?: ""

        with(binding) {
            when (assessmentTaken.assessment) {
                "DASS-21" -> {
                    when (showType.lowercase()) {
                        "anxiety" -> {
                            assessmentTaken.interpretations.anxiety?.let {
                                showAnxiety(it)
                                binding.titleToolbar.text = "Anxiety"
                            }
                            binding.tvMinimal.text = "Minimal"
                        }

                        "depression" -> {
                            assessmentTaken.interpretations.depression?.let {
                                showDepression(it, assessmentTaken)
                                binding.titleToolbar.text = "Depression"
                            }
                            binding.tvMinimal.text = "Minimal"
                        }

                        "stress" -> {
                            assessmentTaken.interpretations.stress?.let {
                                showStress(it, assessmentTaken)
                                binding.titleToolbar.text = "Stress"
                            }
                            binding.tvMinimal.text = "Normal"
                        }

                        else -> {
                            // fallback: show all if no filter passed
                            assessmentTaken.interpretations.anxiety?.let {
                                showAnxiety(it)
                            }
                            assessmentTaken.interpretations.depression?.let {
                                showDepression(
                                    it,
                                    assessmentTaken
                                )
                            }
                            assessmentTaken.interpretations.stress?.let {
                                showStress(
                                    it,
                                    assessmentTaken
                                )
                            }
                        }
                    }

                    scoreBarContainer.visibility = View.VISIBLE
                    scoreBarcard.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun showAnxiety(anxiety: Anxiety) {
        binding.cardviewMainscore.visibility = View.VISIBLE
        binding.mainScoreTitle.text = "Anxiety"
        binding.mainScoreLevel.text = anxiety.level
        binding.tvMainScore.text = anxiety.score.toString()

        binding.cardviewMainscore.setCardBackgroundColor(
            resources.getColor(getColorResForScore(anxiety.level))
        )

        setExplanationTitle(anxiety.level, "Anxiety")
        setRainbowView(anxiety.score.toInt(), showType)

        val explanation = getDASS21AnxietyExplanation(anxiety.score.toFloat())
        binding.tvResultExplanationTitle.text = explanation.first +" Anxiety"
        binding.tvResultExplanation.text = explanation.second

        // ranges
        binding.apply {
            tvRange1.text = "0"
            tvRange2.text = "7"
            tvRange3.text = "9"
            tvRange4.text = "14"
            tvRange5.text = "20+"
            tvRange6.text = ""
            tvRange5.visibility = View.VISIBLE
            tvExtSevere.visibility = View.VISIBLE
            tvRange5.gravity = Gravity.START
        }

        binding.cardviewMainscore.setOnClickListener {
            setRainbowView(anxiety.score.toInt(), showType)
            val explanationClick = getDASS21AnxietyExplanation(anxiety.score.toFloat())
            binding.tvResultExplanationTitle.text = explanationClick.first + " Anxiety"
            binding.tvResultExplanation.text = explanationClick.second
        }
        binding.cardviewMainscore2.visibility = View.GONE
        binding.cardviewMainscore3.visibility = View.GONE
    }

    private fun showDepression(depression: Depression, assessmentTaken: AssessmentsTaken) {
        binding.cardviewMainscore2.visibility = View.VISIBLE
        binding.cardviewMainscore.visibility = View.GONE
        binding.cardviewMainscore3.visibility = View.GONE
        binding.mainScoreTitle2.text = "Depression"
        binding.mainScoreLevel2.text = depression.level
        binding.tvMainScore2.text = depression.score.toString()

        binding.cardviewMainscore2.setCardBackgroundColor(
            resources.getColor(getColorResForScore(depression.level))
        )

        setExplanationTitle(depression.level, "Depression")

        val explanation = getDASS21DepressionExplanation(depression.score.toFloat())
        binding.tvResultExplanationTitle.text = explanation.first + " Depression"
        binding.tvResultExplanation.text = explanation.second

        // ranges
        binding.apply {
            tvRange1.text = "0"
            tvRange2.text = "9"
            tvRange3.text = "13"
            tvRange4.text = "20"
            tvRange5.text = "28+"
            tvRange6.text = ""
        }
        setRainbowView(depression.score.toInt(), showType)
        val explanationClick = getDASS21DepressionExplanation(depression.score.toFloat())
        binding.tvResultExplanationTitle.text = explanationClick.first + " Depression"
        binding.tvResultExplanation.text = explanationClick.second

        binding.cardviewMainscore2.setOnClickListener {
            setRainbowView(depression.score.toInt(), showType)
            val explanationClick = getDASS21DepressionExplanation(depression.score.toFloat())
            binding.tvResultExplanationTitle.text = explanationClick.first + " Depression"
            binding.tvResultExplanation.text = explanationClick.second
        }
    }

    private fun showStress(stress: Stress, assessmentTaken: AssessmentsTaken) {
        binding.cardviewMainscore3.visibility = View.VISIBLE
        binding.cardviewMainscore.visibility = View.GONE
        binding.cardviewMainscore2.visibility = View.GONE
        binding.mainScoreTitle3.text = "Stress"
        binding.mainScoreLevel3.text = stress.level
        binding.tvMainScore3.text = stress.score.toString()

        binding.cardviewMainscore3.setCardBackgroundColor(
            resources.getColor(getColorResForScore(stress.level))
        )

        setExplanationTitle(stress.level, "Stress")

        val explanation = getDASS21StressExplanation(stress.score.toFloat())
        binding.tvResultExplanationTitle.text = explanation.first + " Stress"
        binding.tvResultExplanation.text = explanation.second

        // ranges
        binding.apply {
            tvRange1.text = "0"
            tvRange2.text = "14"
            tvRange3.text = "18"
            tvRange4.text = "25"
            tvRange5.text = "34+"
            tvRange6.text = ""
            tvRange5.visibility = View.VISIBLE
            tvExtSevere.visibility = View.VISIBLE
            tvRange5.gravity = Gravity.START
        }
        setRainbowView(stress.score.toInt(), showType)
        val explanationClick = getDASS21StressExplanation(stress.score.toFloat())
        binding.tvResultExplanationTitle.text = explanationClick.first +" Stress"
        binding.tvResultExplanation.text = explanationClick.second

        binding.cardviewMainscore3.setOnClickListener {
            setRainbowView(stress.score.toInt(), showType)
            val explanationClick = getDASS21StressExplanation(stress.score.toFloat())
            binding.tvResultExplanationTitle.text = explanationClick.first + " Stress"
            binding.tvResultExplanation.text = explanationClick.second
        }
    }


    private fun setRainbowView(score: Int, showType: String) {
        binding.rainbowView.visibility = View.VISIBLE
        if (showType.lowercase() == "depression") {
            binding.rainbowView.setRainbowColors(getColorArrayForDASS_Depression_Score(score))
        } else if (showType.lowercase() == "anxiety") {
            binding.rainbowView.setRainbowColors(getColorArrayForDASS_Anxiety_Score(score))
        } else if (showType.lowercase() == "stress") {
            binding.rainbowView.setRainbowColors(getColorArrayForDASS_Stress_Score(score))
        } else {
            binding.rainbowView.setRainbowColors(getColorArrayForScore(score))
        }
        binding.rainbowView.setStrokeWidth(60f)
        binding.rainbowView.setArcSpacing(8f)
    }

    private fun setExplanationTitle(level: String?, title: String) {
        binding.tvResultExplanationTitle.text = level + " " + title
    }


    private fun getColorResForScore(score: Int): Int {
        return when (score) {
            in 0..3 -> R.color.green_minimal
            in 4..8 -> R.color.cyan_mild
            in 9..13 -> R.color.blue_moderate
            in 14..18 -> R.color.orange_severe
            else -> R.color.red_ext_severe
        }
    }

    private fun getColorResForScore(score: String): Int {
        return when (score.lowercase()) {
            "normal" -> R.color.green_minimal
            "minimal" -> R.color.green_minimal
            "mild" -> R.color.cyan_mild
            "moderate" -> R.color.blue_moderate
            "severe" -> R.color.orange_severe
            else -> R.color.red_ext_severe
        }
    }

    private fun getColorResForScoreCASandGAD(score: String): Int {
        return when (score.lowercase()) {
            "minimal" -> R.color.green_minimal
            "mild" -> R.color.blue_moderate
            "moderate" -> R.color.orange_severe
            "severe" -> R.color.red_ext_severe
            else -> R.color.red_ext_severe
        }
    }


    fun getColorArrayForScore(score: Int): IntArray {
        val colorLevels = listOf(
            0xFF06B27B.toInt(), // green_minimal
            0xFF54C8DB.toInt(), // cyan_mild
            0xFF57A3FC.toInt(), // blue_moderate
            0xFFFFBD44.toInt(), // orange_severe
            0xFFFC6656.toInt()  // red_ext_severe
        )

        val fallbackColor = 0xFFEFF0F6.toInt() // light gray for unfilled slots

        val activeColorCount = when (score) {
            in 0..3 -> 1
            in 4..8 -> 2
            in 9..13 -> 3
            in 14..18 -> 4
            else -> 5
        }

        return IntArray(5) { index ->
            if (index < activeColorCount) colorLevels[index] else fallbackColor
        }
    }

    // Get Depression color array based on DASS-21 scoring
    fun getColorArrayForDASS_Depression_Score(score: Int): IntArray {
        val colorLevels = listOf(
            0xFF06B27B.toInt(), // green_minimal
            0xFF54C8DB.toInt(), // cyan_mild
            0xFF57A3FC.toInt(), // blue_moderate
            0xFFFFBD44.toInt(), // orange_severe
            0xFFFC6656.toInt()  // red_ext_severe
        )

        val fallbackColor = 0xFFEFF0F6.toInt() // light gray for unfilled slots

        val activeColorCount = when (score) {
            in 0..9 -> 1
            in 10..13 -> 2
            in 14..20 -> 3
            in 21..27 -> 4
            else -> 5
        }

        return IntArray(5) { index ->
            if (index < activeColorCount) colorLevels[index] else fallbackColor
        }
    }

    // Get Anxiety color array based on DASS-21 scoring
    fun getColorArrayForDASS_Anxiety_Score(score: Int): IntArray {
        val colorLevels = listOf(
            0xFF06B27B.toInt(), // green_minimal
            0xFF54C8DB.toInt(), // cyan_mild
            0xFF57A3FC.toInt(), // blue_moderate
            0xFFFFBD44.toInt(), // orange_severe
            0xFFFC6656.toInt()  // red_ext_severe
        )

        val fallbackColor = 0xFFEFF0F6.toInt() // light gray for unfilled slots

        val activeColorCount = when (score) {
            in 0..7 -> 1
            in 8..9 -> 2
            in 10..14 -> 3
            in 15..19 -> 4
            else -> 5
        }

        return IntArray(5) { index ->
            if (index < activeColorCount) colorLevels[index] else fallbackColor
        }
    }

    // Get Stress color array based on DASS-21 scoring
    fun getColorArrayForDASS_Stress_Score(score: Int): IntArray {
        val colorLevels = listOf(
            0xFF06B27B.toInt(), // green_minimal
            0xFF54C8DB.toInt(), // cyan_mild
            0xFF57A3FC.toInt(), // blue_moderate
            0xFFFFBD44.toInt(), // orange_severe
            0xFFFC6656.toInt()  // red_ext_severe
        )

        val fallbackColor = 0xFFEFF0F6.toInt() // light gray for unfilled slots

        val activeColorCount = when (score) {
            in 0..14 -> 1
            in 15..18 -> 2
            in 19..25 -> 3
            in 26..33 -> 4
            else -> 5
        }

        return IntArray(5) { index ->
            if (index < activeColorCount) colorLevels[index] else fallbackColor
        }
    }


    private fun getDASS21DepressionExplanation(score: Float): Pair<String, String> {
        return when (score) {
            in 0f..9f -> "Normal" to
                    "Your score’s in the normal range, and that’s great news! Still, remember to keep a check on your well-being. A little self-care can ensure you stay on this track."

            in 10f..13f -> "Mild" to
                    "You’re experiencing mild depression. It’s a signal to take extra care of yourself. Small acts of self-care and talking about your feelings can make a big difference."

            in 14f..20f -> "Moderate" to
                    "Seems you’re in a bit of a fog. Let’s clear it up with some support, be it a heart-to-heart, a hobby, or a helping hand from a pro."

            in 21f..27f -> "Severe" to
                    "Things are feeling pretty heavy, huh? It’s okay to lean on others. A chat with a professional can be a lifeline. Remember, it’s brave to ask for help."

            in 28f..100f -> "Extremely Severe" to
                    "Things may seem tough right now, but reaching out to a professional is a strong move. Together, you can start turning things around. You’re not on this path alone."

            else -> "No Data" to "Score not within defined DASS-21 depression range."
        }
    }

    private fun getDASS21AnxietyExplanation(score: Float): Pair<String, String> {
        return when (score) {
            in 0f..7f -> "Normal" to
                    "You're in the normal range for anxiety, which is fantastic! Still, life throws curveballs, so keeping those relaxation techniques handy is always a good idea."

            in 8f..9f -> "Mild" to
                    "Experiencing mild anxiety? It's a gentle nudge that it’s time to breathe and maybe stretch out those worries. Small daily mindfulness practices can work wonders."

            in 9f..14f -> "Moderate" to
                    "Moderate anxiety can feel like carrying an extra backpack of worries. Remember, it’s okay to put it down through activities like journaling, walks, or seeking support."

            in 15f..19f -> "Severe" to
                    "Your score indicates severe anxiety. It’s a heavy load but sharing it can lighten it. Professional guidance and peer support are valuable tools on your path to calm."

            in 20f..100f -> "Extremely Severe" to
                    "Extremely severe anxiety is tough, but so are you. It’s crucial to reach out for professional help now. There’s a community ready to stand by you as you take steps towards healing."

            else -> "No Data" to "Score not within defined DASS-21 anxiety range."
        }
    }


    private fun getDASS21StressExplanation(score: Float): Pair<String, String> {
        return when (score) {
            in 0f..14f -> "Normal" to
                    "Your stress levels are looking normal—a sign of smooth sailing. Keep those stress-busting activities close to maintain this peaceful vibe."

            in 15f..18f -> "Mild" to
                    "A hint of mild stress detected. It might be life's way of saying, 'Let's find more moments to unwind.' A quick walk or a fun hobby could be your perfect antidote."

            in 19f..25f -> "Moderate" to
                    "Dealing with moderate stress? It can feel like juggling more balls than you have hands. Time to set some down and focus on your breath. Why not try a meditation on our app or a relaxing playlist?"

            in 26f..33f -> "Severe" to
                    "Severe stress is no small feat to manage. But remember, asking for help is a strength, not a weakness. This could be a good time to explore stress management techniques or talk to someone who can help."

            in 34f..100f -> "Extremely Severe" to
                    "Facing extremely severe stress is challenging, but you're not alone. Professional support can offer new strategies to navigate through this storm. Together, let’s find your way back to a less stressful life."

            else -> "No Data" to "Score not within defined DASS-21 stress range."
        }
    }

    // more like this content
    private fun setupListData(contentList: List<Recommendation>) {
        if (contentList.isEmpty()) {
            binding.rlMoreLikeSection.visibility = View.GONE
            binding.recyclerParent.visibility = View.GONE
        } else {
            binding.rlMoreLikeSection.visibility = View.VISIBLE
            binding.recyclerParent.visibility = View.VISIBLE
        }
        val adapter = YouMayAlsoLikeMindAuditAdapter(this, contentList)
        val horizontalLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = horizontalLayoutManager
        binding.recyclerView.adapter = adapter
    }

}