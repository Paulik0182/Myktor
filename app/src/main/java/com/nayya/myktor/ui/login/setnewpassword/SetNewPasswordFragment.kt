package com.nayya.myktor.ui.login.setnewpassword

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentSetNewPasswordBinding
import com.nayya.myktor.ui.login.DefaultAuthRepository
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.viewBinding

class SetNewPasswordFragment : BaseFragment(R.layout.fragment_set_new_password) {

    private val binding by viewBinding<FragmentSetNewPasswordBinding>()

    private val viewModel by viewModels<SetNewPasswordViewModel> {
        SetNewPasswordViewModelFactory(DefaultAuthRepository())
    }

    private val token: String by lazy {
        arguments?.getString(ARG_TOKEN) ?: ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSetPassword.setOnClickListener {
            val newPassword = binding.etNewPassword.text.toString()
            if (newPassword.length < 6) {
                Toast.makeText(requireContext(), "Минимум 6 символов", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.setNewPassword(token, newPassword)
        }

        viewModel.status.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SetPasswordState.Success -> {
                    Toast.makeText(requireContext(), "Пароль обновлён", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }

                is SetPasswordState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val ARG_TOKEN = "token"

        fun newInstance(token: String): SetNewPasswordFragment {
            val fragment = SetNewPasswordFragment()
            fragment.arguments = bundleOf(ARG_TOKEN to token)
            return fragment
        }
    }
}
