package com.jetsynthesys.rightlife.ai_package.ui.thinkright.fragment

import android.Manifest
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresPermission
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.snackbar.Snackbar
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.AffirmationPlaylistResponse
import com.jetsynthesys.rightlife.ai_package.model.JournalAnswerData
import com.jetsynthesys.rightlife.ai_package.model.JournalAnswerResponse
import com.jetsynthesys.rightlife.ai_package.model.MindfullData
import com.jetsynthesys.rightlife.ai_package.model.MindfullResponse
import com.jetsynthesys.rightlife.ai_package.model.ModuleData
import com.jetsynthesys.rightlife.ai_package.model.ModuleResponse
import com.jetsynthesys.rightlife.ai_package.model.ThinkQuoteResponse
import com.jetsynthesys.rightlife.ai_package.model.ThinkRecomendedResponse
import com.jetsynthesys.rightlife.ai_package.model.ToolGridData
import com.jetsynthesys.rightlife.ai_package.model.ToolsData
import com.jetsynthesys.rightlife.ai_package.model.ToolsGridResponse
import com.jetsynthesys.rightlife.ai_package.model.ToolsResponse
import com.jetsynthesys.rightlife.ai_package.model.response.BreathingResponse
import com.jetsynthesys.rightlife.ai_package.ui.moveright.MindfulnessReviewDialog
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.model.AssessmentResponse
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.model.AssessmentResult
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.Phq9Assessment
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.SeverityLevel
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.adapter.MoreToolsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.adapter.RecommendationAdapter
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.adapter.TagAdapter
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.adapter.ToolAdapter
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.adapter.ToolsAdapter
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse
import com.jetsynthesys.rightlife.databinding.FragmentThinkRightLandingBinding
import com.jetsynthesys.rightlife.ui.ActivityUtils
import com.jetsynthesys.rightlife.ui.affirmation.PractiseAffirmationPlaylistActivity
import com.jetsynthesys.rightlife.ui.aireport.AIReportWebViewActivity
import com.jetsynthesys.rightlife.ui.breathwork.BreathworkSessionActivity
import com.jetsynthesys.rightlife.ui.breathwork.pojo.BreathingData
import com.jetsynthesys.rightlife.ui.jounal.new_journal.JournalNewActivity
import com.jetsynthesys.rightlife.ui.mindaudit.MASuggestedAssessmentActivity
import com.jetsynthesys.rightlife.ui.mindaudit.MindAuditResultActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Locale.getDefault
import kotlin.math.abs

class ThinkRightReportFragment : BaseFragment<FragmentThinkRightLandingBinding>() {

    private lateinit var viewPager: ViewPager2
    private lateinit var dotsLayout: LinearLayout
    private lateinit var adapter: AssessmentPagerAdapter
    private lateinit var add_tools_think_right: ImageView
    private lateinit var instruction_your_mindfullness_review: ImageView
    private lateinit var instruction_your_assessment_review: ImageView
    private lateinit var dots: Array<ImageView?>
    private lateinit var tvQuote: TextView
    private lateinit var tvMindfullMinute: TextView
    private lateinit var tvWellnessDays: TextView
    private lateinit var tvJournalDesc: TextView
    private lateinit var tvJournalDate: TextView
    private lateinit var tvJournalTime: TextView
    private lateinit var moodTrackBtn: ImageView
    private lateinit var mindfullArrowBtn: ImageView
    private lateinit var tvAuthor: TextView
    private lateinit var cardAddTools: CardView
    private lateinit var breathingCard: CardView
    private lateinit var emotionCardData: CardView
    private lateinit var emotionCardNoData: CardView
    private lateinit var cardAffirmations: CardView
    private lateinit var journalingCard: CardView
    private lateinit var mindfullNoDataCard: CardView
    private lateinit var mindfullDataCard: CardView
    private lateinit var toolsRecyclerView: RecyclerView
    private lateinit var journalingRecyclerView: RecyclerView
    private lateinit var noDataMindFullnessMetric: ConstraintLayout
    private lateinit var noDataMindAudit: ConstraintLayout
    private lateinit var dataFilledMindAudit: LinearLayout
    private lateinit var reassessYourMental: LinearLayout

    private lateinit var data: UserProfileResponse
    private var noData: Boolean = false
    private lateinit var thinkQuoteResponse: ThinkQuoteResponse
    private lateinit var thinkRecomendedResponse: ThinkRecomendedResponse
    private lateinit var toolRecyclerView: RecyclerView
    private lateinit var toolAdapter: ToolAdapter
    private val toolsList: ArrayList<ModuleData> = arrayListOf()
    private var assessmentList: MutableList<AssessmentResultData> = mutableListOf()

    //   private val toolsAdapter by lazy { ToolsAdapter(requireContext(), 3) }
    private lateinit var toolsResponse: ToolsResponse
    private lateinit var mainView: LinearLayout
    private lateinit var lytAffirmation1: LinearLayout
    private lateinit var lytAffirmation2: LinearLayout
    private lateinit var lytAffirmation3: LinearLayout
    private lateinit var downloadView: ImageView
    private var toolsArray: ArrayList<ToolsData> = arrayListOf()
    private lateinit var toolGridResponse: ToolsGridResponse
    private var toolsGridArray: ArrayList<ToolGridData> = arrayListOf()
    private lateinit var mindfullResponse: MindfullResponse
    private lateinit var journalResponse: JournalAnswerResponse
    private lateinit var breathingResponse: BreathingResponse
    private lateinit var journalAnswerData: JournalAnswerData
    private var mindfullData: ArrayList<MindfullData> = arrayListOf()
    private lateinit var toolsAdapter: ToolsAdapter
    private val toolsMoreAdapter by lazy { MoreToolsAdapter(requireContext(), 4) }
    private lateinit var recomendationRecyclerView: RecyclerView
    private lateinit var recomendationAdapter: RecommendationAdapter
    var itemCount = 0
    var dotSize = 16
    var dotMargin = 8
    private lateinit var affirmationCount: TextView
    private lateinit var emotionLabel: TextView
    private lateinit var emotionTime: TextView
    private lateinit var emotionDescription: TextView
    private lateinit var tvBreathingTitle1: TextView
    private lateinit var tvBreathingTitle2: TextView
    private lateinit var tvBreathingDuration1: TextView
    private lateinit var tvBreathingDuration2: TextView
    private lateinit var tvBreathingDuration3: TextView
    private lateinit var tvBreathingDuration4: TextView
    private lateinit var tvBreathingFormat1: TextView
    private lateinit var tvBreathingFormat2: TextView
    private lateinit var tvBreathingFormat3: TextView
    private lateinit var tvBreathingFormat4: TextView
    private lateinit var lytBreathing1: ConstraintLayout
    private lateinit var lytBreathing2: ConstraintLayout
    private lateinit var emotionIcon: ImageView
    private lateinit var editEmotionIcon: ImageView
    private lateinit var imgBack: ImageView
    private lateinit var tagFlexbox: FlexboxLayout
    private lateinit var recyclerViewTags: RecyclerView
    private var loadingOverlay: FrameLayout? = null
    private lateinit var rightLifeReportCard: FrameLayout

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentThinkRightLandingBinding
        get() = FragmentThinkRightLandingBinding::inflate
    var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSeatName = arguments?.getString("BottomSeatName").toString()
        //RecordEmotionThink
        //Not

