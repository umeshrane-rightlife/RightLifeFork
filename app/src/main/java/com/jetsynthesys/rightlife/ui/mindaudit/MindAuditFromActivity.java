package com.jetsynthesys.rightlife.ui.mindaudit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.viewpager2.widget.ViewPager2;

import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.ui.DialogUtils;
import com.jetsynthesys.rightlife.ui.payment.AccessPaymentActivity;
import com.jetsynthesys.rightlife.ui.utility.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MindAuditFromActivity extends BaseActivity {

    public Button nextButton;
    public boolean isFromThinkRight;
    ImageView ic_back_dialog, close_dialog;
    private boolean isFromMindAuditResult;
    private ViewPager2 viewPager;
    private Button prevButton, submitButton;
    private MindAuditFormPagerAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setChildContentView(R.layout.activity_mind_audit_from);

        isFromThinkRight = getIntent().getBooleanExtra("FROM_THINK_RIGHT", false);
        isFromMindAuditResult = getIntent().getBooleanExtra("IS_FROM_MIND_AUDIT_RESULT", false);
        if (!isFromMindAuditResult)
            getAssessmentResult();

        ic_back_dialog = findViewById(R.id.ic_back_dialog);
        close_dialog = findViewById(R.id.ic_close_dialog);
        viewPager = findViewById(R.id.viewPager);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);

        adapter = new MindAuditFormPagerAdapter(this);
        viewPager.setAdapter(adapter);

        prevButton.setOnClickListener(v -> navigateToPreviousPage());
        nextButton.setOnClickListener(v -> navigateToNextPage());
        submitButton.setOnClickListener(v -> submitFormData());
        submitButton.setOnClickListener(view -> {
            Intent intent = new Intent(MindAuditFromActivity.this, AccessPaymentActivity.class);
            startActivity(intent);
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

            if (currentItem == 0)
                showExitDialog();
            else
                viewPager.setCurrentItem(currentItem - 1);

        });


        close_dialog.setOnClickListener(view -> {
            finishCurrentActivity();

        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int currentItem = viewPager.getCurrentItem();
                if (currentItem == 0)
                    finishCurrentActivity();
                else
                    viewPager.setCurrentItem(currentItem - 1);
            }
        });

    }

    private void updateButtonVisibility(int position) {
        int totalItems = adapter.getItemCount();

        if (position == totalItems - 1) {
            nextButton.setText("Submit");
        } else {
            nextButton.setText("Next");
        }
        if (totalItems == 1) {
            nextButton.setText("Next");
        }
    }

    private void submitFormData() {
    }


    private void navigateToPreviousPage() {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private void navigateToNextPage() {

        int currentItem = viewPager.getCurrentItem();
        int totalItems = adapter.getItemCount();
        // Go to the next page if it's not the last one
        if (currentItem < totalItems - 1) {
            viewPager.setCurrentItem(currentItem + 1);
        } else {
            // If it's the last page, got to scan
            DialogUtils.INSTANCE.showCommonBottomSheetDialog(
                    this,
                    "The assessments provided are for self-evaluation and awareness only, not for diagnostic use. They are designed for self-awareness and are based on widely recognized methodologies in the public domain. They are not a substitute for professional medical advice or psychological diagnoses, treatments, or consultations. If you have or suspect you may have a health condition, consult with a qualified healthcare provider.",
                    "Disclaimer",
                    "Okay",
                    () -> {                                // onOkayClick lambda
                        return null;
                    },
                    () -> {                                // onCloseClick lambda
                        return null;
                    },
                    R.color.color_think_right,
                    R.color.btn_color_journal
            );

        }
    }

    private void updateProgress(int fragmentIndex) {
        // Set progress percentage based on the current fragment (out of 8)
        if (adapter.getItemCount() == 1) {

            progressBar.setProgress(0);
            return;
        }
        int progressPercentage = (int) (((fragmentIndex + 1) / (double) adapter.getItemCount()) * 100);
        progressBar.setProgress(progressPercentage);
    }

    // Exit Dialog
    private void showExitDialog() {
        DialogUtils.INSTANCE.showExitDialog(this,
                () -> {
                    finish();
                    return null;
                });
    }

    private void finishCurrentActivity() {
        finish();
        /*if (isFromMindAuditResult)
            finish();
        else {
            Intent intent = new Intent(MindAuditFromActivity.this, HomeNewActivity.class);
            intent.putExtra("FROM_THINK_RIGHT", isFromThinkRight);
            startActivity(intent);
            finishAffinity();
        }*/
    }

    private void getAssessmentResult() {
        Utils.showLoader(this);
        Call<MindAuditResultResponse> call = apiService.getMindAuditAssessmentResult(sharedPreferenceManager.getAccessToken());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<MindAuditResultResponse> call, Response<MindAuditResultResponse> response) {
                Utils.dismissLoader(MindAuditFromActivity.this);
                if (response.isSuccessful() && response.body() != null) {
                    if (!response.body().getResult().isEmpty()) {
                        Intent intent = new Intent(MindAuditFromActivity.this, MindAuditResultActivity.class);
                        if (!response.body().getResult().isEmpty()) {
                            intent.putExtra("REPORT_ID", response.body().getResult().get(0).getId());
                            if (!response.body().getResult().get(0).getAssessmentsTaken().isEmpty())
                                intent.putExtra("Assessment", response.body().getResult().get(0).getAssessmentsTaken().get(0).getAssessment());
                        }
                        intent.putExtra("FROM_THINK_RIGHT", isFromThinkRight);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(MindAuditFromActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MindAuditResultResponse> call, Throwable t) {
                handleNoInternetView(t);
                Utils.dismissLoader(MindAuditFromActivity.this);
            }
        });
    }

}