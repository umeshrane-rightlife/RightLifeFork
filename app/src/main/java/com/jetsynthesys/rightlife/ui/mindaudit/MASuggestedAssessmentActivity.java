package com.jetsynthesys.rightlife.ui.mindaudit;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient;
import com.jetsynthesys.rightlife.ai_package.model.ThinkRecomendedResponse;
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.adapter.RecommendationAdapter;
import com.jetsynthesys.rightlife.databinding.DialogMindAuditDisclaimerBinding;
import com.jetsynthesys.rightlife.ui.DialogUtils;
import com.jetsynthesys.rightlife.ui.utility.AppConstants;
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceConstants;
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MASuggestedAssessmentActivity extends BaseActivity {
    private final ArrayList<String> suggestedAssessmentString = new ArrayList<>();
    private final ArrayList<String> allAssessments = new ArrayList<>();
    private ImageView ic_back_dialog, close_dialog;
    private RecyclerView rvSuggestedAssessment, rvAllAssessment, recyclerViewAlsolike;
    private SuggestedAssessmentAdapter suggestedAssessmentAdapter;
    private AllAssessmentAdapter allAssessmentAdapter;
    private Assessments assessments;
    private String selectedAssessment;
    private boolean isFromThinkRight = false;
    private TextView tv_all_assessment;
    private boolean isDisclaimerDialogShowing = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_ma_suggested_assessment);

        ic_back_dialog = findViewById(R.id.ic_back_dialog);
        close_dialog = findViewById(R.id.ic_close_dialog);
        tv_all_assessment = findViewById(R.id.tv_all_assessment);

        ic_back_dialog.setOnClickListener(view -> {
            finish();
        });
        close_dialog.setOnClickListener(view -> {
            showExitDialog();
        });

        rvSuggestedAssessment = findViewById(R.id.rv_suggested_assessment);
        rvAllAssessment = findViewById(R.id.rv_all_assessment);
        recyclerViewAlsolike = findViewById(R.id.recommendationRecyclerView);

        assessments = (Assessments) getIntent().getSerializableExtra("AssessmentData");
        selectedAssessment = getIntent().getStringExtra("SelectedAssessment");
        isFromThinkRight = getIntent().getBooleanExtra("FROM_THINK_RIGHT", false);

        setUpUI();

        UserEmotions userEmotions = SharedPreferenceManager.getInstance(this).getUserEmotions();
        getSuggestedAssessment(userEmotions);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedAssessment != null) {
            if (!isDisclaimerDialogShowing) {
                showDisclaimerDialog(selectedAssessment);
            }
        } else {
            fetchThinkRecomendedData();
        }
    }

    private void showDisclaimerDialog(String header) {
        DialogMindAuditDisclaimerBinding binding =
                DialogMindAuditDisclaimerBinding.inflate(LayoutInflater.from(this));

        BottomSheetDialog bottomSheetDialog =
                new BottomSheetDialog(this, R.style.TransparentBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(binding.getRoot());
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);
        // ✅ Force transparent background for system container
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
                bottomSheet.setClipToOutline(false);
            }
        });

        // ✅ Set rounded background on your root layout
        binding.getRoot().setBackground(ContextCompat.getDrawable(this, R.drawable.roundedcornershape));

        // ✅ Dim background
        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = 0.7f;
            window.setAttributes(params);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // ✅ Slide up animation
        Animation slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up);
        binding.getRoot().startAnimation(slideUpAnimation);

        // Header and content
        binding.tvSelectedAssessment.setText(header);
        setDialogText(binding.itemText1, binding.itemText2, binding.itemText3, header);

        // Close button
        binding.icCloseDialog.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            isDisclaimerDialogShowing = false;
            if (selectedAssessment != null) finish();
        });

        // Handle back press
        if (selectedAssessment != null) {
            bottomSheetDialog.setOnKeyListener((dialogInterface, keyCode, keyEvent) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    dialogInterface.dismiss();
                    finish();
                    return true;
                }
                return false;
            });
        }

        // Start assessment
        binding.btnTakeAssessment.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            isDisclaimerDialogShowing = false;
            Intent intent = new Intent(MASuggestedAssessmentActivity.this, MAAssessmentQuestionaireActivity.class);
            intent.putExtra("AssessmentType", header);
            intent.putExtra("FROM_THINK_RIGHT", isFromThinkRight);
            startActivity(intent);
        });
        bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetDialog.getBehavior().setDraggable(false);
        bottomSheetDialog.getBehavior().setSkipCollapsed(true);
