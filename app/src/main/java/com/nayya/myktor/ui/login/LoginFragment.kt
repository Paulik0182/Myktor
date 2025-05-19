package com.nayya.myktor.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentLoginBinding
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.viewBinding

class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val binding by viewBinding<FragmentLoginBinding>()
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(DefaultAuthRepository())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Введите email и пароль", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            requireController<LoginFragment.LoginController>().onRegisterClicked()
        }

        binding.tvResetPassword.setOnClickListener {
            requireController<LoginFragment.LoginController>().onForgotPasswordClicked()
        }

        binding.tvPrivacyPolicy.setOnClickListener {
            requireController<LoginFragment.LoginController>().onPrivacyPolicyClicked()
        }

        binding.tvClientInfo.setOnClickListener {
            requireController<LoginFragment.LoginController>().onClientInfoClicked()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginState.Success -> {
                    Toast.makeText(requireContext(), "Добро пожаловать!", Toast.LENGTH_SHORT).show()
                    requireController<LoginFragment.LoginController>().onLoginSuccess(state.token)
                }

                is LoginState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    interface LoginController : BaseFragment.Controller {
        fun onLoginSuccess(token: String)
        fun onRegisterClicked()
        fun onForgotPasswordClicked()
        fun onPrivacyPolicyClicked()
        fun onClientInfoClicked()
    }

    companion object {

        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}