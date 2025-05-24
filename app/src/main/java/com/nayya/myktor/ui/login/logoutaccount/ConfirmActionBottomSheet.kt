package com.nayya.myktor.ui.login.logoutaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nayya.myktor.databinding.BottomSheetConfirmActionBinding

class ConfirmActionBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetConfirmActionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConfirmActionViewModel by viewModels {
        LogoutViewModelFactory(DefaultLogoutRepository())
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
                binding.tvTitle.text = "Подтверждение ВЫХОДА из личного кабинета"
                binding.btnPrimary.text = "Выход с данного устройства"
                binding.btnSecondary.text = "Выход со всех устройств"
                binding.btnCancel.text = "Отменить"

                binding.btnPrimary.setOnClickListener {
                    viewModel.logoutCurrentDevice()
                }

                binding.btnSecondary.setOnClickListener {
                    viewModel.logoutAllDevices()
                }
            }

            ConfirmActionType.DELETE_ACCOUNT -> {
                binding.tvTitle.text =
                    "Внимание! Удаление аккаунта.\nЕсли вы подтверждаете удаление аккаунта, то " +
                            "ваш аккаунт будет заблокирован на 30 дней с последующим удалением." +
                            "\nЕсли Вы решите восстановить аккаунт в течении указанных 30 дней, " +
                            "то обратитесь в поддержку"
                binding.btnPrimary.text = "Удалить"
                binding.btnSecondary.visibility = View.GONE
                binding.btnCancel.text = "Отменить"

                binding.btnPrimary.setOnClickListener {
                    viewModel.deleteAccount()
                }
            }
        }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnClose.setOnClickListener { dismiss() }
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

    companion object {
        private const val ARG_ACTION_TYPE = "action_type"

        fun newInstance(actionType: ConfirmActionType): ConfirmActionBottomSheet {
            return ConfirmActionBottomSheet().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ACTION_TYPE, actionType)
                }
            }
        }
    }
}