        recyclerViewTags = view.findViewById(R.id.recyclerViewTags)
        recomendationRecyclerView = view.findViewById(R.id.recommendationRecyclerView)
        recyclerViewTags.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        editEmotionIcon = view.findViewById(R.id.editEmotionIcon)
        emotionCardData = view.findViewById(R.id.emotionCard)
        breathingCard = view.findViewById(R.id.lyt_breathing_card)
        emotionCardNoData = view.findViewById(R.id.lyt_feel)
        cardAffirmations = view.findViewById(R.id.card_affirmations)
        tvJournalDate = view.findViewById(R.id.tv_journaling_date)
        lytBreathing1 = view.findViewById(R.id.lyt_breathing_1)
        lytBreathing2 = view.findViewById(R.id.lyt_breathing_2)
        tvBreathingTitle1 = view.findViewById(R.id.tv_breathing_title1)
        tvBreathingTitle2 = view.findViewById(R.id.tv_breathing_title2)
        tvBreathingDuration1 = view.findViewById(R.id.tv_breathing_duration1)
        tvBreathingDuration2 = view.findViewById(R.id.tv_breathing_duration2)
        tvBreathingDuration3 = view.findViewById(R.id.tv_breathing_duration3)
        tvBreathingDuration4 = view.findViewById(R.id.tv_breathing_duration4)
        tvBreathingFormat1 = view.findViewById(R.id.tv_breathing_format1)
        tvBreathingFormat2 = view.findViewById(R.id.tv_breathing_format2)
        tvBreathingFormat3 = view.findViewById(R.id.tv_breathing_format3)
        tvBreathingFormat4 = view.findViewById(R.id.tv_breathing_format4)
        tvJournalDesc = view.findViewById(R.id.tv_journaling_desc)
        tvJournalTime = view.findViewById(R.id.tv_journaling_time)
        affirmationCount = view.findViewById(R.id.tv_affirmation_count)
        lytAffirmation1 = view.findViewById(R.id.lyt_affirmation_1)
        lytAffirmation2 = view.findViewById(R.id.lyt_affirmation_2)
        lytAffirmation3 = view.findViewById(R.id.lyt_affirmation_3)
        journalingCard = view.findViewById(R.id.lyt_journaling_card)
        tvQuote = view.findViewById(R.id.tv_quote_desc)
        imgBack = view.findViewById(R.id.img_back)
        mindfullNoDataCard = view.findViewById(R.id.card_mindfull_no_data)
        mindfullDataCard = view.findViewById(R.id.card_mindfull_data)
        cardAddTools = view.findViewById(R.id.add_tools_think_right)
        moodTrackBtn = view.findViewById(R.id.img_mood_tracking)
        mindfullArrowBtn = view.findViewById(R.id.img_mindfull_arrow)
        noDataMindAudit = view.findViewById(R.id.no_data_mind_audit)
        reassessYourMental = view.findViewById(R.id.lyt_reassess_your_mental)
        dataFilledMindAudit = view.findViewById(R.id.data_filled_mind_audit)
        tvWellnessDays = view.findViewById(R.id.tv_wellness_days)
        tvMindfullMinute = view.findViewById(R.id.tv_mindfull_minute)
        emotionLabel = view.findViewById<TextView>(R.id.emotionLabel)
        emotionTime = view.findViewById<TextView>(R.id.emotionTime)
        emotionDescription = view.findViewById<TextView>(R.id.emotionDescription)
        emotionIcon = view.findViewById<ImageView>(R.id.emotionIcon)
        tagFlexbox = view.findViewById<FlexboxLayout>(R.id.emotionTags)
        toolsRecyclerView = view.findViewById(R.id.rec_journaling_tools)
        tvAuthor = view.findViewById(R.id.tv_quote_author)
        rightLifeReportCard = view.findViewById(R.id.rightLifeReportCard)
        noDataMindFullnessMetric = view.findViewById(R.id.noDataMindFullnessMetric)
        instruction_your_mindfullness_review =
            view.findViewById(R.id.instruction_your_mindfullness_review)
        instruction_your_assessment_review =
            view.findViewById(R.id.clinical_assessments_info)
        viewPager = view.findViewById<ViewPager2>(R.id.assessmentViewPager)
        //    tabLayout = view.findViewById<TabLayout>(R.id.tabDots)
        dotsLayout = view.findViewById(R.id.customDotsContainer)
        //getBreathingData()
        data = SharedPreferenceManager.getInstance(requireContext()).userProfile
        tvWellnessDays.text = if (data.wellnessStreak == 0) {
            "${data.wellnessStreak} day"
        }else if(data.wellnessStreak == 1){
            "${data.wellnessStreak} day"
        }else{
            "${data.wellnessStreak} days"
        }
        //fetchMindfulData()
        // add_tools_think_right = view.findViewById(R.id.add_tools_think_right)
        instruction_your_mindfullness_review.setOnClickListener {
            val dialog = MindfulnessReviewDialog.newInstance()
            dialog.show(parentFragmentManager, "MindfulnessReviewDialog")
            // Use parentFragmentManager for fragments
        }
        instruction_your_assessment_review.setOnClickListener {
            val dialog = AssessmentReviewDialog.newInstance()
            dialog.show(parentFragmentManager, "AssessmentReviewDialog")
            // Use parentFragmentManager for fragments
        }
        toolsRecyclerView.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        // toolsRecyclerView.adapter = toolsAdapter
        toolsRecyclerView.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        toolsRecyclerView.adapter = toolsMoreAdapter
        downloadView = view.findViewById(R.id.view_download_icon)
        mainView = view.findViewById(R.id.lyt_main_page)
        toolsAdapter = ToolsAdapter(requireContext(), toolsGridArray, ::onToolsItem)
        journalingRecyclerView = view.findViewById(R.id.rec_add_tools)
        journalingRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        journalingRecyclerView.adapter = toolsAdapter

        toolRecyclerView = view.findViewById(R.id.tool_list_think_right)
        toolAdapter = ToolAdapter(requireContext(), toolsList, ::onToolItem)
        toolRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        toolRecyclerView.adapter = toolAdapter


        tvQuote.setOnClickListener {
            navigateToFragment(ViewQuoteFragment(), "ViewQuote")
        }
        imgBack.setOnClickListener {
            requireActivity().finish()
        }
        moodTrackBtn.setOnClickListener {
            navigateToFragment(
                MoodTrackerFragment("", 0, "ThinkRightReportFragment"),
                "MoodTracker"
            )
        }
        mindfullArrowBtn.setOnClickListener {
            context?.let { it1 ->
                AnalyticsLogger.logEvent(
                    it1, AnalyticsEvent.TR_Report_PageOpen
                )
            }
            navigateToFragment(MindfulnessAnalysisFragment(), "MindfulnessAnalysis")
        }
        downloadView.setOnClickListener {
            if (isAdded && view != null) {
                requireActivity().runOnUiThread {
                    showLoader(requireView())
                }
            }
            Toast.makeText(context?.let { it }, "start Report downloading...", Toast.LENGTH_SHORT).show()
            saveViewAsPdf(requireContext(), mainView, "Journal")
        }

        cardAddTools.setOnClickListener {
            navigateToFragment(AddToolsFragment(), "AddToolsFragment")
        }

