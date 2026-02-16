package com.jetsynthesys.rightlife.ui.mindaudit;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.ui.DialogUtils;
import com.jetsynthesys.rightlife.ui.healthaudit.HealthAuditPagerAdapter;
import com.zhpan.indicator.IndicatorView;

import me.relex.circleindicator.CircleIndicator3;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MindAuditActivity extends BaseActivity {

    ImageView ic_back_dialog, close_dialog;
    HealthAuditPagerAdapter adapter;
    Button btn_howitworks;
    boolean isFromThinkRight = false;

    /**
     * {@inheritDoc}
     * <p>
     * Perform initialization of all fragments.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_mindaudit);

        getAssessmentResult();

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        ic_back_dialog = findViewById(R.id.ic_back_dialog);
        close_dialog = findViewById(R.id.ic_close_dialog);
        btn_howitworks = findViewById(R.id.btn_howitworks);

        CircleIndicator3 indicator = findViewById(R.id.indicator);
        IndicatorView indicator_view = findViewById(R.id.indicator_view);
        indicator_view.setSliderHeight(21);
        indicator_view.setSliderWidth(80);
        // Array of layout resources to use in the ViewPager
        int[] layouts = {
                R.layout.page_one_mind, // Define these layout files in your res/layout directory
                R.layout.page_two_mind,
        };

        isFromThinkRight = getIntent().getBooleanExtra("FROM_THINK_RIGHT",false);

        // Set up the adapter
        adapter = new HealthAuditPagerAdapter(layouts);

        viewPager.setAdapter(adapter);
        indicator.setViewPager(viewPager);
        indicator_view.setupWithViewPager(viewPager);


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

        btn_howitworks.setOnClickListener(view -> {
            int currentItem = viewPager.getCurrentItem();
            int totalItems = adapter.getItemCount();
            // Go to the next page if it's not the last one
            if (currentItem < totalItems - 1) {
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                Intent intent = new Intent(MindAuditActivity.this, MindAuditFromActivity.class);
                intent.putExtra("FROM_THINK_RIGHT",isFromThinkRight);
                startActivity(intent);
            }
        });

        // Add page change callback to update button text
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonText(position);
            }
        });
    }

    // Method to update button text based on the current page
    private void updateButtonText(int position) {
        int totalItems = adapter.getItemCount();

        if (position == totalItems - 1) {
            btn_howitworks.setText("Begin");
        } else {
            btn_howitworks.setText("Next Page");
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

    private void getAssessmentResult() {

        Call<MindAuditResultResponse> call = apiService.getMindAuditAssessmentResult(sharedPreferenceManager.getAccessToken());
        call.enqueue(new Callback<MindAuditResultResponse>() {
            @Override
            public void onResponse(Call<MindAuditResultResponse> call, Response<MindAuditResultResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!response.body().getResult().isEmpty()) {
                        Intent intent = new Intent(MindAuditActivity.this, MindAuditResultActivity.class);
                        if (!response.body().getResult().isEmpty()) {
                            intent.putExtra("REPORT_ID", response.body().getResult().get(0).getId());
                            if (!response.body().getResult().get(0).getAssessmentsTaken().isEmpty())
                                intent.putExtra("Assessment", response.body().getResult().get(0).getAssessmentsTaken().get(0).getAssessment());
                        }
                        intent.putExtra("FROM_THINK_RIGHT",isFromThinkRight);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(MindAuditActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MindAuditResultResponse> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }

}
