package com.jetsynthesys.rightlife.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.DialogChecklistQuestionsBinding
import com.jetsynthesys.rightlife.databinding.DialogChecklistWhyMattersBinding
import com.jetsynthesys.rightlife.databinding.DialogJournalCommonBinding
import com.jetsynthesys.rightlife.databinding.DialogPlaylistCreatedBinding
import com.jetsynthesys.rightlife.databinding.DialogSwitchAccountBinding
import com.jetsynthesys.rightlife.databinding.DisclaimerCommonDialogBinding
import com.jetsynthesys.rightlife.databinding.FreeTrialRelatedBottomsheetBinding
import com.jetsynthesys.rightlife.databinding.LayoutExitDialogMindBottomsheetBinding

object DialogUtils {

    fun showJournalCommonDialog(context: Context, header: String, htmlText: String) {
        val bottomSheetDialog = BottomSheetDialog(context)
        // Inflate the BottomSheet layout
        val binding = DialogJournalCommonBinding.inflate(LayoutInflater.from(context))
        val bottomSheetView = binding.root
        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(context, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setCanceledOnTouchOutside(false)

        // ✅ Set dim background manually for safety
        bottomSheetDialog.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.dimAmount = 0.7f // 0.0 to 1.0
            window.attributes = layoutParams
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        binding.tvTitle.text = header
        binding.tvDescription.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)

        // Handle close button click
        binding.btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        binding.btnOK.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    fun showCheckListQuestionCommonDialog(
        context: Context,
        header: String = "Finish Checklist to Unlock",
        onLetsDoItClick: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        val binding = DialogChecklistQuestionsBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window = dialog.window
        // Set the dim amount
        val layoutParams = window!!.attributes
        layoutParams.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window.attributes = layoutParams

        binding.titleText.text = header

        // Handle close button click
        binding.btnClose.setOnClickListener {
            dialog.dismiss()
            onLetsDoItClick?.invoke()
        }

        dialog.show()
    }

    fun showWhyChecklistMattersDialog(
        context: Context,
        header: String = "Here’s Why It Matters"
    ) {
        val dialog = Dialog(context)
        val binding = DialogChecklistWhyMattersBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window = dialog.window
        // Set the dim amount
        val layoutParams = window!!.attributes
        layoutParams.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window.attributes = layoutParams

        binding.titleText.text = header

        // Handle close button click
        binding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        binding.ivClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showSuccessDialog(
        activity: Activity,
        message: String,
        desc: String = ""
    ) {
        val bottomSheetDialog = BottomSheetDialog(activity)
        // Inflate the BottomSheet layout
        val binding = DialogPlaylistCreatedBinding.inflate(LayoutInflater.from(activity))
        val bottomSheetView = binding.root
        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(activity, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setCanceledOnTouchOutside(false)

        // ✅ Set dim background manually for safety
        bottomSheetDialog.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.dimAmount = 0.7f // 0.0 to 1.0
            window.attributes = layoutParams
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        // Set message using view binding
        binding.tvDialogPlaylistCreated.text = message

        if (desc.isNotEmpty()) {
            binding.tvDialogDescription.visibility = View.VISIBLE
            binding.tvDialogDescription.text = desc
        }

        bottomSheetDialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetDialog.dismiss()
            activity.finish()
        }, 2000)
    }

    fun showCommonBottomSheetDialog(
        context: Context,
        description: String,
        header: String = "Disclaimer",
        btnText: String = "Okay",
        onOkayClick: (() -> Unit)? = null,
        onCloseClick: (() -> Unit)? = null,
        btnColor: Int = R.color.menuselected,
        btnTextColor: Int = R.color.white
    ) {
        val bottomSheetDialog = BottomSheetDialog(context)
        // Inflate the BottomSheet layout
        val binding = DisclaimerCommonDialogBinding.inflate(LayoutInflater.from(context))
        val bottomSheetView = binding.root
        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(context, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setCanceledOnTouchOutside(false)

        // ✅ Set dim background manually for safety
        bottomSheetDialog.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.dimAmount = 0.7f // 0.0 to 1.0
            window.attributes = layoutParams
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        binding.tvTitle.text = header
        binding.tvDescription.text = description
        binding.btnOkay.text = btnText
        binding.btnOkay.apply {
            setTextColor(ContextCompat.getColor(context, btnTextColor))
            backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, btnColor)
            )
        }

        binding.btnOkay.setOnClickListener {
            bottomSheetDialog.dismiss()
            onOkayClick?.invoke()
        }

        binding.btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
            onCloseClick?.invoke()
        }

        bottomSheetDialog.show()
    }

    fun showFreeTrailRelatedBottomSheet(
        context: Context,
        description: String,
        header: String,
        btnOkayText: String = "Okay",
        isExplorePlanBtnVisible: Boolean = false,
        mainImage: Int,
        leftImage: Int,
        onOkayClick: (() -> Unit)? = null,
        onExploreClick: (() -> Unit)? = null
    ) {
        val bottomSheetDialog = BottomSheetDialog(context)
        // Inflate the BottomSheet layout
        val binding = FreeTrialRelatedBottomsheetBinding.inflate(LayoutInflater.from(context))
        val bottomSheetView = binding.root
        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(context, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setCanceledOnTouchOutside(false)

        // ✅ Set dim background manually for safety
        bottomSheetDialog.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.dimAmount = 0.7f // 0.0 to 1.0
            window.attributes = layoutParams
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        binding.tvTitle.text = header
        binding.tvDescription.text = description
        binding.btnOkay.text = btnOkayText
        binding.leftImage.setImageResource(leftImage)
        binding.mainImage.setImageResource(mainImage)

        binding.btnExplorePlans.visibility =
            if (isExplorePlanBtnVisible) View.VISIBLE else View.GONE

        binding.btnExplorePlans.setOnClickListener {
            bottomSheetDialog.dismiss()
            onExploreClick?.invoke()
        }

        binding.btnOkay.setOnClickListener {
            bottomSheetDialog.dismiss()
            onOkayClick?.invoke()
        }

        binding.btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    fun showExitDialog(
        context: Context,
        onQuitClick: (() -> Unit)? = null,
    ) {
        val bottomSheetDialog = BottomSheetDialog(context)
        // Inflate layout using View Binding
        val binding: LayoutExitDialogMindBottomsheetBinding =
            LayoutExitDialogMindBottomsheetBinding.inflate(
                LayoutInflater.from(
                    context
                )
            )

        val bottomSheetView = binding.root
        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(context, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setCanceledOnTouchOutside(false)

        // ✅ Set dim background manually for safety
        bottomSheetDialog.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.dimAmount = 0.7f // 0.0 to 1.0
            window.attributes = layoutParams
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        // Set button click listeners
        binding.dialogButtonStay.setOnClickListener { v ->
            bottomSheetDialog.dismiss()
        }

        binding.dialogButtonExit.setOnClickListener { v ->
            bottomSheetDialog.dismiss()
            onQuitClick?.invoke()
        }

        // Show dialog
        bottomSheetDialog.show()
    }

    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String = "OK",
        negativeButtonText: String = "Cancel",
        onPositiveClick: () -> Unit
    ) {
        // Using MaterialAlertDialogBuilder for a modern, rounded look
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false) // Ensures the user makes a choice
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                onPositiveClick()
                dialog.dismiss()
            }
            .setNegativeButton(negativeButtonText) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


}