package com.nayya.myktor.ui.login.logoutaccount

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nayya.myktor.R
import com.nayya.myktor.databinding.BottomSheetConfirmActionBinding
import com.nayya.myktor.ui.animator.BottomSheetAnimator

class ConfirmActionBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetConfirmActionBinding? = null
    private val binding get() = _binding!!

    private var title: String? = null
    private var subtitle: String? = null
    private var primaryText: String? = null
    private var cancelText: String? = null

    private val viewModel: ConfirmActionViewModel by viewModels {
        ConfirmViewModelFactory(DefaultConfirmRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.getString(ARG_TITLE)
        subtitle = arguments?.getString(ARG_SUBTITLE)
        primaryText = arguments?.getString(ARG_PRIMARY_TEXT)
        cancelText = arguments?.getString(ARG_CANCEL_TEXT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        _binding = BottomSheetConfirmActionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val actionType = arguments?.getSerializable(ARG_ACTION_TYPE) as? ConfirmActionType ?: return
        observeViewModel()

        when (actionType) {
            ConfirmActionType.LOGOUT, ConfirmActionType.LOGOUT_ALL -> {
                binding.tvTitle.text = "Внимание!"
                binding.tvSubtitle.text = "Подтверждение ВЫХОДА из личного кабинета"
                binding.btnPrimary.text = primaryText ?: "Выход с данного устройства"
                binding.btnSecondary.text = "Выход со всех устройств"
                binding.btnCancel.text = cancelText ?: "Отменить"

                binding.btnPrimary.setOnClickListener {
                    viewModel.logoutCurrentDevice()
                }

                binding.btnSecondary.setOnClickListener {
                    viewModel.logoutAllDevices()
                }
            }

            ConfirmActionType.DELETE_ACCOUNT -> {
                binding.tvTitle.text =
                    "Внимание! Удаление аккаунта."
                binding.tvSubtitle.text = "Если вы подтверждаете удаление аккаунта, то\nВаш " +
                        "аккаунт будет заблокирован на 30 дней с последующим удалением.\nЕсли Вы " +
                        "решите восстановить аккаунт в течении указанных 30 дней, то обратитесь в поддержку"
                binding.btnPrimary.text = primaryText ?: "Удалить"
                binding.btnSecondary.visibility = View.GONE
                binding.btnCancel.text = cancelText ?: "Отменить"

                binding.btnPrimary.setOnClickListener {
                    viewModel.deleteAccount()
                }
            }

            ConfirmActionType.DELETE_ADDRESS -> {
                binding.tvTitle.text = "Внимание! Удаление адреса?"
                binding.tvSubtitle.text = subtitle ?: "Вы уверены?"
                binding.btnPrimary.text = primaryText ?: "Удалить"
                binding.btnSecondary.visibility = View.GONE
                binding.btnCancel.text = cancelText ?: "Отменить"

                binding.btnPrimary.setOnClickListener {
                    // Вызвать удаление через callback/VM
                    (parentFragment as? ConfirmActionCallback)?.onConfirmDeleteAddress()
                    dismiss()
                }
            }
        }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnClose.setOnClickListener { dismiss() }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme_Compat)

        BottomSheetAnimator.applyEnterExitAnimations(dialog)

        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // Оповести родителя о закрытии (если нужно)
        (parentFragment as? OnConfirmSheetClosedListener)?.onConfirmSheetClosed()
    }

    private fun observeViewModel() {
        viewModel.actionCompleted.observe(viewLifecycleOwner) {
            requireParentFragment().parentFragmentManager.setFragmentResult(
                "counterparty_updated",
                Bundle()
            )
            dismiss()

            requireActivity().onBackPressedDispatcher.onBackPressed() // ← вернуться назад
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface ConfirmActionCallback {
        fun onConfirmDeleteAddress()
    }


    interface OnConfirmSheetClosedListener {
        fun onConfirmSheetClosed()
    }

    companion object {
        private const val ARG_ACTION_TYPE = "action_type"
        private const val ARG_TITLE = "title"
        private const val ARG_SUBTITLE = "subtitle"
        private const val ARG_PRIMARY_TEXT = "primary"
        private const val ARG_CANCEL_TEXT = "cancel"

        fun newInstance(
            actionType: ConfirmActionType,
            title: String? = null,
            subtitle: String? = null,
            primaryText: String? = null,
            cancelText: String? = null
        ): ConfirmActionBottomSheet {
            return ConfirmActionBottomSheet().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ACTION_TYPE, actionType)
                    putString(ARG_TITLE, title)
                    putString(ARG_SUBTITLE, subtitle)
                    putString(ARG_PRIMARY_TEXT, primaryText)
                    putString(ARG_CANCEL_TEXT, cancelText)
                }
            }
        }
    }
}