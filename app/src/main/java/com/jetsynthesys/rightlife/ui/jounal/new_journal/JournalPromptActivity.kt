package com.jetsynthesys.rightlife.ui.jounal.new_journal

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityJournalPromptBinding
import com.jetsynthesys.rightlife.ui.DialogUtils
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.format.DateTimeFormatter

class JournalPromptActivity : BaseActivity() {

    private lateinit var binding: ActivityJournalPromptBinding
    private lateinit var adapter: PromptAdapter
    private var questionsList: ArrayList<Question> = ArrayList()
    private var questions4: ArrayList<Question> = ArrayList()
    private var sectionList: ArrayList<Section> = ArrayList()
    private var isFromThinkRight = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJournalPromptBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        sharedPreferenceManager = SharedPreferenceManager.getInstance(this)

        val journalItem: JournalItem = intent.getSerializableExtra("Section") as JournalItem
        isFromThinkRight = intent.getBooleanExtra("FROM_THINK_RIGHT", false)

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnInfo.setOnClickListener {
            val htmlText = if (journalItem.title == "Gratitude") {
                """
    <p>This practice centers on noticing what’s going well—no matter how big or small.</p>
    <p>Gratitude Journaling has been shown to boost mood, shift perspective, and build emotional resilience.</p>
    <p>Even a few simple entries can help rewire your focus toward the positive.</p>
""".trimIndent()
            } else if (journalItem.title == "Bullet") {
                """
    <p>Bullet Journaling helps organize your inner world in small, manageable pieces.</p>
    <p>Use it to list your moods, wins, worries, intentions—or anything else on your mind.</p>
    <p>It’s a great option when you don’t feel like writing full paragraphs but still want to check in with yourself.</p>
""".trimIndent()
            } else {
                """
    <p>Grief Journaling is a safe place to hold pain, memories, questions, or anger.</p>
    <p>It’s for anyone navigating loss, change, or heartache.</p>
    <p>There’s no right way to grieve—this space is here to let your feelings breathe, however they show up.</p>
""".trimIndent()
            }


            DialogUtils.showJournalCommonDialog(this, journalItem.title?:"", htmlText)
        }
        // write a code here to show subtext according to journal item
        val subtext = when
        {
            journalItem.title.equals("Gratitude", ignoreCase = true) -> "Reflect on small joys and big wins to build a thankful mindset."
            journalItem.title.equals("Bullet", ignoreCase = true) -> "Quick prompts to help you track, organize, and reflect with clarity."
            journalItem.title.equals("Grief", ignoreCase = true) -> "Each question is here to help you process, remember, and heal—one step at a time."
            else -> "" // Default empty text for any other case
        }

        binding.tvSubtitle.text = subtext


        getSections(journalItem.id!!)

        setSelectedChipListener()

