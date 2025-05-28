package com.nayya.myktor.ui.animator

import android.app.Dialog
import android.content.Context
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nayya.myktor.R

object BottomSheetAnimator {

    fun applyEnterExitAnimations(dialog: Dialog) {
        dialog.window?.setWindowAnimations(R.style.BottomSheetDialogThemeAnimation)
    }

    fun expandFully(bottomSheetDialog: BottomSheetDialog, offsetDp: Int = 0) {
        val bottomSheet =
            bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isFitToContents = false
                expandedOffset = dpToPx(offsetDp, it.context)
                peekHeight = 0
            }
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            it.requestLayout()
        }
    }

    private fun dpToPx(dp: Int, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