        view.findViewById<LinearLayout>(R.id.play_now).setOnClickListener {
            startActivity(Intent(context?.let { it }, PractiseAffirmationPlaylistActivity::class.java))
        }
        view.findViewById<LinearLayout>(R.id.play_now_mind_audit).setOnClickListener {

            //ActivityUtils.startMindAuditActivity(requireContext(), true)
            if (isAdded && assessmentList.isNotEmpty()) {

                val first = assessmentList.firstOrNull()
                if (isAdded && first != null) {
                    startActivity(Intent(requireContext(), MindAuditResultActivity::class.java).apply {
                        putExtra("Assessment", first.assessment.uppercase(Locale.getDefault()))
                    })
                } else {
                    context?.let { Toast.makeText(it, "No assessment to show", Toast.LENGTH_SHORT).show() }
                }
            } else {
                context?.let { ctx -> ActivityUtils.startMindAuditActivity(ctx, true) }
            }
        }
        reassessYourMental.setOnClickListener {
            //ActivityUtils.startMindAuditActivity(requireContext(), true)
            val first = assessmentList.firstOrNull()
            if (isAdded && first != null) {
                startActivity(Intent(requireContext(), MindAuditResultActivity::class.java).apply {
                    putExtra("Assessment", first.assessment.uppercase(Locale.getDefault()))
                })
            } else {
                context?.let { Toast.makeText(it, "No assessment to show", Toast.LENGTH_SHORT).show() }
            }
        }
        view.findViewById<LinearLayout>(R.id.lyt_journaling).setOnClickListener {
            ActivityUtils.startJournalListActivity(requireContext(), true)
        }
        view.findViewById<LinearLayout>(R.id.lyt_add_new).setOnClickListener {
            ActivityUtils.startJournalNewActivity(requireContext(), true)
        }
        view.findViewById<LinearLayout>(R.id.lyt_breathing).setOnClickListener {
            ActivityUtils.startBreathWorkActivity(requireContext())
        }
        view.findViewById<ConstraintLayout>(R.id.lyt_top_header).setOnClickListener {
            //ActivityUtils.startMindAuditActivity(requireContext(), true)
            if (isAdded && assessmentList.isNotEmpty()) {

                val first = assessmentList.firstOrNull()
                if (isAdded && first != null) {
                    startActivity(Intent(requireContext(), MindAuditResultActivity::class.java).apply {
                        putExtra("Assessment", first.assessment.uppercase(Locale.getDefault()))
                    })
                } else {
                    context?.let { Toast.makeText(it, "No assessment to show", Toast.LENGTH_SHORT).show() }
                }
            } else {
                context?.let { ctx -> ActivityUtils.startMindAuditActivity(ctx, true) }
            }
        }
        view.findViewById<ImageView>(R.id.ivSetting).setOnClickListener {
            ActivityUtils.startTodaysAffirmationActivity(requireContext())
        }

        view.findViewById<ImageView>(R.id.img_happy_icon).setOnClickListener {

        }

        view.findViewById<ImageView>(R.id.img_relaxed).setOnClickListener {

        }

        view.findViewById<ImageView>(R.id.img_unsure).setOnClickListener {

        }

        view.findViewById<ImageView>(R.id.img_stressed).setOnClickListener {

        }

        view.findViewById<ImageView>(R.id.img_sad).setOnClickListener {
            //showImageDialog(R.drawable.sad_icon)
        }

