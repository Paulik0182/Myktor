package com.nayya.uicomponents

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.nayya.uicomponents.databinding.LayoutCardActionViewBinding

class CustomCardActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutCardActionViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomCardActionView,
            0,
            0
        ).apply {
            try {
                title = getString(R.styleable.CustomCardActionView_titleText)
                description = getString(R.styleable.CustomCardActionView_descriptionText)
                info = getString(R.styleable.CustomCardActionView_infoText)

                leftIconRes = getResourceId(R.styleable.CustomCardActionView_leftIconRes, 0)
                editIconRes = getResourceId(R.styleable.CustomCardActionView_editIconRes, 0)

                showInfoIcon = getBoolean(R.styleable.CustomCardActionView_showInfoIcon, true)
                showInfoText = getBoolean(R.styleable.CustomCardActionView_showInfoText, true)
                showDescriptionIcon = getBoolean(R.styleable.CustomCardActionView_showDescriptionIcon, true)
                showDescriptionText = getBoolean(R.styleable.CustomCardActionView_showDescriptionText, true)

            } finally {
                recycle()
            }
        }
    }

    var title: String? = null
        set(value) {
            field = value
            binding.tvActionTitle.text = value
        }

    var description: String? = null
        set(value) {
            field = value
            binding.tvDescription.text = value
            binding.tvDescription.isVisible = !value.isNullOrEmpty()
        }

    var info: String? = null
        set(value) {
            field = value
            binding.tvInformation.text = value
            binding.tvInformation.isVisible = !value.isNullOrEmpty()
        }

    var leftIconRes: Int = 0
        set(value) {
            field = value
            if (value != 0) {
                binding.ivActionIcon.setImageResource(value)
            }
        }

    var editIconRes: Int = 0
        set(value) {
            field = value
            if (value != 0) {
                binding.btnEdit.setImageResource(value)
            }
        }


    var showInfoIcon: Boolean = true
        set(value) {
            field = value
            binding.ivInformationIcon.isVisible = value
        }

    var showInfoText: Boolean = true
        set(value) {
            field = value
            binding.tvInformation.isVisible = value
        }

    var showDescriptionIcon: Boolean = true
        set(value) {
            field = value
            binding.ivDescriptionIcon.isVisible = value
        }

    var showDescriptionText: Boolean = true
        set(value) {
            field = value
            binding.tvDescription.isVisible = value
        }

    fun setEditClickListener(listener: () -> Unit) {
        binding.btnEdit.setOnClickListener { listener.invoke() }
    }

    fun show() {
        binding.cardActionContainer.isVisible = true
    }

    fun hide() {
        binding.cardActionContainer.isVisible = false
    }
}