// Handle slide-down dismiss (when user swipes down the bottom sheet)
        bottomSheetDialog.getBehavior().addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN ||
                        newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetDialog.dismiss();
                    isDisclaimerDialogShowing = false;
                    if (selectedAssessment != null) finish();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // You can optionally animate dim or background blur here if needed
            }
        });


        bottomSheetDialog.show();
        isDisclaimerDialogShowing = true;
    }

    private void setDialogText(TextView tvItem1, TextView tvItem2, TextView tvItem3, String header) {
        switch (header) {
            case "DASS-21": {
                tvItem1.setText(AppConstants.dass21FirstPara);
                tvItem2.setText(AppConstants.dass21SecondPara);
                tvItem3.setText(AppConstants.dass21ThirdPara);
                break;
            }
            case "Sleep Audit": {
                tvItem1.setText(AppConstants.ssFirstPara);
                tvItem2.setText(AppConstants.ssSecondPara);
                tvItem3.setText(AppConstants.ssThirdPara);
                break;
            }
            case "GAD-7": {
                tvItem1.setText(AppConstants.gad7FirstPara);
                tvItem2.setText(AppConstants.gad7SecondPara);
                tvItem3.setText(AppConstants.gad7ThirdPara);
                break;
            }
            case "OHQ": {
                tvItem1.setText(AppConstants.ohqFirstPara);
                tvItem2.setText(AppConstants.ohqSecondPara);
                tvItem3.setText(AppConstants.ohqThirdPara);
                break;
            }
            case "CAS": {
                tvItem1.setText(AppConstants.casFirstPara);
                tvItem2.setText(AppConstants.casSecondPara);
                tvItem3.setText(AppConstants.casThirdPara);
                break;
            }
            case "PHQ-9": {
                tvItem1.setText(AppConstants.phq9FirstPara);
                tvItem2.setText(AppConstants.phq9SecondPara);
                tvItem3.setText(AppConstants.phq9ThirdPara);
                break;
            }

        }
    }

    private void showExitDialog() {
        // Create the dialog
        DialogUtils.INSTANCE.showExitDialog(this,
                () -> {
                    finish();
                    return null;
                });
    }


    private void fetchThinkRecomendedData() {

        Call<ThinkRecomendedResponse> call = ApiClient.INSTANCE.getApiService().fetchThinkRecomended(sharedPreferenceManager.getAccessToken(), "HOME", "THINK_RIGHT");
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ThinkRecomendedResponse> call, Response<ThinkRecomendedResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ThinkRecomendedResponse thinkRecomendedResponse = response.body();
                    if (thinkRecomendedResponse.getData() != null) {
                        thinkRecomendedResponse.getData().getContentList();
                        if (!thinkRecomendedResponse.getData().getContentList().isEmpty()) {
                            RecommendationAdapter recomendationAdapter = new RecommendationAdapter(
                                    MASuggestedAssessmentActivity.this,
                                    thinkRecomendedResponse.getData().getContentList()
                            );
                            recyclerViewAlsolike.setLayoutManager(
                                    new LinearLayoutManager(MASuggestedAssessmentActivity.this)
                            );
                            recyclerViewAlsolike.setAdapter(recomendationAdapter);
                        }
                    }
                } else {
                    try {
                        Log.e("Error", "Response not successful: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ThinkRecomendedResponse> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDisclaimerDialogShowing = false;
    }

    private void getSuggestedAssessment(UserEmotions userEmotions) {
        String accessToken = sharedPreferenceManager.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        Call<ResponseBody> call = apiService.getSuggestedAssessment(accessToken, userEmotions);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //Toast.makeText(requireContext(), "Success: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        String jsonString = response.body().string();
                        Gson gson = new Gson();
                        assessments = gson.fromJson(jsonString, Assessments.class);
                        setUpUI();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(MASuggestedAssessmentActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MASuggestedAssessmentActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpUI(){
        if (assessments != null) {
            SuggestedAssessments suggestedAssessments = assessments.suggestedAssessments;

            rvSuggestedAssessment.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvAllAssessment.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            suggestedAssessmentAdapter = new SuggestedAssessmentAdapter(this, suggestedAssessmentString, this::showDisclaimerDialog);
            rvSuggestedAssessment.setAdapter(suggestedAssessmentAdapter);
            rvSuggestedAssessment.scrollToPosition(0);

            AllAssessment assessment = assessments.allAssessment;

            if (assessment.getDass21() != null) {
                allAssessments.add(assessment.getDass21());
            } else {
                suggestedAssessmentString.add("DASS-21");
            }

            if (assessment.getSleepAudit() != null) {
                //allAssessments.add(assessment.getSleepAudit());
            } else {
                //suggestedAssessmentString.add("Sleep Audit");
            }

            if (assessment.getGad7() != null) {
                allAssessments.add(assessment.getGad7());
            } else {
                suggestedAssessmentString.add("GAD-7");
            }
            if (assessment.getOhq() != null) {
                allAssessments.add(assessment.getOhq());
            } else {
                suggestedAssessmentString.add("OHQ");
            }

            if (assessment.getCas() != null) {
                allAssessments.add(assessment.getCas());
            } else {
                suggestedAssessmentString.add("CAS");
            }

            if (assessment.getPhq9() != null) {
                allAssessments.add(assessment.getPhq9());
            } else {
                suggestedAssessmentString.add("PHQ-9");
            }


            allAssessmentAdapter = new AllAssessmentAdapter(this, allAssessments, this::showDisclaimerDialog);
            rvAllAssessment.setAdapter(allAssessmentAdapter);
            rvAllAssessment.scrollToPosition(0);
            if (allAssessments.isEmpty()) {
                tv_all_assessment.setVisibility(View.GONE);
            }
        }
    }
}
