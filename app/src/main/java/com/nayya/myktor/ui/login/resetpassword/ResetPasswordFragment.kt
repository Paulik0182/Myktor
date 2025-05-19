package com.nayya.myktor.ui.login.resetpassword

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentResetPasswordBinding
import com.nayya.myktor.ui.login.DefaultAuthRepository
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.viewBinding

class ResetPasswordFragment : BaseFragment(R.layout.fragment_reset_password) {

    private val binding by viewBinding<FragmentResetPasswordBinding>()
    private val viewModel by viewModels<ResetPasswordViewModel> {
        ResetPasswordViewModelFactory(DefaultAuthRepository())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSend.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Введите email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.resetPassword(email)
        }

        viewModel.status.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResetPasswordState.Success -> {
                    Toast.makeText(requireContext(), "Письмо отправлено", Toast.LENGTH_LONG).show()
                    requireController<ResetPasswordFragment.Controller>().onSetNewPassword()
//                    parentFragmentManager.popBackStack() // возвращаемся назад
                }

                is ResetPasswordState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    interface Controller : BaseFragment.Controller {
        fun onSetNewPassword()
    }

    companion object {
        fun newInstance() = ResetPasswordFragment()
    }
}
