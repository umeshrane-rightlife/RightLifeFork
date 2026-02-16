package com.jetsynthesys.rightlife.ui

import android.content.Context
import android.content.Intent
import com.jetsynthesys.rightlife.ai_package.ui.MainAIActivity
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.ui.NewSleepSounds.NewSleepSoundActivity
import com.jetsynthesys.rightlife.ui.affirmation.PractiseAffirmationPlaylistActivity
import com.jetsynthesys.rightlife.ui.affirmation.TodaysAffirmationActivity
import com.jetsynthesys.rightlife.ui.breathwork.BreathworkActivity
import com.jetsynthesys.rightlife.ui.context_screens.AffirmationContextScreenActivity
import com.jetsynthesys.rightlife.ui.context_screens.AllInOnePlaceContextScreenActivity
import com.jetsynthesys.rightlife.ui.context_screens.BreathWorkContextScreenActivity
import com.jetsynthesys.rightlife.ui.context_screens.FaceScanContextScreenActivity
import com.jetsynthesys.rightlife.ui.context_screens.JournalContextScreenActivity
import com.jetsynthesys.rightlife.ui.context_screens.MRERAssessmentContextScreenActivity
import com.jetsynthesys.rightlife.ui.context_screens.MealScanContextScreenActivity
import com.jetsynthesys.rightlife.ui.context_screens.MindAuditContextScreenActivity
import com.jetsynthesys.rightlife.ui.context_screens.SleepSoundsContextScreenActivity
import com.jetsynthesys.rightlife.ui.context_screens.TRSRAssessmentContextScreenActivity
import com.jetsynthesys.rightlife.ui.context_screens.WelcomeEatRightContextScreenActivity
import com.jetsynthesys.rightlife.ui.context_screens.WelcomeMoveRightContextScreenActivity
import com.jetsynthesys.rightlife.ui.context_screens.WelcomeSleepRightContextScreenActivity
import com.jetsynthesys.rightlife.ui.context_screens.WelcomeThinkRightContextScreenActivity
import com.jetsynthesys.rightlife.ui.healthcam.HealthCamActivity
import com.jetsynthesys.rightlife.ui.healthcam.basicdetails.HealthCamBasicDetailsNewActivity
import com.jetsynthesys.rightlife.ui.jounal.new_journal.JournalListActivity
import com.jetsynthesys.rightlife.ui.jounal.new_journal.JournalNewActivity
import com.jetsynthesys.rightlife.ui.mindaudit.MindAuditFromActivity
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireEatRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireThinkRightActivity
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceConstants
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager

object ActivityUtils {

