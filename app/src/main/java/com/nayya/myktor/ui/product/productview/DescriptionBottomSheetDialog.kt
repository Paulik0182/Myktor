package com.nayya.myktor.ui.product.productview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nayya.myktor.R

class DescriptionBottomSheetDialog : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme_Compat)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val textView = TextView(requireContext()).apply {
            setPadding(16, 16, 16, 16)
            setText(arguments?.getString(ARG_DESC)?.take(1500) ?: "")
            setTextColor(ContextCompat.getColor(context, R.color.grey_text))
        }
        return textView
    }

    companion object {
        private const val ARG_DESC = "desc"

        fun newInstance(description: String): DescriptionBottomSheetDialog {
            return DescriptionBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_DESC, description)
                }
            }
        }
    }
}