        // Setup RecyclerView
        adapter = PromptAdapter(questions4, object : PromptAdapter.OnItemClickListener {
            override fun onItemClick(question: Question) {
                startActivity(
                    Intent(
                        this@JournalPromptActivity,
                        GriefJournalActivity::class.java
                    ).apply {
                        putExtra("Section", journalItem)
                        putExtra("Answer", question.question)
                        putExtra("QuestionList", questionsList)
                        putExtra("Position", questionsList.indexOf(question))
                        putExtra("FROM_THINK_RIGHT", isFromThinkRight)
                    })
            }

            override fun onSwapClick(question: Question, position: Int) {
           /*     if (questionsList.isNotEmpty() && questionsList.size > 4) {
                    questionsList[position] = questionsList[4]
                    questionsList.removeAt(position)
                    questionsList.add(question)
                    questions4.clear()
                    questions4.addAll(questionsList.take(4))
                    adapter.notifyDataSetChanged()
                }*/
                handleQuestionReplacement(position)
            }

        })
        binding.recyclerViewPrompts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPrompts.adapter = adapter
    }

    private fun addChip(name: String, isSelected: Boolean) {
        val chip = Chip(this)
        chip.id = View.generateViewId() // Generate unique ID
        chip.text = name
        chip.isCheckable = true
        chip.isChecked = false
        chip.chipCornerRadius = 50f
        chip.chipStrokeColor = ContextCompat.getColorStateList(
            this,
            R.color.menuselected
        )

        if (isSelected) {
            chip.isChecked = true
        }

        chip.textSize = 11f

        val heightInDp = 50 // or whatever height you want
        val heightInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            heightInDp.toFloat(),
            resources.displayMetrics
        ).toInt()

        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            heightInPx
        )

        chip.layoutParams = layoutParams


        // Set different colors for selected state
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                ContextCompat.getColor(this, R.color.menuselected),
                ContextCompat.getColor(this, R.color.white)
            )
        )

        val textColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                ContextCompat.getColor(this, R.color.white),
                ContextCompat.getColor(this, R.color.chip_text_color)
            )
        )
        chip.chipBackgroundColor = colorStateList

        // Text color for selected state
        chip.setTextColor(textColorStateList)
        binding.chipGroup.addView(chip)
    }

    private fun setSelectedChipListener() {
        binding.chipGroup.setOnCheckedChangeListener { group: ChipGroup, checkedId: Int ->
            val chip = group.findViewById<Chip>(checkedId)
            if (chip != null) {
                var position = -1
                for (i in 0 until group.childCount) {
                    val child = group.getChildAt(i)
                    if (child.id == checkedId) {
                        position = i
                        break
                    }
                }

                // Use the position as needed
                if (position != -1) {
                    sectionList[position].id?.let { getQuestions(it) }
                }
            }
        }
    }


    private fun getSections(id: String, type: String = "section") {
        val call = apiService.getJournalSections(sharedPreferenceManager.accessToken, type, id)

        call.enqueue(object : Callback<JournalSectionResponse> {
            override fun onResponse(
                call: Call<JournalSectionResponse>,
                response: Response<JournalSectionResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.data?.let { sectionList.addAll(it) }

                    // Check if only default section exists
                    val hasOnlyDefaultSection = sectionList.size == 1 &&
                            (sectionList[0].sectionName.equals("default", ignoreCase = true))

                    if (hasOnlyDefaultSection) {
                        // Hide chip list/chip group
                        binding.chipGroup.visibility = View.GONE  // Replace with your actual chip container ID

                        // Still load the questions for the default section
                        sectionList[0].id?.let { getQuestions(it) }
                    } else {
                        // Show chip list
                        binding.chipGroup.visibility = View.VISIBLE  // Replace with your actual chip container ID

                        // Add chips for all sections
                        for (i in 0 until sectionList.size) {
                            if (i == 0) {
                                sectionList[0].sectionName?.let { addChip(it, true) }
                                sectionList[0].id?.let { getQuestions(it) }
                            } else {
                                sectionList[i].sectionName?.let { addChip(it, false) }
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        this@JournalPromptActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<JournalSectionResponse>, t: Throwable) {
                handleNoInternetView(t)
            }

        })
    }

    private fun getQuestions(id: String, type: String = "question") {
        val call = apiService.getJournalQuestions(sharedPreferenceManager.accessToken, type, id)

        call.enqueue(object : Callback<JournalQuestionsResponse> {
            override fun onResponse(
                call: Call<JournalQuestionsResponse>,
                response: Response<JournalQuestionsResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    questionsList.clear()
                    questions4.clear()
                    response.body()?.data?.let { questionsList.addAll(it) }
                    questions4.addAll(questionsList)
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        this@JournalPromptActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<JournalQuestionsResponse>, t: Throwable) {
                handleNoInternetView(t)
            }

        })
    }

    //single question replacement function
    /**
     * Replaces the clicked question with the next available question
     * The clicked question is moved to the end of the list (can come back later)
     */
    private fun handleQuestionReplacement(position: Int) {
        // Validate we have more questions to show
        if (questionsList.size <= 4) {
            Toast.makeText(
                this,
                "No more prompts available",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Safety check for position
        if (position < 0 || position >= questions4.size || position >= questionsList.size) {
            return
        }

        // Store the old question that will be replaced
        val oldQuestion = questionsList[position]

        // Get the next question (always at index 4 since we only show first 4)
        val nextQuestion = questionsList[4]

        // Replace the clicked question at its position with the next question
        questionsList[position] = nextQuestion

        // Remove the next question from index 4 (since we just moved it to 'position')
        questionsList.removeAt(4)

        // Add the old question to the end of the list (so it can come back later)
        questionsList.add(oldQuestion)

        // Update the display list (first 4 items)
        questions4.clear()
        questions4.addAll(questionsList.take(4))

        // Animate only the changed item for smooth UX
        adapter.notifyItemChanged(position)
    }


}
