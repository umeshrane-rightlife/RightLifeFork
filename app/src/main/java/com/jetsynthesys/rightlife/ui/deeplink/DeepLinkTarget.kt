package com.jetsynthesys.rightlife.ui.deeplink

enum class DeepLinkTarget(val slug: String) {
    HOME("home"),
    MY_HEALTH("my-health"),
    MEAL_LOG("meal_log"),
    PROFILE("profile"),
    CATEGORY_LIST("categorylist"),
    AI_REPORT("ai-report"),
    MIND_AUDIT("mind-audit"),

    // Explore Section
    THINK_EXPLORE("thinkright-explore"),
    EAT_EXPLORE("eatright-explore"),
    SLEEP_EXPLORE("sleepright-explore"),
    MOVE_EXPLORE("moveright-explore"),
    MOVE_HOME("moveright-home"),
    WORKOUT_LOG_DEEP("workoutlog"),
    EAT_HOME("eatright-home"),
    SLEEP_HOME("sleepright-home"),
    WEIGHT_LOG_DEEP("weight-log"),
    WATER_LOG_DEEP("water-log"),
    SLEEP_LOG_DEEP("sleep-log"),
    THINK_HOME("thinkright-home"),

    // Quick Links
    FACE_SCAN("face-scan"),
    SNAP_MEAL("snap-meal"),
    SLEEP_SOUND("sleep-sound"),
    SLEEP_SOUND_PLAYLIST("sleep-sound/playlist"),
    AFFIRMATION("affirmation"),
    AFFIRMATION_PLAYLIST("affirmationPlaylist"),
    JOURNAL("journal"),
    BREATHING("breathing"),
    BREATHING_ALTERNATE("breathing-alternate"),
    BREATHING_BOX("breathing-boxbreathing"),
    BREATHING_CUSTOM("breathing-custom"),
    BREATHING_4_7_8("breathing-4-7-8"),

    // Logs
    ACTIVITY_LOG("activity-log"),
    WEIGHT_LOG("weight-log"),
    WATER_LOG("water-log"),
    SLEEP_LOG("sleep-log"),
    FOOD_LOG("food-log"),

    // Content
    JUMPBACK("jumpback"),
    SAVED_ITEMS("saveditems"),
    ARTICLE("article"),
    AUDIO("audio"),
    VIDEO("video"),

    // Challenges
    CHALLENGE_HOME("challenge-home"),
    CHALLENGE_LEADERBOARD("challenge-leaderboard"),

    // Plans
    SUBSCRIPTION_PLAN("plans/SUBSCRIPTION_PLAN"),
    BOOSTER_PLAN("plans/BOOSTER_PLAN"),

    // Mind Audit Info
    MIND_AUDIT_PHQ9("mind-audit/phq9Info"),
    MIND_AUDIT_GAD7("mind-audit/GAD7"),
    MIND_AUDIT_OHQ("mind-audit/ohq"),
    MIND_AUDIT_CAS("mind-audit/cas"),
    MIND_AUDIT_DASS21("mind-audit/dass21");

    companion object {
        // Keys for Intent Extras (keep these as constants)
        const val EXTRA_DEEP_LINK_TARGET = "EXTRA_DEEP_LINK_TARGET"
        const val EXTRA_DEEP_LINK_DETAIL_ID = "EXTRA_DEEP_LINK_DETAIL_ID"

        // Helper function to find Enum by string slug (useful when parsing URLs)
        fun fromSlug(slug: String?): DeepLinkTarget? {
            return entries.find { it.slug == slug }
        }
    }
}