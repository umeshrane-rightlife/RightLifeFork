package com.jetsynthesys.rightlife.ui.mindaudit;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity;
import com.jetsynthesys.rightlife.ui.DialogUtils;

public class MindAuditBasicScreeningQuestionsActivity extends BaseActivity {
    public Button nextButton, submitButton;
    public boolean isFromThinkRight = false;
    ImageView ic_back_dialog, close_dialog;
    private ViewPager2 viewPager;
    private Button prevButton;
    private ProgressBar progressBar;
    private MindAuditBasicQuestionsAdapter adapter;
    private BasicScreeningQuestion basicScreeningQuestions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_mind_audit_from);
        ic_back_dialog = findViewById(R.id.ic_back_dialog);
        close_dialog = findViewById(R.id.ic_close_dialog);
        viewPager = findViewById(R.id.viewPager);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);

        basicScreeningQuestions = (BasicScreeningQuestion) getIntent().getSerializableExtra(MindAuditFeelingsFragment.ARG_BASIC_QUESTION);
        isFromThinkRight = getIntent().getBooleanExtra("FROM_THINK_RIGHT", false);

        adapter = new MindAuditBasicQuestionsAdapter(this);
        adapter.setData(basicScreeningQuestions);
        viewPager.setAdapter(adapter);

        prevButton.setOnClickListener(v -> navigateToPreviousPage());
        nextButton.setOnClickListener(v -> {
            disableViewForSeconds(nextButton, 1400);
            int currentItem = viewPager.getCurrentItem();
            Fragment fragment = adapter.getRegisteredFragment(currentItem);

            if (fragment instanceof OnNextButtonClickListener) {
                ((OnNextButtonClickListener) fragment).onNextClicked();
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonVisibility(position);
                updateProgress(position);
            }
        });

        ic_back_dialog.setOnClickListener(view -> {
            int currentItem = viewPager.getCurrentItem();
            int totalItems = adapter.getItemCount();

            if (currentItem == 0) {
                showExitDialog();
            }
            // If on any other page, move to the previous page
            else {
                viewPager.setCurrentItem(currentItem - 1);
            }
        });
        close_dialog.setOnClickListener(view -> {
            //finish();
            showExitDialog();
        });
    }

    private void updateButtonVisibility(int position) {
        int totalItems = adapter.getItemCount();

        if (position == totalItems - 1) {
            nextButton.setText("Submit");
        } else {
            nextButton.setText("Next");
        }
    }

    private void navigateToPreviousPage() {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    public void navigateToNextPage() {

        int currentItem = viewPager.getCurrentItem();
        int totalItems = adapter.getItemCount();
        // Go to the next page if it's not the last one
        if (currentItem < totalItems - 1) {
            viewPager.setCurrentItem(currentItem + 1);
        }
    }

    private void updateProgress(int fragmentIndex) {
        // Set progress percentage based on the current fragment (out of 8)
        int progressPercentage = (int) (((fragmentIndex + 1) / (double) adapter.getItemCount()) * 100);
        progressBar.setProgress(progressPercentage);
    }

    // Exit Dailog
    private void showExitDialog() {
        // Create the dialog
        DialogUtils.INSTANCE.showExitDialog(this,
                () -> {
                    finish();
                    return null;
                });
    }

    public interface OnNextButtonClickListener {
        void onNextClicked();
    }
    public static void disableViewForSeconds(final View view, long millis) {
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), millis);
    }

}
