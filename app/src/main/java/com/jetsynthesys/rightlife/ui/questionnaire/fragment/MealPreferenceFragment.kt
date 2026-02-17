package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.FragmentMealPreferenceBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireEatRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.adapter.MealOptionAdapter
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.ERQuestionTwo
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.MealOption
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.utility.runWithCooldown

class MealPreferenceFragment : Fragment() {

    private var _binding: FragmentMealPreferenceBinding? = null
    private val binding get() = _binding!!

    private val mealOptions = listOf(
        MealOption(R.drawable.ic_meal_1, "1-2 meals-it's enough for me"),
        MealOption(R.drawable.ic_meal_2, "3 meals-a balanced routine"),
        MealOption(R.drawable.ic_meal_3, "4-5 meals-I eat often but manage well"),
        MealOption(R.drawable.ic_meal_4, "6+ meals-I eat throughout the day")
    )

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): MealPreferenceFragment {
            val fragment = MealPreferenceFragment()
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
        _binding = FragmentMealPreferenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = MealOptionAdapter(mealOptions, { selectedOption: MealOption ->
            submit(selectedOption.title)
        }.runWithCooldown())
        binding.rvMealOptions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMealOptions.adapter = adapter
    }

    private fun submit(answer: String) {
        val questionTwo = ERQuestionTwo()
        questionTwo.answer = answer
        QuestionnaireEatRightActivity.questionnaireAnswerRequest.eatRight?.questionTwo = questionTwo
        QuestionnaireEatRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireEatRightActivity.questionnaireAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
