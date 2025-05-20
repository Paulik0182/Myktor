package com.nayya.myktor.ui.login

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentLoginBinding
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.showSnackbar
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
                binding.etEmail.error = "Введите email и пароль"
                binding.etPassword.error = "Введите email и пароль"
                showSnackbar("Введите email и пароль")
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
                    showSnackbar("Добро пожаловать!")
                    requireController<LoginFragment.LoginController>().onLoginSuccess(state.token)
                }

                is LoginState.Error -> {
                    if (state.code != "wrong_credentials") {
                        hideKeyboard()
                    }

                    when (state.code) {
                        "wrong_credentials" -> {
                            binding.etEmail.error = "Проверьте email или пароль"
                            binding.etPassword.error = "Проверьте email или пароль"
                        }

                        "user_blocked_admin" -> showSupportDialog(state.message)
                        "user_deleted_self" -> showSupportDialog(state.message)
                        "user_soft_deleted" -> showSnackbar(state.message)
                        else -> showSnackbar(state.message)
                    }
                }
            }
        }
    }

    private fun showSupportDialog(message: String) {
        hideKeyboard()

        AlertDialog.Builder(requireContext())
            .setTitle("Вход невозможен")
            .setMessage(message)
            .setPositiveButton("Отмена", null)
            .setNegativeButton("Написать в поддержку") { _, _ ->
                showSnackbar("Письмо ушло. Доделать реализацию!")
            }
            .show()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireContext())
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
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
