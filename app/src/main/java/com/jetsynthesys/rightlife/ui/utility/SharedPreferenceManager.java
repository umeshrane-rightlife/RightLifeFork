package com.jetsynthesys.rightlife.ui.utility;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse;
import com.jetsynthesys.rightlife.newdashboard.model.ChecklistResponse;
import com.jetsynthesys.rightlife.ui.mindaudit.MindAuditAssessmentSaveRequest;
import com.jetsynthesys.rightlife.ui.mindaudit.UserEmotions;
import com.jetsynthesys.rightlife.ui.new_design.pojo.InterestDataList;
import com.jetsynthesys.rightlife.ui.new_design.pojo.LoggedInUser;
import com.jetsynthesys.rightlife.ui.new_design.pojo.ModuleTopic;
import com.jetsynthesys.rightlife.ui.new_design.pojo.OnboardingQuestionRequest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferenceManager {

    private static final String PREF_NAME = "app_shared_prefs"; // File name for SharedPreferences
    private static SharedPreferenceManager instance;
    private final SharedPreferences sharedPreferences;

    private SharedPreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Singleton instance
    public static SharedPreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferenceManager(context.getApplicationContext());
        }
        return instance;
    }

    // Method to save the access token
    public void saveAccessToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.ACCESS_TOKEN, token);
        editor.apply(); // Apply changes asynchronously
    }

    // Method to retrieve the access token
    public String getAccessToken() {
        return sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, "");
    }

    public void saveUserFirstVisit(String visit) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.FIRST_VISIT, visit);
        editor.apply(); // Apply changes asynchronously
    }

    // Method to retrieve the access token
    public String getUserFirstVisit() {
        return sharedPreferences.getString(SharedPreferenceConstants.FIRST_VISIT, "");
    }

    public void saveSyncFirstVisit(String visit) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.FIRST_SYNC, visit);
        editor.apply(); // Apply changes asynchronously
    }

    // Method to retrieve the access token
    public String getSyncFirstVisit() {
        return sharedPreferences.getString(SharedPreferenceConstants.FIRST_SYNC, "");
    }

    // Method to save the user ID
    public void saveUserId(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.USER_ID, userId);
        editor.apply();
    }

    // Method to retrieve the user ID
    public String getUserId() {
        return sharedPreferences.getString(SharedPreferenceConstants.USER_ID, "");
    }

    public void saveDeviceName(String deviceName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.DEVICE_NAME, deviceName);
        editor.apply();
    }

    // Method to retrieve the user ID
    public String getDeviceName() {
        return sharedPreferences.getString(SharedPreferenceConstants.DEVICE_NAME, "android phone");
    }

    public void saveMoveRightSyncTime(String time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.SYNC_TIME, time);
        editor.apply();
    }

    // Method to retrieve the user ID
    public String getMoveRightSyncTime() {
        return sharedPreferences.getString(SharedPreferenceConstants.SYNC_TIME, "");
    }

    // Clear the access token and user ID (for example, when logging out)
    public void clearData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(SharedPreferenceConstants.ACCESS_TOKEN);
        editor.remove(SharedPreferenceConstants.USER_ID);
        editor.clear();
        editor.apply();
    }

    public void saveUserProfile(UserProfileResponse userProfileResponse) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userProfileResponse);
        prefsEditor.putString(SharedPreferenceConstants.USER_PROFILE, json);
        prefsEditor.apply();
    }

    public UserEmotions getUserEmotions() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SharedPreferenceConstants.USER_EMOTIONS, "");
        UserEmotions obj = gson.fromJson(json, UserEmotions.class);
        return obj;
    }

    public void saveUserEmotions(UserEmotions userEmotions) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userEmotions);
        prefsEditor.putString(SharedPreferenceConstants.USER_EMOTIONS, json);
        prefsEditor.apply();
    }

    public UserProfileResponse getUserProfile() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SharedPreferenceConstants.USER_PROFILE, "");
        UserProfileResponse obj = gson.fromJson(json, UserProfileResponse.class);
        return obj;
    }

    public void saveChecklistResponse(ChecklistResponse checklistResponse) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(checklistResponse);
        prefsEditor.putString(SharedPreferenceConstants.CHECKLISTDATA, json);
        prefsEditor.apply();
    }

    public ChecklistResponse getChecklistResponse() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SharedPreferenceConstants.CHECKLISTDATA, "");
        ChecklistResponse obj = gson.fromJson(json, ChecklistResponse.class);
        return obj;
    }

    public void saveVoiceScanAnswerId(String answerId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.VOICE_SCAN_ANSWER_ID, answerId);
        editor.apply();
    }

    public String getVoiceScanAnswerId() {
        return sharedPreferences.getString(SharedPreferenceConstants.VOICE_SCAN_ANSWER_ID, "");
    }

    public void saveOnboardingQuestionAnswer(OnboardingQuestionRequest onboardingQuestionRequest) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(onboardingQuestionRequest);
        prefsEditor.putString(SharedPreferenceConstants.ON_BOARDING_QUESTIONS, json);
        prefsEditor.apply();
    }

    public OnboardingQuestionRequest getOnboardingQuestionRequest() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SharedPreferenceConstants.ON_BOARDING_QUESTIONS, "");
        OnboardingQuestionRequest obj = gson.fromJson(json, OnboardingQuestionRequest.class);
        if (obj == null) {
            obj = new OnboardingQuestionRequest();
        }
        return obj;
    }

    public void clearOnboardingQuestionRequest() {
        sharedPreferences.edit().remove(SharedPreferenceConstants.ON_BOARDING_QUESTIONS).apply();
    }

    public String getSelectedOnboardingModule() {
        return sharedPreferences.getString(SharedPreferenceConstants.ON_BOARDING_SELECTED_MODULE, "");
    }

    public void setSelectedOnboardingModule(String moduleName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.ON_BOARDING_SELECTED_MODULE, moduleName);
        editor.apply();
    }

    public String getSelectedOnboardingSubModule() {
        return sharedPreferences.getString(SharedPreferenceConstants.ON_BOARDING_SELECTED_SUB_MODULE, "");
    }

    public void setSelectedOnboardingSubModule(String moduleName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.ON_BOARDING_SELECTED_SUB_MODULE, moduleName);
        editor.apply();
    }

    public void saveMindAuditRequest(MindAuditAssessmentSaveRequest mindAuditAssessmentSaveRequest) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mindAuditAssessmentSaveRequest);
        prefsEditor.putString(SharedPreferenceConstants.MIND_AUDIT_FEELINGS, json);
        prefsEditor.apply();
    }

    public MindAuditAssessmentSaveRequest getMindAuditRequest() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SharedPreferenceConstants.MIND_AUDIT_FEELINGS, "");
        MindAuditAssessmentSaveRequest obj = gson.fromJson(json, MindAuditAssessmentSaveRequest.class);
        if (obj == null) {
            obj = new MindAuditAssessmentSaveRequest();
        }
        return obj;
    }

    public void clearMindAuditRequest() {
        sharedPreferences.edit().remove(SharedPreferenceConstants.MIND_AUDIT_FEELINGS).apply();
    }

    public void saveAppMode(String mode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.APP_MODE, mode);
        editor.apply();
    }

    public String getAppMode() {
        return sharedPreferences.getString(SharedPreferenceConstants.APP_MODE, "System");
    }

    public <LoggedInUser> void setLoggedInUsers(ArrayList<LoggedInUser> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.LOGGED_IN_USER, json);
        editor.apply();
    }

    public ArrayList<LoggedInUser> getLoggedUserList() {
        ArrayList<LoggedInUser> arrayItems = new ArrayList<>();
        String serializedObject = sharedPreferences.getString(SharedPreferenceConstants.LOGGED_IN_USER, null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<LoggedInUser>>() {
            }.getType();
            arrayItems = gson.fromJson(serializedObject, type);
        }
        return arrayItems;
    }

    public String getUserName() {
        return sharedPreferences.getString(SharedPreferenceConstants.USERNAME, "");
    }

    public void setUserName(String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.USERNAME, username);
        editor.apply();
    }

    public String getDisplayName() {
        return sharedPreferences.getString(SharedPreferenceConstants.DISPLAY_NAME, "");
    }

    public void setDisplayName(String displayName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.DISPLAY_NAME, displayName);
        editor.apply();
    }

    public String getEmail() {
        return sharedPreferences.getString(SharedPreferenceConstants.USER_EMAIL, "");
    }

    public void setEmail(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.USER_EMAIL, email);
        editor.apply();
    }

    public String getSelectedWellnessFocus() {
        return sharedPreferences.getString(SharedPreferenceConstants.SELECTED_WELLNESS_FOCUS, "");
    }

    public void setSelectedWellnessFocus(String wellnessFocus) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.SELECTED_WELLNESS_FOCUS, wellnessFocus);
        editor.apply();
    }

    public ArrayList<ModuleTopic> getWellnessFocusTopics() {
        ArrayList<ModuleTopic> arrayItems = new ArrayList<>();
        String serializedObject = sharedPreferences.getString(SharedPreferenceConstants.SELECTED_WELLNESS_FOCUS_TOPICS, null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ModuleTopic>>() {
            }.getType();
            arrayItems = gson.fromJson(serializedObject, type);
        }
        return arrayItems;
    }

    public <ModuleTopic> void setWellnessFocusTopics(ArrayList<ModuleTopic> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.SELECTED_WELLNESS_FOCUS_TOPICS, json);
        editor.apply();
    }

    public Boolean getUnLockPower() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.UNLOCK_POWER, false);
    }

    public void setUnLockPower(boolean isUnlock) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.UNLOCK_POWER, isUnlock);
        editor.apply();
    }

    public Boolean getCreateUserName() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.CREATE_USERNAME, false);
    }

    public void setCreateUserName(boolean isCreateUser) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.CREATE_USERNAME, isCreateUser);
        editor.apply();
    }

    public Boolean getThirdFiller() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.THIRD_FILLER, false);
    }

    public void setThirdFiller(boolean isThirdFiller) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.THIRD_FILLER, isThirdFiller);
        editor.apply();
    }

    public Boolean getInterest() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.INTEREST, false);
    }

    public void setInterest(boolean isThirdFiller) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.INTEREST, isThirdFiller);
        editor.apply();
    }

    public ArrayList<InterestDataList> getSavedInterest() {
        ArrayList<InterestDataList> arrayItems = new ArrayList<>();
        String serializedObject = sharedPreferences.getString(SharedPreferenceConstants.SAVED_INTEREST, null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<InterestDataList>>() {
            }.getType();
            arrayItems = gson.fromJson(serializedObject, type);
        }
        return arrayItems;
    }

    public <InterestDataList> void setSavedInterest(ArrayList<InterestDataList> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.SAVED_INTEREST, json);
        editor.apply();
    }

    public Boolean getAllowPersonalization() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.PERSONALIZATION, false);
    }

    public void setAllowPersonalization(boolean isPersonalisation) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.PERSONALIZATION, isPersonalisation);
        editor.apply();
    }

    public Boolean getEnableNotificationServer() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.SERVER_ENABLE_NOTIFICATION, false);
    }

    public void setEnableNotificationServer(boolean enableNotification) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.SERVER_ENABLE_NOTIFICATION, enableNotification);
        editor.apply();
    }

    public Boolean getEnableNotification() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.ENABLE_NOTIFICATION, false);
    }

    public void setEnableNotification(boolean enableNotification) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.ENABLE_NOTIFICATION, enableNotification);
        editor.apply();
    }

    public Boolean getSyncNow() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.SYNC_NOW, false);
    }

    public void setSyncNow(boolean isSyncNow) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.SYNC_NOW, isSyncNow);
        editor.apply();
    }

    public int getCurrentQuestion() {
        return sharedPreferences.getInt(SharedPreferenceConstants.CURRENT_QUESTION, 0);
    }

    public void setCurrentQuestion(int position) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPreferenceConstants.CURRENT_QUESTION, position);
        editor.apply();
    }

    public Boolean getOnBoardingQuestion() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.ONBOARDING_QUESTION, false);
    }

    public void setOnBoardingQuestion(boolean isOnBoardingQuestion) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.ONBOARDING_QUESTION, isOnBoardingQuestion);
        editor.apply();
    }

    public void clearOnboardingData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(SharedPreferenceConstants.USER_EMAIL).apply();
        editor.remove(SharedPreferenceConstants.USERNAME).apply();
        editor.remove(SharedPreferenceConstants.DISPLAY_NAME).apply();
        editor.remove(SharedPreferenceConstants.SELECTED_WELLNESS_FOCUS).apply();
        editor.remove(SharedPreferenceConstants.SELECTED_WELLNESS_FOCUS_TOPICS).apply();
        editor.remove(SharedPreferenceConstants.UNLOCK_POWER).apply();
        editor.remove(SharedPreferenceConstants.THIRD_FILLER).apply();
        editor.remove(SharedPreferenceConstants.SAVED_INTEREST).apply();
        editor.remove(SharedPreferenceConstants.INTEREST).apply();
        editor.remove(SharedPreferenceConstants.PERSONALIZATION).apply();
        editor.remove(SharedPreferenceConstants.SYNC_NOW).apply();
        editor.remove(SharedPreferenceConstants.CURRENT_QUESTION).apply();
        editor.remove(SharedPreferenceConstants.ONBOARDING_QUESTION).apply();
    }

    public Boolean getFirstTimeUserForAffirmation() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.FIRST_TIME_AFFIRMATION, true);
    }

    public void setFirstTimeUserForAffirmation(boolean isUnlock) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.FIRST_TIME_AFFIRMATION, isUnlock);
        editor.apply();
    }

    public Boolean getFirstTimeUserPlaylistAffirmation() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.FIRST_TIME_AFFIRMATION_PLAYLIST, true);
    }

    public void setFirstTimeUserPlaylistAffirmation(boolean isUnlock) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.FIRST_TIME_AFFIRMATION_PLAYLIST, isUnlock);
        editor.apply();
    }

    public Boolean getFirstTimeUserAffirmationInfoShown() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.FIRST_TIME_AFFIRMATION, true);
    }

    public void setFirstTimeForHomeDashboard(boolean isUnlock) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.FIRST_TIME_HOMEDASHBOARD, isUnlock);
        editor.apply();
    }

    public Boolean getFirstTimeForHomeDashboard() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.FIRST_TIME_HOMEDASHBOARD, true);
    }

    public void setFirstTimeCheckListEventLogged(boolean isUnlock) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.FIRST_TIME_CHECKLIST_EVENT, isUnlock);
        editor.apply();
    }

    public Boolean getFirstTimeCheckListEventLogged() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.FIRST_TIME_CHECKLIST_EVENT, true);
    }


    //FIRST_TIME_CHECKLIST_VISIT_EVENT

    public void setFirstTimeCheckListVisitLogged(boolean isUnlock) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.FIRST_TIME_CHECKLIST_VISIT_EVENT, isUnlock);
        editor.apply();
    }

    public Boolean getFirstTimeCheckListVisitLogged() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.FIRST_TIME_CHECKLIST_VISIT_EVENT, true);
    }


    public void setFirstTimeUserAffirmationInfoShown(boolean isUnlock) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.FIRST_TIME_AFFIRMATION, isUnlock);
        editor.apply();
    }

    public void saveTooltip(String prefKey, boolean isShowed) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(prefKey, isShowed);
        editor.apply();
    }

    public Boolean isTooltipShowed(String prefKey) {
        return sharedPreferences.getBoolean(prefKey, false);
    }

    public Boolean getFirstTimeUserForSnapMealVideo() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.FIRST_TIME_SNAP_MEAL_VIDEO, false);
    }

    public void setFirstTimeUserForSnapMealVideo(boolean isVideoUi) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.FIRST_TIME_SNAP_MEAL_VIDEO, isVideoUi);
        editor.apply();
    }

    public Boolean getFirstTimeUserForSnapMealRating() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.FIRST_TIME_SNAP_MEAL_RATING, false);
    }

    public void setFirstTimeUserForSnapMealRating(boolean isVideoUi) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.FIRST_TIME_SNAP_MEAL_RATING, isVideoUi);
        editor.apply();
    }

    public int getMaxCalories() {
        return sharedPreferences.getInt(SharedPreferenceConstants.EAT_RIGHT_MAX_CALORIES, 0);
    }

    public void setMaxCalories(int maxCalories) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPreferenceConstants.EAT_RIGHT_MAX_CALORIES, maxCalories);
        editor.apply();
    }

    public int getMaxCarbs() {
        return sharedPreferences.getInt(SharedPreferenceConstants.EAT_RIGHT_MAX_CARBS, 0);
    }

    public void setMaxCarbs(int maxCarbs) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPreferenceConstants.EAT_RIGHT_MAX_CARBS, maxCarbs);
        editor.apply();
    }

    public int getMaxProtein() {
        return sharedPreferences.getInt(SharedPreferenceConstants.EAT_RIGHT_MAX_PROTEIN, 0);
    }

    public void setMaxProtein(int maxProtein) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPreferenceConstants.EAT_RIGHT_MAX_PROTEIN, maxProtein);
        editor.apply();
    }

    public int getMaxFats() {
        return sharedPreferences.getInt(SharedPreferenceConstants.EAT_RIGHT_MAX_FATS, 0);
    }

    public void setMaxFats(int maxFats) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPreferenceConstants.EAT_RIGHT_MAX_FATS, maxFats);
        editor.apply();
    }

    public void saveMealCalenderTooltip(String prefKey, boolean isShowed) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(prefKey, isShowed);
        editor.apply();
    }

    public Boolean isMealCalenderTooltipShowed(String prefKey) {
        return sharedPreferences.getBoolean(prefKey, false);
    }

    // Method to save the user ID
    public void saveSnapMealId(String snapMealId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.SNAP_MEAL_ID, snapMealId);
        editor.apply();
    }

    // Method to retrieve the user ID
    public String getSnapMealId() {
        return sharedPreferences.getString(SharedPreferenceConstants.SNAP_MEAL_ID, "");
    }

    public Boolean getAIReportGeneratedView() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.AI_REPORT_GENERATED, false);
    }

    public void setAIReportGeneratedView(boolean isReportGenerated) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.AI_REPORT_GENERATED, isReportGenerated);
        editor.apply();
    }

    public Boolean getFirstTimeView(String name) {
        return sharedPreferences.getBoolean(name, true);
    }

    public void setFirstTimeView(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(name, false);
        editor.apply();
    }


    // Add to SharedPreferenceManager.java

    // Generic string save/get/remove methods
    public void saveString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key, String defaultValue) {
        String value = sharedPreferences.getString(key, defaultValue);
        return value != null ? value : defaultValue;
    }


    public void removeKey(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    // Mobile number getter (add setter too if not present)
    public String getMobile() {
        return sharedPreferences.getString(SharedPreferenceConstants.USER_MOBILE, "");
    }

    public void setMobile(String mobile) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.USER_MOBILE, mobile);
        editor.apply();
    }

    // ---------- Force Update (Config API) ----------

    public void saveForceUpdateConfig(boolean enabled, String minVersion, String updateUrl, String message) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.FORCE_UPDATE_ENABLED, enabled);
        editor.putString(SharedPreferenceConstants.FORCE_UPDATE_MIN_VERSION, minVersion != null ? minVersion : "");
        editor.putString(SharedPreferenceConstants.FORCE_UPDATE_URL, updateUrl != null ? updateUrl : "");
        editor.putString(SharedPreferenceConstants.FORCE_UPDATE_MESSAGE, message != null ? message : "");
        editor.apply();
    }

    public boolean isForceUpdateEnabled() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.FORCE_UPDATE_ENABLED, false);
    }

    public String getForceUpdateMinVersion() {
        return getString(SharedPreferenceConstants.FORCE_UPDATE_MIN_VERSION, "");
    }

    public String getForceUpdateUrl() {
        return getString(SharedPreferenceConstants.FORCE_UPDATE_URL, "");
    }

    public String getForceUpdateMessage() {
        return getString(SharedPreferenceConstants.FORCE_UPDATE_MESSAGE, "");
    }

    public void clearForceUpdateConfig() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(SharedPreferenceConstants.FORCE_UPDATE_ENABLED);
        editor.remove(SharedPreferenceConstants.FORCE_UPDATE_MIN_VERSION);
        editor.remove(SharedPreferenceConstants.FORCE_UPDATE_URL);
        editor.remove(SharedPreferenceConstants.FORCE_UPDATE_MESSAGE);
        editor.apply();
    }

    public void saveAppConfigJson(@NonNull String json) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.APP_CONFIG_RESPONSE, json);
        editor.apply();
    }

    public String getAppConfigJson() {
        return sharedPreferences.getString(SharedPreferenceConstants.APP_CONFIG_RESPONSE, "");
    }

    public void setHomeFirstVisited(boolean visit) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.FIRST_HOME_VISIT, visit);
        editor.apply(); // Apply changes asynchronously
    }

    // Method to retrieve the access token
    public boolean isHomeFirstVisited() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.FIRST_HOME_VISIT, false);
    }

    public void setChallengeState(int state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPreferenceConstants.CHALLENGE_STATUS, state);
        editor.apply(); // Apply changes asynchronously
    }

    // Method to retrieve the access token
    public int getChallengeState() {
        return sharedPreferences.getInt(SharedPreferenceConstants.CHALLENGE_STATUS, 1);
    }

    public void setChallengeEndDate(String state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.CHALLENGE_END_DATE, state);
        editor.apply(); // Apply changes asynchronously
    }

    // Method to retrieve the access token
    public String getChallengeEndDate() {
        return sharedPreferences.getString(SharedPreferenceConstants.CHALLENGE_END_DATE, "28 Feb 2026, 09:00 PM");
    }

    public void setChallengeStartDate(String state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.CHALLENGE_START_DATE, state);
        editor.apply(); // Apply changes asynchronously
    }

    // Method to retrieve the access token
    public String getChallengeStartDate() {
        return sharedPreferences.getString(SharedPreferenceConstants.CHALLENGE_START_DATE, "01 Feb 2026, 09:00 AM");
    }

    public void setChallengeParticipatedDate(String state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.CHALLENGE_PARTICIPATED_DATE, state);
        editor.apply(); // Apply changes asynchronously
    }

    // Method to retrieve the access token
    public String getChallengeParticipatedDate() {
        return sharedPreferences.getString(SharedPreferenceConstants.CHALLENGE_PARTICIPATED_DATE, "");
    }

    public Boolean isNewUser() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.NEW_USER, true);
    }

    public void setNewUser(boolean isNewUser) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.NEW_USER, isNewUser);
        editor.apply();
    }

    public int getHealthPermissionDenialCount() {
        return sharedPreferences.getInt(SharedPreferenceConstants.SYNC_PERMISSION_COUNT, 0);
    }

    public void setHealthPermissionDenialCount(int count) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPreferenceConstants.SYNC_PERMISSION_COUNT, count);
        editor.apply();
    }
}