        view.findViewById<LinearLayout>(R.id.llGAD7).setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    MASuggestedAssessmentActivity::class.java
                ).apply {
                    putExtra("SelectedAssessment", "GAD-7")
                })
        }
        view.findViewById<LinearLayout>(R.id.llDASS21).setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    MASuggestedAssessmentActivity::class.java
                ).apply {
                    putExtra("SelectedAssessment", "DASS-21")
                })
        }
        view.findViewById<LinearLayout>(R.id.llOHQ).setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    MASuggestedAssessmentActivity::class.java
                ).apply {
                    putExtra("SelectedAssessment", "OHQ")
                })
        }

        if (noData == true) {
            noDataMindFullnessMetric.visibility = View.VISIBLE
        } else {
            noDataMindFullnessMetric.visibility = View.GONE
        }

        editEmotionIcon.setOnClickListener {
            val bottomSheet = RecordEmotionDialogFragment(
                getEmojiFromString(journalAnswerData.emotion!!),
                journalAnswerData.answer!!,
                journalAnswerData
            )
            bottomSheet.show(parentFragmentManager, "RecordEmotionDialog")
        }

        // Sample data
        val assessment = Phq9Assessment(
            score = 7,
            dateTaken = LocalDate.now(),
            nextScanDate = LocalDate.now().plusDays(29)
        )

        // Bind data to UI elements
        val scoreTextView = view.findViewById<TextView>(R.id.scoreTextView)
        val nextScanTextView = view.findViewById<TextView>(R.id.nextScanTextView)
        val severityRangeLayout = view.findViewById<LinearLayout>(R.id.severityRangeLayout)

        // Set the score
        scoreTextView.text = "YOUR SCORE: ${assessment.score}"

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // navigateToFragment(ThinkRightReportFragment(), "ThinkRightLandingFragment")
                    activity?.finish()
                }
            })

        fetchToolGridData()

        rightLifeReportCard.setOnClickListener {
            var dynamicReportId = ""
            dynamicReportId = SharedPreferenceManager.getInstance(requireActivity()).userId
            if (dynamicReportId.isEmpty()) {
                // Some error handling if the ID is not available
            } else {
                val intent = Intent(requireActivity(), AIReportWebViewActivity::class.java).apply {
                    putExtra(AIReportWebViewActivity.EXTRA_REPORT_ID, dynamicReportId)
                }
                startActivity(intent)
            }
        }
    }

    fun getEmojiFromString(emoji: String): Int {
        if (emoji == "Happy") {
            return R.drawable.happy_icon
        } else if (emoji == "Relaxed") {
            return R.drawable.relaxed_icon
        } else if (emoji == "Unsure") {
            return R.drawable.unsure_icon
        } else if (emoji == "Stressed") {
            return R.drawable.stressed_icon
        } else if (emoji == "Sad") {
            return R.drawable.sad_icon
        }
        return 0
    }

    private fun fetchJournalAnswerData() {
        // progressDialog.show()
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val startDate = getCurrentDate()
        val call = ApiClient.apiService.fetchJournalAnswer(token, startDate)
        call.enqueue(object : Callback<JournalAnswerResponse> {
            override fun onResponse(
                call: Call<JournalAnswerResponse>,
                response: Response<JournalAnswerResponse>
            ) {
                if (response.isSuccessful) {
                    journalResponse = response.body()!!
                    if (journalResponse.data.isNotEmpty()) {
                        journalingCard.visibility = View.VISIBLE
                        setJournalAnswerData(journalResponse.data.getOrNull(journalResponse.data.size - 1))
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    journalingCard.visibility = View.GONE
                    //   progressDialog.dismiss()
                }
            }
            override fun onFailure(call: Call<JournalAnswerResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                journalingCard.visibility = View.GONE
                //    progressDialog.dismiss()
            }
        })
    }

    private fun setJournalAnswerData(journalData: JournalAnswerData?) {
        tvJournalDate.text = journalData?.createdAt?.let { formatDate(it) }
        tvJournalTime.text = journalData?.createdAt?.let { formatTimeNew(it) }
        tvJournalDesc.text = journalData?.answer
        val tagAdapter = journalData?.tags?.let { TagAdapter(it) }
        recyclerViewTags.adapter = tagAdapter
    }

    fun formatDate(isoDate: String): String {
        val inputFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
        val outputFormatter = DateTimeFormatter.ofPattern("EEEE dd MMM, yyyy", Locale.ENGLISH)

        val parsedDate = ZonedDateTime.parse(isoDate, inputFormatter)
        return parsedDate.format(outputFormatter)
    }
    fun formatTimeNew(isoDate: String): String {
        return try {
            val zonedDateTime = ZonedDateTime.parse(isoDate, DateTimeFormatter.ISO_ZONED_DATE_TIME)
            val localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
            localDateTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))
        } catch (e: Exception) {
            "Invalid Time"
        }
    }
    fun formatTime(isoDate: String): String {
        val inputFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
        val outputFormatter = DateTimeFormatter.ofPattern("h a", Locale.ENGLISH)

        val parsedDate = ZonedDateTime.parse(isoDate, inputFormatter)
        return parsedDate.format(outputFormatter).lowercase(Locale.ENGLISH)
    }

    private fun fetchJournalSmileyData() {
        // progressDialog.show()
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val startDate = "2025-04-23"
        val call = ApiClient.apiService.fetchJournalAnswer(token, startDate)
        call.enqueue(object : Callback<JournalAnswerResponse> {
            override fun onResponse(
                call: Call<JournalAnswerResponse>,
                response: Response<JournalAnswerResponse>
            ) {
                if (response.isSuccessful) {
                    journalResponse = response.body()!!
                    if (journalResponse.data.isNotEmpty()) {
                        emotionCardData.visibility = View.VISIBLE
                        emotionCardNoData.visibility = View.GONE
                        journalAnswerData = journalResponse.data.getOrNull(0)!!
                        setJournalData(journalResponse.data.getOrNull(0))
                    } else {
                        emotionCardData.visibility = View.GONE
                        emotionCardNoData.visibility = View.VISIBLE
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    emotionCardData.visibility = View.GONE
                    emotionCardNoData.visibility = View.VISIBLE
                    //   progressDialog.dismiss()
                }
            }

            override fun onFailure(call: Call<JournalAnswerResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                emotionCardData.visibility = View.GONE
                emotionCardNoData.visibility = View.VISIBLE
                //    progressDialog.dismiss()
            }
        })
    }

    private fun setJournalData(list: JournalAnswerData?) {
        emotionLabel.text = list?.emotion
        emotionTime.text = "Emotion recorded at \n" + formatToTimeOnly(list?.createdAt!!)
        emotionDescription.text = list.answer
        if (list.emotion == "Happy") {
            emotionIcon.setImageResource(R.drawable.happy_icon)
        } else if (list.emotion == "Relaxed") {
            emotionIcon.setImageResource(R.drawable.relaxed_icon)
        } else if (list.emotion == "Unsure") {
            emotionIcon.setImageResource(R.drawable.unsure_icon)
        } else if (list.emotion == "Stressed") {
            emotionIcon.setImageResource(R.drawable.stressed_icon)
        } else if (list.emotion == "Sad") {
            emotionIcon.setImageResource(R.drawable.sad_icon)
        }
        // Replace with your actual emoji image

        tagFlexbox.removeAllViews()

        for (tag in list.tags) {
            val chip = TextView(requireContext()).apply {
                text = tag
                setPadding(24, 12, 24, 12)
                background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.tag_background)
                setTextColor(Color.parseColor("#B85C00"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            }
            tagFlexbox.addView(chip)
        }
    }

    fun formatToTimeOnly(isoString: String): String {
        val zonedDateTime = ZonedDateTime.parse(isoString)
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        return zonedDateTime.format(formatter).lowercase()
    }

    private fun fetchMindfulData() {
        if (!isAdded || view == null) return

        requireActivity().runOnUiThread {
            showLoader(requireView())
        }

        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val startDate = getYesterdayDate()
        val endDate = getCurrentDate()

        val call = ApiClient.apiService.fetchMindFull(token, startDate, endDate)

        call.enqueue(object : Callback<MindfullResponse> {
            override fun onResponse(call: Call<MindfullResponse>, response: Response<MindfullResponse>) {
                if (!isAdded || view == null) {
                    // Fragment already destroyed → don't touch UI
                    return
                }

                requireActivity().runOnUiThread {
                    dismissLoader(requireView())

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            mindfullResponse = body

                            mindfullNoDataCard.visibility = View.GONE
                            mindfullDataCard.visibility = View.VISIBLE

                            // ── This is the corrected & safe update ──
                            body.data?.formattedData?.lastOrNull()?.duration?.let { duration ->
                                tvMindfullMinute.text = formatDuration(duration)
                            } ?: run {
                                tvMindfullMinute.text = "00 hr 00 min"   // ← fallback when no data
                            }
                        } else {
                            showNoDataState()
                        }
                    } else {
                        showNoDataState()
                        Log.e("Mindful", "API error: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                }
            }

            override fun onFailure(call: Call<MindfullResponse>, t: Throwable) {
                if (!isAdded || view == null) return

                requireActivity().runOnUiThread {
                    dismissLoader(requireView())
                    showNoDataState()
                    Log.e("Mindful", "API failed", t)
                }
            }

            private fun showNoDataState() {
                mindfullNoDataCard.visibility = View.VISIBLE
                mindfullDataCard.visibility = View.GONE
                tvMindfullMinute.text = "0 min"   // or "--" or whatever you prefer
            }
        })
    }

    fun formatDuration(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        val hrText = if (hours <= 1) "hr" else "hrs"
        val minText = if (minutes <= 1) "min" else "mins"

        return when {
            hours > 0 && minutes > 0 -> "$hours $hrText $minutes $minText"
            hours > 0 && minutes == 0 -> "$hours $hrText 00 $minText"
            minutes == 0 -> "00 $hrText 00 $minText"  // ✅ Yahan bhi 00 min
            else -> "00 $hrText $minutes $minText"
        }
    }

    fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return LocalDate.now().format(formatter)
    }

    fun getYesterdayDate(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return LocalDate.now().minusDays(1).format(formatter)
    }

    private fun fetchToolGridData() {
        if (isAdded && view != null) {
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        //  val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjdhNWZhZTkxOTc5OTI1MTFlNzFiMWM4Iiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiJBZGl0eWEiLCJsYXN0TmFtZSI6IlR5YWdpIiwiZGV2aWNlSWQiOiJCNkRCMTJBMy04Qjc3LTRDQzEtOEU1NC0yMTVGQ0U0RDY5QjQiLCJtYXhEZXZpY2VSZWFjaGVkIjpmYWxzZSwidHlwZSI6ImFjY2Vzcy10b2tlbiJ9LCJpYXQiOjE3MzkxNzE2NjgsImV4cCI6MTc1NDg5NjQ2OH0.koJ5V-vpGSY1Irg3sUurARHBa3fArZ5Ak66SkQzkrxM"
        val call = ApiClient.apiService.thinkTools(token)
        call.enqueue(object : Callback<ToolsGridResponse> {
            override fun onResponse(
                call: Call<ToolsGridResponse>,
                response: Response<ToolsGridResponse>
            ) {
                if (response.isSuccessful) {
                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    toolGridResponse = response.body()!!
                    for (i in 0 until toolGridResponse.data.size) {
                        toolGridResponse.data.getOrNull(i)?.let {
                            toolsGridArray.add(it)
                        }
                    }
                    toolsAdapter.notifyDataSetChanged()
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    //                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }
            override fun onFailure(call: Call<ToolsGridResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                //          Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                if (isAdded && view != null) {
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }

    private fun getBreathingData() {
        if (isAdded && view != null) {
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val call = ApiClient.apiService.getBreathing(token)
        call.enqueue(object : Callback<BreathingResponse> {
            override fun onResponse(
                call: Call<BreathingResponse>,
                response: Response<BreathingResponse>
            ) {
                if (response.isSuccessful) {
                    breathingResponse = response.body()!!
                    if (breathingResponse.data.isNotEmpty()) {
                        breathingCard.visibility = View.VISIBLE
                        if (breathingResponse.data.size == 1) {
                            lytBreathing1.visibility = View.VISIBLE
                            lytBreathing2.visibility = View.GONE
                            var duration1 = convertSecondsToHourMinPretty(breathingResponse.data.getOrNull(0)?.duration ?: 0)
                            var format1 = duration1.split(" ")
                            tvBreathingDuration1.setText(duration1)
                            tvBreathingTitle1.setText(breathingResponse.data.getOrNull(0)?.title.toString())
                        }else if (breathingResponse.data.size == 2){
                            lytBreathing1.visibility = View.VISIBLE
                            lytBreathing2.visibility = View.VISIBLE
                            var duration1 = convertSecondsToHourMinPretty(breathingResponse.data.getOrNull(0)?.duration ?: 0)
                            var format1 = duration1.split(" ")
                            var duration2 = convertSecondsToHourMinPretty(breathingResponse.data.getOrNull(1)?.duration ?: 0)
                            var format2 = duration2.split(" ")
                            tvBreathingDuration1.setText(duration1)
                            tvBreathingDuration3.setText(duration2)
                            tvBreathingTitle1.setText(breathingResponse.data.getOrNull(0)?.title.toString())
                            tvBreathingTitle2.setText(breathingResponse.data.getOrNull(1)?.title.toString())
                        }

                        lytBreathing1.setOnClickListener {
                            breathingResponse.data.getOrNull(0)
                                ?.let { it1 -> navigateToBreathworkSession(it1) }
                        }
                        lytBreathing2.setOnClickListener {
                            breathingResponse.data.getOrNull(1)
                                ?.let { it1 -> navigateToBreathworkSession(it1) }
                        }
                    } else {
                        breathingCard.visibility = View.GONE
                    }
                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                } else {
                    breathingCard.visibility = View.GONE
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }
            override fun onFailure(call: Call<BreathingResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                breathingCard.visibility = View.GONE
                if (isAdded && view != null) {
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }

    fun convertSecondsToHourMinPretty(seconds: Int): String {
        return when {
            seconds < 60 -> {
                "${seconds}sec"
            }
            seconds < 3600 -> {
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                "${minutes}min ${remainingSeconds}sec"
            }
            else -> {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                "${hours}hr ${minutes}min"
            }
        }
    }

    private fun navigateToBreathworkSession(breathingData: BreathingData?) {
        val intent = Intent(requireContext(), BreathworkSessionActivity::class.java).apply {
            var startDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            putExtra("BREATHWORK", breathingData)
            putExtra("StartDate", startDate)
            // Add StartDate if available: putExtra("StartDate", startDate)
        }
        startActivity(intent)
    }

    private fun fetchToolList() {
        Log.d("ToolList", "fetchToolList called")
        // progressDialog.show()

        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        Log.d("ToolList", "Token retrieved: ${token?.take(20)}...")
        Log.d("ToolList", "UserId retrieved: $userId")

        //  val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjdlM2ZiMjdiMzNlZGZkNzRlMDY5OWFjIiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiIiLCJsYXN0TmFtZSI6IiIsImRldmljZUlkIjoiVEUxQS4yNDAyMTMuMDA5IiwibWF4RGV2aWNlUmVhY2hlZCI6ZmFsc2UsInR5cGUiOiJhY2Nlc3MtdG9rZW4ifSwiaWF0IjoxNzQzMDU2OTEwLCJleHAiOjE3NTg3ODE3MTB9.gYLi895fpb4HGitALoGDRwHw3MIDCjYXTyqAKDNjS0A"

        val call = ApiClient.apiService.getToolList(token, userId)
        Log.d("ToolList", "API call initiated for userId: $userId")

        call.enqueue(object : Callback<ModuleResponse> {
            override fun onResponse(
                call: Call<ModuleResponse>,
                response: Response<ModuleResponse>
            ) {
                Log.d("ToolList", "onResponse received - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")

                if (response.isSuccessful) {
                    // progressDialog.dismiss()
                    val toolResponse = response.body()
                    Log.d("ToolList", "Response body received, success: ${toolResponse?.success}")

                    toolResponse?.let {
                        if (it.success == true) {
                            val tools = it.data
                            Log.d("ToolList", "Tools data received, count: ${tools.size}")

                            toolsList.clear()
                            Log.d("ToolList", "Cleared existing toolsList")

                            toolsList.addAll(tools)
                            Log.d("ToolList", "Added tools to toolsList, total count: ${toolsList.size}")

                            toolAdapter.notifyDataSetChanged()
                            Log.d("ToolList", "Adapter notified of data change")
                            Log.d("ToolList", "Tool list fetched successfully")
                        } else {
                            Log.w("ToolList", "Request failed with statusCode: ${it.statusCode}")
                            Toast.makeText(
                                activity,
                                "Request failed with status: ${it.statusCode}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    if (toolResponse == null) {
                        Log.w("ToolList", "Response body is null")
                    }
                } else {
                    Log.e("ToolList", "Response not successful: ${response.code()}")
                    Log.e("ToolList", "Error body: ${response.errorBody()?.string()}")
                    /* Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                     Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                     progressDialog.dismiss()*/
                    //             Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ModuleResponse>, t: Throwable) {
                Log.e("ToolList", "API call failed: ${t.message}")
                Log.e("ToolList", "Exception: ${t.javaClass.simpleName}")
                t.printStackTrace()
                /* Log.e("Error", "API call failed: ${t.message}")
                 Toast.makeText(activity, "Failure: ${t.message}", Toast.LENGTH_SHORT).show()
                 progressDialog.dismiss()*/
                //          Toast.makeText(activity, "Failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSeverityRangeBar(layout: LinearLayout, assessment: Phq9Assessment) {
        SeverityLevel.values().forEach { level ->
            val rangeView = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    level.range.count().toFloat() // Weight based on range size
                )
                setBackgroundColor(resources.getColor(level.colorResId))
            }

            // Add a label for the current severity level
            if (assessment.score in level.range) {
                val label = TextView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER
                    }
                    text = level.name
                    textSize = 12f
                    setTextColor(Color.BLACK)
                    setTypeface(null, Typeface.BOLD)
                }
                val container = LinearLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        level.range.count().toFloat()
                    )
                    gravity = Gravity.CENTER
                    setBackgroundColor(resources.getColor(level.colorResId))
                    addView(label)
                }
                layout.addView(container)
            } else {
                layout.addView(rangeView)
            }
        }
    }

    private fun showImageDialog(imageRes: Int) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image_zoom)

        val imageView: ImageView = dialog.findViewById(R.id.dialogImageView)
        imageView.setImageResource(imageRes)

        dialog.show()
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    private fun onToolItem(toolsData: ModuleData, position: Int, isRefresh: Boolean) {
        if (toolsData.title != null) {
            if (toolsData.title?.contains("Breathing") == true ||toolsData.title?.contains("Custom") == true) {
                val breathingData = BreathingData().apply {
                    id = toolsData._id ?: ""
                    title = toolsData.title
                    subTitle = toolsData.desc
                    thumbnail = toolsData.image
                    breathInhaleTime = toolsData.breathInhaleTime
                    breathExhaleTime = toolsData.breathExhaleTime
                    breathHoldTime = toolsData.breathHoldTime
                    createdAt = ""
                    updatedAt = ""
                    duration = 0
                    totalTime = toolsData.totalTime
                }
                navigateToBreathworkSession(breathingData)
//                startActivity(
//                    Intent(requireContext(), BreathworkActivity::class.java).apply {
//                        putExtra("IS_FROM_TOOLS", true)
//                        putExtra("TOOLS_VALUE", toolsData._id)
//                    }
//                )
            } else if (toolsData.title.equals("Free Form") || toolsData.title.equals("Bullet") || toolsData.title.equals(
                    "Gratitude"
                ) || toolsData.title.equals("Grief")
            ) {
                startActivity(Intent(requireContext(), JournalNewActivity::class.java).apply {
                    putExtra("IS_FROM_TOOLS", true)
                    putExtra("TOOLS_VALUE", toolsData._id)
                    putExtra("FROM_THINK_RIGHT", true)
                }
                )
            } else
                startActivity(
                    Intent(
                        requireContext(),
                        PractiseAffirmationPlaylistActivity::class.java
                    )
                )
        }
    }

    private fun fetchQuoteData() {
        // progressDialog.show()
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        //     val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjdhNWZhZTkxOTc5OTI1MTFlNzFiMWM4Iiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiJBZGl0eWEiLCJsYXN0TmFtZSI6IlR5YWdpIiwiZGV2aWNlSWQiOiJCNkRCMTJBMy04Qjc3LTRDQzEtOEU1NC0yMTVGQ0U0RDY5QjQiLCJtYXhEZXZpY2VSZWFjaGVkIjpmYWxzZSwidHlwZSI6ImFjY2Vzcy10b2tlbiJ9LCJpYXQiOjE3MzkxNzE2NjgsImV4cCI6MTc1NDg5NjQ2OH0.koJ5V-vpGSY1Irg3sUurARHBa3fArZ5Ak66SkQzkrxM"
        val call = ApiClient.apiService.quoteOfDay(token)
        call.enqueue(object : Callback<ThinkQuoteResponse> {
            override fun onResponse(
                call: Call<ThinkQuoteResponse>,
                response: Response<ThinkQuoteResponse>
            ) {
                if (response.isSuccessful) {
                    // progressDialog.dismiss()
                    thinkQuoteResponse = response.body()!!
                    tvQuote.text = thinkQuoteResponse.data.getOrNull(0)?.quote
                    tvAuthor.text = "-" + thinkQuoteResponse.data.getOrNull(0)?.authorName
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    //          Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    // progressDialog.dismiss()
                }
            }
            override fun onFailure(call: Call<ThinkQuoteResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                //          Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                //progressDialog.dismiss()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (!SharedPreferenceManager.getInstance(requireContext()).aiReportGeneratedView) {
            if (SharedPreferenceManager.getInstance(requireContext()).userProfile?.isReportGenerated == true) {
                rightLifeReportCard.visibility = View.VISIBLE
            } else {
                rightLifeReportCard.visibility = View.GONE
            }
        } else {
            rightLifeReportCard.visibility = View.GONE
        }
        fetchToolList()
        fetchQuoteData()
        fetchAssessmentResult()
        fetchAffirmationsList()
        fetchMindfulData()
        getBreathingData()
        fetchJournalAnswerData()
        fetchThinkRecomendedData()
    }

    private fun fetchThinkRecomendedData() {
        val act = activity ?: return   // ✅ safe
        val ctx = context ?: return    // ✅ safe
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val call = ApiClient.apiService.fetchThinkRecomended(token, "HOME", "THINK_RIGHT")
        call.enqueue(object : Callback<ThinkRecomendedResponse> {
            override fun onResponse(
                call: Call<ThinkRecomendedResponse>,
                response: Response<ThinkRecomendedResponse>
            ) {
                val ctx = context ?: return  // ✅ recheck inside callback
                val rootView = view ?: return
                if (!isAdded) return         // ✅ fragment detached
                if (response.isSuccessful) {
                    // progressDialog.dismiss()
                    val body = response.body() ?: return
                    thinkRecomendedResponse = body
                    if (body.data?.contentList?.isNotEmpty() == true) {
                        recomendationAdapter = RecommendationAdapter(
                            ctx,
                            thinkRecomendedResponse.data?.contentList!!
                        )
                        recomendationRecyclerView.layoutManager =
                            LinearLayoutManager(ctx)
                        recomendationRecyclerView.adapter = recomendationAdapter
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    //          Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    // progressDialog.dismiss()
                }
            }
            override fun onFailure(call: Call<ThinkRecomendedResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                //          Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                //progressDialog.dismiss()
            }
        })
    }

    private fun fetchAffirmationsList() {
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val call = ApiClient.apiService.getAffirmationPlaylist(token)
        call.enqueue(object : Callback<AffirmationPlaylistResponse> {
            override fun onResponse(
                call: Call<AffirmationPlaylistResponse>,
                response: Response<AffirmationPlaylistResponse>
            ) {
                if (response.isSuccessful) {
                    val assessmentResponse = response.body()
                    if (assessmentResponse != null) {
                        if (assessmentResponse.data.isNotEmpty()) {
                            cardAffirmations.visibility = View.VISIBLE
                            val count = assessmentResponse.data.size
                            affirmationCount.text = count.toString() + " Affirmations Saved"
                            if (count > 2) {
                                lytAffirmation1.visibility = View.VISIBLE
                                lytAffirmation2.visibility = View.VISIBLE
                                lytAffirmation3.visibility = View.VISIBLE
                            }else{
                                cardAffirmations.visibility = View.GONE
                            }/*else if (count == 2) {
                                lytAffirmation1.visibility = View.VISIBLE
                                lytAffirmation2.visibility = View.VISIBLE
                                lytAffirmation3.visibility = View.GONE
                            } else if (count == 1) {
                                lytAffirmation1.visibility = View.VISIBLE
                                lytAffirmation2.visibility = View.GONE
                                lytAffirmation3.visibility = View.GONE
                            }*/
                        } else {
                            cardAffirmations.visibility = View.GONE
                        }
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    cardAffirmations.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<AffirmationPlaylistResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                cardAffirmations.visibility = View.GONE
            }
        })
    }

    private fun fetchAssessmentResult() {
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val call = ApiClient.apiService.getAssessmentResult(token)
        call.enqueue(object : Callback<AssessmentResponse> {
            override fun onResponse(
                call: Call<AssessmentResponse>,
                response: Response<AssessmentResponse>
            ) {
                if (response.isSuccessful) {
                    val assessmentResponse = response.body()
                    if (assessmentResponse != null) {
                        if (assessmentResponse.result.isNotEmpty()) {
                            dataFilledMindAudit.visibility = View.VISIBLE
                            noDataMindAudit.visibility = View.GONE
                            reassessYourMental.visibility = View.VISIBLE

                            assessmentList = parseAssessmentData(assessmentResponse.result)
                            adapter = AssessmentPagerAdapter(assessmentList)
                            viewPager.adapter = adapter
                            viewPager.post {
                                updateViewPagerHeight(viewPager)
                            }
                            val transformer = CompositePageTransformer().apply {
                                addTransformer(MarginPageTransformer(16))
                                addTransformer { page, position ->
                                    val scale = 0.95f + (1 - kotlin.math.abs(position)) * 0.05f
                                    page.scaleY = scale
                                }
                            }
                            viewPager.setPageTransformer(transformer)
                            viewPager.offscreenPageLimit = 3
                            dotsLayout.removeAllViews()
                            itemCount = adapter.itemCount

                            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                                override fun onPageSelected(position: Int) {
                                    viewPager.post {
                                        updateViewPagerHeight(viewPager)
                                    }
                                    for (i in 0 until itemCount) {
                                        val dot = dotsLayout.getChildAt(i) ?: continue
                                        val isActive = i == position
                                        val layoutParams = dot.layoutParams
                                        layoutParams.width = if (isActive) 32.dpToPx() else 14.dpToPx()
                                        dot.layoutParams = layoutParams
                                        dot.background = ContextCompat.getDrawable(
                                            requireContext(),
                                            if (isActive) R.drawable.active_dots else R.drawable.inactive_dots
                                        )
                                    }
                                }
                            })

                            dotSize = 14
                            dotMargin = 6

                            // Dots banate waqt pehle clear kiya → ab duplicate nahi honge
                            for (i in 0 until itemCount) {
                                val dot = View(requireContext()).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        if (i == 0) 32.dpToPx() else dotSize.dpToPx(),
                                        dotSize.dpToPx()
                                    ).apply {
                                        setMargins(dotMargin.dpToPx(), 0, dotMargin.dpToPx(), 0)
                                    }
                                    background = ContextCompat.getDrawable(
                                        requireContext(),
                                        if (i == 0) R.drawable.active_dots else R.drawable.inactive_dots
                                    )
                                }
                                dotsLayout.addView(dot)
                            }

                        } else {
                            dataFilledMindAudit.visibility = View.GONE
                            noDataMindAudit.visibility = View.VISIBLE
                            reassessYourMental.visibility = View.GONE
                            dotsLayout.removeAllViews()  // No data → dots bhi hata do
                        }
                    }
                } else {
                    dataFilledMindAudit.visibility = View.GONE
                    noDataMindAudit.visibility = View.VISIBLE
                    reassessYourMental.visibility = View.GONE
                    dotsLayout.removeAllViews()
                }
            }

            override fun onFailure(call: Call<AssessmentResponse>, t: Throwable) {
                dataFilledMindAudit.visibility = View.GONE
                noDataMindAudit.visibility = View.VISIBLE
                reassessYourMental.visibility = View.GONE
                dotsLayout.removeAllViews()
            }
        })
    }

    fun Int.dpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun updateViewPagerHeight(viewPager: ViewPager2) {
        val recyclerView = viewPager.getChildAt(0) as RecyclerView
        val view = recyclerView.layoutManager?.findViewByPosition(viewPager.currentItem)

        view?.post {
            val wMeasureSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(wMeasureSpec, hMeasureSpec)

            val height = view.measuredHeight
            viewPager.layoutParams.height = height
            viewPager.requestLayout()
        }
    }

    private fun parseAssessmentData(listData: List<AssessmentResult>): MutableList<AssessmentResultData> {
        val resultList = mutableListOf<AssessmentResultData>()

        for (assessmentResult in listData) {
            for (taken in assessmentResult.assessmentsTaken) {
                when (taken.assessment) {
                    "DASS-21" -> {
                        // DASS-21 ke liye ek hi entry banani hai, teeno scores ke saath
                        val depression = taken.interpretations["depression"]
                        val anxiety = taken.interpretations["anxiety"]
                        val stress = taken.interpretations["stress"]

                        // Safe check (agar koi missing ho)
                        if (depression != null && anxiety != null && stress != null) {
                            val combinedInterpretation = "Depression"
                            val combinedLevel = "${depression.level} | ${anxiety.level} | ${stress.level}"
                            val combinedScore = "${depression.score} | ${anxiety.score} | ${stress.score}"

                            resultList.add(
                                AssessmentResultData(
                                    assessment = "DASS-21",
                                    interpretation = combinedInterpretation, // ya "Depression, Anxiety, Stress"
                                    score = combinedScore,        // "26.0 | 24.0 | 26.0"
                                    level = combinedLevel         // "Severe | Extremely Severe | Severe"
                                )
                            )
                        }
                    }
                    else -> {
                        // Baaki assessments (PHQ-9, GAD-7 etc.) ke liye normally add karo
                        for ((interpretationName, interpretation) in taken.interpretations) {
                            resultList.add(
                                AssessmentResultData(
                                    assessment = taken.assessment,
                                    interpretation = interpretationName.capitalize(),
                                    score = interpretation.score.toString(),
                                    level = interpretation.level
                                )
                            )
                        }
                    }
                }
            }
        }
        return resultList
    }


    private fun onToolsItem(toolsData: ToolGridData, position: Int, isRefresh: Boolean) {

        if (toolsData.moduleName.contentEquals("Breathwork")) {
            ActivityUtils.startBreathWorkActivity(requireContext())
        } else if (toolsData.moduleName.contentEquals("Journalling")) {
            startActivity(Intent(requireContext(), JournalNewActivity::class.java).apply {
                putExtra("FROM_THINK_RIGHT", true)
            })
        } else if (toolsData.moduleName.contentEquals("Affirmation")) {
            ActivityUtils.startTodaysAffirmationActivity(requireContext())
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun saveViewAsPdf(context: Context, view: View, fileName: String) {
        val bitmap = getBitmapFromView(view)
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        document.finishPage(page)

        var fileUri: Uri? = null
        var success = false
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    fileUri = uri
                    outputStream = resolver.openOutputStream(uri)
                }
            } else {
                val directory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(directory, "$fileName.pdf")
                fileUri = Uri.fromFile(file)
                outputStream = file.outputStream()
            }

            outputStream?.use {
                document.writeTo(it)
                success = true
                Toast.makeText(context?.let { it }, "Report downloaded successfully", Toast.LENGTH_SHORT).show()
            }

        } catch (e: IOException) {
            if (isAdded && view != null) {
                requireActivity().runOnUiThread {
                    dismissLoader(requireView())
                }
            }
            e.printStackTrace()
        } finally {
            document.close()
            outputStream?.close()
            if (isAdded && view != null) {
                requireActivity().runOnUiThread {
                    dismissLoader(requireView())
                }
            }
        }

        if (success && fileUri != null) {
            showDownloadNotification(context, fileName, fileUri)
            if (isAdded && view != null) {
                requireActivity().runOnUiThread {
                    dismissLoader(requireView())
                }
            }
        }
    }

    fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showDownloadNotification(context: Context, fileName: String, fileUri: Uri) {
        val channelId = "download_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download Notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open the PDF file
        val openPdfIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openPdfIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Download Complete")
            .setContentText("$fileName.pdf saved to Downloads")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent) // Open PDF on click
            .setAutoCancel(true) // Dismiss when tapped
            .build()

        NotificationManagerCompat.from(context).notify(1, notification)
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }

    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }
}

class AssessmentPagerAdapter(
    private val assessments: List<AssessmentResultData>
) : RecyclerView.Adapter<AssessmentPagerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.tvAssessmentTitle)
        val scoreText = itemView.findViewById<TextView>(R.id.score_result_text)
        val scoreTextDepression = itemView.findViewById<TextView>(R.id.score_result_text_new)
        val scoreTextStress = itemView.findViewById<TextView>(R.id.score_result_text_new_one)
        val mind_audit_scale = itemView.findViewById<TextView>(R.id.mind_audit_scale_category)
        val mind_audit_scale_dipression_level = itemView.findViewById<TextView>(R.id.mind_audit_scale_category_new)
        val mind_audit_scale_stress_level = itemView.findViewById<TextView>(R.id.mind_audit_scale_category_new_one)
        val mind_audit_scale_new = itemView.findViewById<TextView>(R.id.mind_audit_scale)
        val mind_audit_scale_dipression = itemView.findViewById<TextView>(R.id.mind_audit_scale_new)
        val mind_audit_scale_stress = itemView.findViewById<TextView>(R.id.mind_audit_scale_new_one)
        val new_score_layout = itemView.findViewById<CardView>(R.id.new_score_layout)
        val new_score_layout_new = itemView.findViewById<CardView>(R.id.new_score_layout_new)
        val new_score_layout_new_one = itemView.findViewById<CardView>(R.id.new_score_layout_new_one)
        val scaleLayout = itemView.findViewById<LinearLayout>(R.id.scoreScaleLayout)
        val pointer = itemView.findViewById<FrameLayout>(R.id.lyt_score)
        val scoreScaleImage = itemView.findViewById<ImageView>(R.id.scoreScaleImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.assessment_result_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = assessments.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = assessments[position]
        holder.title.text = item.assessment


        when (item?.assessment) {
            "DASS-21" -> {
                holder.scoreScaleImage.setImageResource(R.drawable.ic_mind_dass)
                val scores = item.score.split("|").map { it.trim() }
                val levels = item.level.split("|").map { it.trim() }

                // Individual scores
                val depressionScore = scores.getOrNull(0) ?: "0"
                val anxietyScore = scores.getOrNull(1) ?: "0"
                val stressScore = scores.getOrNull(2) ?: "0"

                val score = anxietyScore.toFloatOrNull() ?: 0f

                val backgroundColor = when {
                    score <= 4 -> "#06B27B"
                    score <= 9 -> "#54C8DB"
                    score <= 12 -> "#06B27B"
                    score <= 14 -> "#57A3FC"
                    score <= 19 -> "#FFBD44"
                    else -> "#FC6656"
                }

                holder.new_score_layout.setCardBackgroundColor(Color.parseColor(backgroundColor))
                val dipressionscore = anxietyScore.toFloatOrNull() ?: 0f

                val dipressionbackgroundColor = when {
                    dipressionscore <= 4 -> "#06B27B"
                    dipressionscore <= 9 -> "#54C8DB"
                    dipressionscore <= 12 -> "#06B27B"
                    dipressionscore <= 14 -> "#57A3FC"
                    dipressionscore <= 19 -> "#FFBD44"
                    else -> "#FC6656"
                }

                holder.new_score_layout_new.setCardBackgroundColor(Color.parseColor(dipressionbackgroundColor))
                val stressscore = anxietyScore.toFloatOrNull() ?: 0f

                val stressbackgroundColor = when {
                    stressscore <= 4 -> "#06B27B"
                    stressscore <= 9 -> "#54C8DB"
                    stressscore <= 12 -> "#06B27B"
                    stressscore <= 14 -> "#57A3FC"
                    stressscore <= 19 -> "#FFBD44"
                    else -> "#FC6656"
                }

                holder.new_score_layout_new_one.setCardBackgroundColor(Color.parseColor(stressbackgroundColor))
                holder.new_score_layout.visibility =View.VISIBLE
                holder.new_score_layout_new.visibility = View.VISIBLE
                holder.new_score_layout_new_one.visibility =View.VISIBLE
                val anxietyResult = anxietyScore.substringBefore(".")
                holder.scoreText.text = anxietyResult
                val depressionResult = depressionScore.substringBefore(".")
                holder.scoreTextDepression.text = depressionResult
                val stressResult = stressScore.substringBefore(".")
                holder.scoreTextStress.text = stressResult
                holder.mind_audit_scale_new.text = "Anxiety"
                holder.mind_audit_scale_dipression.text = "Depression"
                holder.mind_audit_scale_stress.text = "Stress"
                // Individual levels
                val depressionLevel = levels.getOrNull(0) ?: ""
                val anxietyLevel = levels.getOrNull(1) ?: ""
                val stressLevel = levels.getOrNull(2) ?: ""
                holder.mind_audit_scale.text = anxietyLevel
                holder.mind_audit_scale_dipression_level.text = depressionLevel
                holder.mind_audit_scale_stress_level.text = stressLevel


            }
            "GAD-7" -> {
                val score = item.score.toFloatOrNull() ?: 0f

                val backgroundColor = when {
                    score <= 4 -> "#06B27B"
                    score <= 9 -> "#54C8DB"
                    score <= 12 -> "#06B27B"
                    score <= 14 -> "#57A3FC"
                    score <= 19 -> "#FFBD44"
                    else -> "#FC6656"
                }

                holder.new_score_layout.setCardBackgroundColor(Color.parseColor(backgroundColor))
                holder.new_score_layout.visibility =View.VISIBLE
                holder.new_score_layout_new.visibility = View.GONE
                holder.new_score_layout_new_one.visibility =View.GONE
                val anxietyResult = item.score.substringBefore(".")
                holder.scoreText.text = anxietyResult
                holder.mind_audit_scale.text = item.level
                holder.mind_audit_scale_new.text = item.interpretation
                holder.scoreScaleImage.setImageResource(R.drawable.ic_mind_gad7)
            }
            "OHQ" -> {
                val score = item.score.toFloatOrNull() ?: 0f

                val backgroundColor = when {
                    score <= 4 -> "#06B27B"
                    score <= 9 -> "#54C8DB"
                    score <= 12 -> "#06B27B"
                    score <= 14 -> "#57A3FC"
                    score <= 19 -> "#FFBD44"
                    else -> "#FC6656"
                }

                holder.new_score_layout.setCardBackgroundColor(Color.parseColor(backgroundColor))
                holder.new_score_layout.visibility =View.VISIBLE
                holder.new_score_layout_new.visibility = View.GONE
                holder.new_score_layout_new_one.visibility =View.GONE
                val anxietyResult = item.score.substringBefore(".")
                holder.scoreText.text = anxietyResult
                holder.mind_audit_scale.text = item.level
                holder.mind_audit_scale_new.text = item.interpretation
                holder.scoreScaleImage.setImageResource(R.drawable.ic_mind_ohq)
            }
            "CAS" -> {
                val score = item.score.toFloatOrNull() ?: 0f

                val backgroundColor = when {
                    score <= 4 -> "#06B27B"
                    score <= 9 -> "#54C8DB"
                    score <= 12 -> "#06B27B"
                    score <= 14 -> "#57A3FC"
                    score <= 19 -> "#FFBD44"
                    else -> "#FC6656"
                }

                holder.new_score_layout.setCardBackgroundColor(Color.parseColor(backgroundColor))
                holder.new_score_layout.visibility =View.VISIBLE
                holder.new_score_layout_new.visibility = View.GONE
                holder.new_score_layout_new_one.visibility =View.GONE
                val anxietyResult = item.score.substringBefore(".")
                holder.scoreText.text = anxietyResult
                holder.mind_audit_scale.text = item.level
                holder.mind_audit_scale_new.text = item.interpretation
                holder.scoreScaleImage.setImageResource(R.drawable.ic_mind_cas)
            }
            "PHQ-9" -> {
                val score = item.score.toFloatOrNull() ?: 0f

                val backgroundColor = when {
                    score <= 4 -> "#06B27B"
                    score <= 9 -> "#54C8DB"
                    score <= 12 -> "#06B27B"
                    score <= 14 -> "#57A3FC"
                    score <= 19 -> "#FFBD44"
                    else -> "#FC6656"
                }

                holder.new_score_layout.setCardBackgroundColor(Color.parseColor(backgroundColor))
                holder.new_score_layout.visibility =View.VISIBLE
                holder.new_score_layout_new.visibility = View.GONE
                holder.new_score_layout_new_one.visibility =View.GONE
                val anxietyResult = item.score.substringBefore(".")
                holder.scoreText.text = anxietyResult
                holder.mind_audit_scale.text = item.level
                holder.mind_audit_scale_new.text = item.interpretation
                holder.scoreScaleImage.setImageResource(R.drawable.ic_mind_dass)
            }
        }
        // Clear old views if recycled
      //  holder.scaleLayout.removeAllViews()
        val context = holder.itemView.context

      //  holder.scaleLayout.removeAllViews()
//        for (i in labels.indices) {
//            val column = LinearLayout(context).apply {
//                orientation = LinearLayout.VERTICAL
//                layoutParams =
//                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
//                gravity = Gravity.CENTER
//            }
//            // Number label
//            val numberText = TextView(context).apply {
//                text = thresholds[i].toString()
//                setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f)
//                setTextColor(Color.DKGRAY)
//            }
//            // Category label
//            val radius = 24f
//            val bgDrawable = GradientDrawable().apply {
//                shape = GradientDrawable.RECTANGLE
//                cornerRadii = when (i) {
//                    0 -> floatArrayOf(radius, radius, 0f, 0f, 0f, 0f, radius, radius) // left round
//                    labels.lastIndex -> floatArrayOf(
//                        0f,
//                        0f,
//                        radius,
//                        radius,
//                        radius,
//                        radius,
//                        0f,
//                        0f
//                    ) // right round
//                    else -> FloatArray(8) { 0f }
//                }
//                setColor(colors[i])
//                if (score >= thresholds[i] && score < thresholds[i + 1]) {
//                    setStroke(4, Color.BLACK) // 4dp black border
//                }
//            }
//            val labelText = TextView(context).apply {
//                text = labels[i]
//                setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f)
//                setTextColor(Color.WHITE)
//                setTypeface(null, Typeface.BOLD)
//                gravity = Gravity.CENTER
//                setPadding(0, 8, 0, 8)
//                background = bgDrawable
//                layoutParams = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//                ).apply {
//                    topMargin = 4
//                }
//            }
//            column.addView(numberText)
//            column.addView(labelText)
//            holder.scaleLayout.addView(column)
//        }
//        holder.scaleLayout.post {
//            val totalWidth = holder.scaleLayout.width
//            val segmentCount = labels.size
//            val segmentWidth = totalWidth / segmentCount
//            // Find the correct index based on score
//            var index = 0
//            for (i in 0 until thresholds.size - 1) {
//                if (score >= thresholds[i] && score < thresholds[i + 1]) {
//                    index = i
//                    break
//                }
//            }
//            // Calculate position inside the segment (optional: finer placement)
//            val scoreInSegment = score - thresholds[index]
//            val segmentRange = thresholds[index + 1] - thresholds[index]
//            val fractionInSegment = scoreInSegment / segmentRange
//            // Final X position (can be center or start of segment)
//            val pointerX = (index + fractionInSegment) * segmentWidth
//            // Set X translation (center the pointer if needed)
//           // holder.pointer.translationX = pointerX - holder.pointer.width / 2
//        }
    }
    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}


data class AssessmentResultData(
    val assessment: String,
    val interpretation: String,
    val score: String,
    val level: String
)
data class Interpretation(
    val score: String,
    val level: String
)