    fun startMindAuditActivity(context: Context, isFromThinkRight: Boolean = false) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
        if (sharedPreferenceManager.getFirstTimeView(SharedPreferenceConstants.MIND_AUDIT_CONTEXT_SCREEN)) {
            sharedPreferenceManager.setFirstTimeView(SharedPreferenceConstants.MIND_AUDIT_CONTEXT_SCREEN)
            context.startActivity(
                Intent(
                    context,
                    MindAuditContextScreenActivity::class.java
                ).apply {
                    putExtra("FROM_THINK_RIGHT", isFromThinkRight)
                }
            )
        } else {
            context.startActivity(Intent(context, MindAuditFromActivity::class.java).apply {
                putExtra("FROM_THINK_RIGHT", isFromThinkRight)
            })
        }
    }

    fun startChecklistCompleteActivity(context: Context) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
        if (sharedPreferenceManager.getFirstTimeView(
                SharedPreferenceConstants.ALL_IN_ONE_PLACE
            )
        ) {
            sharedPreferenceManager.setFirstTimeView(
                SharedPreferenceConstants.ALL_IN_ONE_PLACE
            )
            context.startActivity(
                Intent(
                    context,
                    AllInOnePlaceContextScreenActivity::class.java
                )
            )
        }
    }

    fun startTodaysAffirmationActivity(context: Context) {
        val sharedPrefManager = SharedPreferenceManager.getInstance(context)

        if (sharedPrefManager.getFirstTimeView(SharedPreferenceConstants.AFFIRMATION_CONTEXT_SCREEN)) {
            sharedPrefManager.setFirstTimeView(SharedPreferenceConstants.AFFIRMATION_CONTEXT_SCREEN)
            context.startActivity(
                Intent(context, AffirmationContextScreenActivity::class.java)
            )
        } else {
            context.startActivity(
                Intent(context, PractiseAffirmationPlaylistActivity::class.java)
            )
        }
    }

    fun startEatRightQuestionnaireActivity(context: Context) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        if (sharedPreferenceManager.getFirstTimeView(SharedPreferenceConstants.MRER_CONTEXT_SCREEN)) {
            sharedPreferenceManager.setFirstTimeView(SharedPreferenceConstants.MRER_CONTEXT_SCREEN)
            context.startActivity(
                Intent(
                    context,
                    MRERAssessmentContextScreenActivity::class.java
                )
            )
        } else
            context.startActivity(Intent(context, QuestionnaireEatRightActivity::class.java))
    }

    fun startThinkRightQuestionnaireActivity(context: Context) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        if (sharedPreferenceManager.getFirstTimeView(SharedPreferenceConstants.TRSR_CONTEXT_SCREEN)) {
            sharedPreferenceManager.setFirstTimeView(SharedPreferenceConstants.TRSR_CONTEXT_SCREEN)
            context.startActivity(
                Intent(
                    context,
                    TRSRAssessmentContextScreenActivity::class.java
                )
            )
        } else
            context.startActivity(Intent(context, QuestionnaireThinkRightActivity::class.java))
    }

    fun startSleepSoundActivity(context: Context) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        if (sharedPreferenceManager.getFirstTimeView(SharedPreferenceConstants.SLEEP_SOUND_CONTEXT_SCREEN)) {
            sharedPreferenceManager.setFirstTimeView(SharedPreferenceConstants.SLEEP_SOUND_CONTEXT_SCREEN)
            context.startActivity(
                Intent(
                    context,
                    SleepSoundsContextScreenActivity::class.java
                )
            )
        } else
            context.startActivity(Intent(context, NewSleepSoundActivity::class.java))
    }

    fun startBreathWorkActivity(context: Context) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        if (sharedPreferenceManager.getFirstTimeView(SharedPreferenceConstants.BREATH_WORK_CONTEXT_SCREEN)) {
            sharedPreferenceManager.setFirstTimeView(SharedPreferenceConstants.BREATH_WORK_CONTEXT_SCREEN)
            context.startActivity(
                Intent(
                    context,
                    BreathWorkContextScreenActivity::class.java
                )
            )
        } else
            context.startActivity(Intent(context, BreathworkActivity::class.java))
    }

    fun startJournalListActivity(context: Context, isFromThinkRight: Boolean = false) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        if (sharedPreferenceManager.getFirstTimeView(SharedPreferenceConstants.JOURNAL_CONTEXT_SCREEN)) {
            sharedPreferenceManager.setFirstTimeView(SharedPreferenceConstants.JOURNAL_CONTEXT_SCREEN)
            context.startActivity(
                Intent(
                    context,
                    JournalContextScreenActivity::class.java
                ).apply { putExtra("FROM_THINK_RIGHT", isFromThinkRight) }
            )
        } else
            context.startActivity(Intent(context, JournalListActivity::class.java).apply {
                putExtra(
                    "FROM_THINK_RIGHT",
                    isFromThinkRight
                )
            })
    }

    fun startJournalNewActivity(context: Context, isFromThinkRight: Boolean = false) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        if (sharedPreferenceManager.getFirstTimeView(SharedPreferenceConstants.JOURNAL_CONTEXT_SCREEN)) {
            sharedPreferenceManager.setFirstTimeView(SharedPreferenceConstants.JOURNAL_CONTEXT_SCREEN)
            context.startActivity(
                Intent(
                    context,
                    JournalContextScreenActivity::class.java
                ).apply {
                    putExtra("IS_CREATE_JOURNAL", true)
                    putExtra("FROM_THINK_RIGHT", isFromThinkRight)
                }
            )
        } else
            context.startActivity(Intent(context, JournalNewActivity::class.java).apply {
                putExtra("FROM_THINK_RIGHT", isFromThinkRight)
            })
    }

    fun startFaceScanActivity(context: Context) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        if (sharedPreferenceManager.getFirstTimeView(SharedPreferenceConstants.FACE_SCAN_CONTEXT_SCREEN)) {
            sharedPreferenceManager.setFirstTimeView(SharedPreferenceConstants.FACE_SCAN_CONTEXT_SCREEN)
            context.startActivity(Intent(context, FaceScanContextScreenActivity::class.java))
        } else
            context.startActivity(Intent(context, HealthCamBasicDetailsNewActivity::class.java))
    }

    fun startMealScanActivity(context: Context, snapMealId: String = "") {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        if (sharedPreferenceManager.getFirstTimeView(SharedPreferenceConstants.MEAL_SCAN_CONTEXT_SCREEN)) {
            sharedPreferenceManager.setFirstTimeView(SharedPreferenceConstants.MEAL_SCAN_CONTEXT_SCREEN)
            context.startActivity(Intent(context, MealScanContextScreenActivity::class.java))
        } else
            context.startActivity(Intent(context, MainAIActivity::class.java).apply {
                putExtra("ModuleName", "EatRight")
                putExtra("BottomSeatName", "SnapMealTypeEat")
                putExtra("snapMealId", snapMealId)
            })
    }

    fun startThinkRightReportsActivity(
        context: Context, bottomSheetName: String, isFromAIDashBoard: Boolean = false
    ) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        if (sharedPreferenceManager.getFirstTimeView(SharedPreferenceConstants.THINK_RIGHT_CONTEXT_SCREEN)) {
            sharedPreferenceManager.setFirstTimeView(SharedPreferenceConstants.THINK_RIGHT_CONTEXT_SCREEN)
            context.startActivity(
                Intent(
                    context,
                    WelcomeThinkRightContextScreenActivity::class.java
                ).apply {
                    putExtra("ModuleName", "ThinkRight")
                    putExtra("BottomSeatName", bottomSheetName)
                    putExtra("isFromAIDashBoard", isFromAIDashBoard)
                }
            )
        } else if (!isFromAIDashBoard)
            context.startActivity(Intent(context, MainAIActivity::class.java).apply {
                putExtra("ModuleName", "ThinkRight")
                putExtra("BottomSeatName", bottomSheetName)
            })
    }

    fun startSleepRightReportsActivity(
        context: Context,
        bottomSheetName: String,
        isFromAIDashBoard: Boolean = false
    ) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        if (sharedPreferenceManager.getFirstTimeView(SharedPreferenceConstants.SLEEP_RIGHT_CONTEXT_SCREEN)) {
            sharedPreferenceManager.setFirstTimeView(SharedPreferenceConstants.SLEEP_RIGHT_CONTEXT_SCREEN)
            context.startActivity(
                Intent(
                    context,
                    WelcomeSleepRightContextScreenActivity::class.java
                ).apply {
                    putExtra("ModuleName", "SleepRight")
                    putExtra("BottomSeatName", bottomSheetName)
                    putExtra("isFromAIDashBoard", isFromAIDashBoard)
                }
            )
        } else if (!isFromAIDashBoard)
            context.startActivity(Intent(context, MainAIActivity::class.java).apply {
                putExtra("ModuleName", "SleepRight")
                putExtra("BottomSeatName", bottomSheetName)
            })
    }

    fun startMoveRightReportsActivity(
        context: Context,
        bottomSheetName: String,
        isFromAIDashBoard: Boolean = false
    ) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        if (sharedPreferenceManager.getFirstTimeView(SharedPreferenceConstants.MOVE_RIGHT_CONTEXT_SCREEN)) {
            sharedPreferenceManager.setFirstTimeView(SharedPreferenceConstants.MOVE_RIGHT_CONTEXT_SCREEN)
            context.startActivity(
                Intent(
                    context,
                    WelcomeMoveRightContextScreenActivity::class.java
                ).apply {
                    putExtra("ModuleName", "MoveRight")
                    putExtra("BottomSeatName", bottomSheetName)
                    putExtra("isFromAIDashBoard", isFromAIDashBoard)
                }
            )
        } else if (!isFromAIDashBoard)
            context.startActivity(Intent(context, MainAIActivity::class.java).apply {
                putExtra("ModuleName", "MoveRight")
                putExtra("BottomSeatName", bottomSheetName)
            })
    }

    fun startEatRightReportsActivity(
        context: Context,
        bottomSheetName: String,
        snapMealId: String = "",
        isFromAIDashBoard: Boolean = false
    ) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        if (sharedPreferenceManager.getFirstTimeView(SharedPreferenceConstants.EAT_RIGHT_CONTEXT_SCREEN)) {
            sharedPreferenceManager.setFirstTimeView(SharedPreferenceConstants.EAT_RIGHT_CONTEXT_SCREEN)
            context.startActivity(
                Intent(
                    context,
                    WelcomeEatRightContextScreenActivity::class.java
                ).apply {
                    putExtra("ModuleName", "EatRight")
                    putExtra("BottomSeatName", bottomSheetName)
                    putExtra("snapMealId", snapMealId)
                    putExtra("isFromAIDashBoard", isFromAIDashBoard)
                }
            )
        } else if (!isFromAIDashBoard)
            context.startActivity(Intent(context, MainAIActivity::class.java).apply {
                putExtra("ModuleName", "EatRight")
                putExtra("BottomSeatName", bottomSheetName)
                putExtra("snapMealId", snapMealId)
            })
    }

    fun startRightLifeContextScreenActivity(context: Context) {
        /*val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        if (sharedPreferenceManager.getFirstTimeView(SharedPreferenceConstants.RIGHT_LIFE_CONTEXT_SCREEN)) {
            sharedPreferenceManager.setFirstTimeView(SharedPreferenceConstants.RIGHT_LIFE_CONTEXT_SCREEN)
            context.startActivity(
                Intent(
                    context,
                    WelcomeRightLifeContextScreenActivity::class.java
                )
            )
        } else*/
        context.startActivity(Intent(context, HomeNewActivity::class.java))
    }

}
