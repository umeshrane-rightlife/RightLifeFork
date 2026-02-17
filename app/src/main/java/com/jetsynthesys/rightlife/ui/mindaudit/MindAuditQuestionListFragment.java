package com.jetsynthesys.rightlife.ui.mindaudit;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.ui.mindaudit.questions.Question;
import com.jetsynthesys.rightlife.ui.mindaudit.questions.ScoringPattern;

import java.util.ArrayList;

public class MindAuditQuestionListFragment extends Fragment {
    private static final String ARG_QUESTION = "QUESTION";
    private static final String ARG_POSITION = "POSITION";
    public static boolean isSubmitClickable = true;
    private Question question;
    private TextView txt_question;
    private RecyclerView recyclerView;
    //private OnNextFragmentClickListener onNextFragmentClickListener;
    private MindAuditOptionsAdapter adapter;
    private int position = 0;

    public static MindAuditQuestionListFragment newInstance(Question question, int position) {
        MindAuditQuestionListFragment fragment = new MindAuditQuestionListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_QUESTION, question);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            question = (Question) getArguments().getSerializable(ARG_QUESTION);
            position = getArguments().getInt(ARG_POSITION, 0);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //onNextFragmentClickListener = (OnNextFragmentClickListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mind_audit_question_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        txt_question = view.findViewById(R.id.txt_question);

        txt_question.setText(question.getQuestion());
        if (question.getQuestion() == null || question.getQuestion().isEmpty())
            txt_question.setVisibility(View.GONE);
        else
            txt_question.setVisibility(View.VISIBLE);
        ((MAAssessmentQuestionaireActivity) requireActivity()).nextButton.setVisibility(View.GONE);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        adapter = new MindAuditOptionsAdapter(requireContext(), (ArrayList<ScoringPattern>) question.getScoringPattern(), scoringPattern -> {
            ((MAAssessmentQuestionaireActivity) requireActivity()).addScore(question, scoringPattern);
            MAAssessmentQuestionaireActivity activity = (MAAssessmentQuestionaireActivity) requireActivity();
            String header = activity.header;

            String currentOption = scoringPattern.getOption();

            if (activity.adapter.getItemCount() - 1 == position && ("PHQ-9".equalsIgnoreCase(header) || "DASS-21".equalsIgnoreCase(header)) && (!(currentOption.equalsIgnoreCase("Not difficult at all") || currentOption.equalsIgnoreCase("Not at all") || currentOption.equalsIgnoreCase("Did not apply to me at all")))) {
                isSubmitClickable = false;
                activity.openSecondActivity();
            } else {
                isSubmitClickable = true;
                new Handler().postDelayed(() -> {
                    if (question.isContinueFurtherIfTrue()) {
                        activity.submitButton.setVisibility(View.VISIBLE);
                    } else {
                        activity.navigateToNextPage();
                    }
                }, 800);
            }
        });
        recyclerView.setAdapter(adapter);

        return view;
    }
}