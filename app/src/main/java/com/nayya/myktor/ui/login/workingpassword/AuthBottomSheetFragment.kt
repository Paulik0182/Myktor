package com.nayya.myktor.ui.login.workingpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nayya.myktor.databinding.BottomSheetAuthBinding
import com.nayya.myktor.ui.login.DefaultAuthRepository

class AuthBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetAuthBinding
    private val viewModel by viewModels<AuthViewModel> { AuthViewModelFactory(DefaultAuthRepository()) }

    private val mode: AuthMode by lazy {
        arguments?.getSerializable(ARG_MODE) as AuthMode
    }

    private val token: String? by lazy {
        arguments?.getString(ARG_TOKEN)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = BottomSheetAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUiByMode()
        setupObservers()

        binding.btnAction.setOnClickListener {
            when (mode) {
                AuthMode.REGISTER -> {
                    val email = binding.etEmail.text.toString().trim()
                    val password = binding.etPassword.text.toString().trim()
                    viewModel.register(email, password)
                }

                AuthMode.RESET_PASSWORD -> {
                    val email = binding.etEmail.text.toString().trim()
                    viewModel.resetPassword(email)
                    // TODO после ввода email должно открытся AuthBottomSheetFragment. Но наверное
                    //  это не правильно, ведь у нас режим востановление и ссылка ушла на email
                    // одновременно с этим, если мы не ввели валидный email, то и перехода не должно быть.
                    // Еще должна быть проверка на наличие зарегистрированного email
                    AuthBottomSheetFragment.newInstance(AuthMode.SET_NEW_PASSWORD).show(childFragmentManager, "auth")
                    // Вот тут логика немного не понятная. Если письмо отправлено, то ссылка
                // находится в письме, а мне нужно кудато вписать новый пароль.Подумать, куда и
                // как вписывать новый пароль.
                }

                AuthMode.SET_NEW_PASSWORD -> {
                    val password = binding.etPassword.text.toString().trim()
                    viewModel.setNewPassword(token ?: return@setOnClickListener, password)
                }
            }
        }
    }

    private fun setupUiByMode() {
        when (mode) {
            AuthMode.REGISTER -> {
                binding.tvTitle.text = "Регистрация"
                binding.btnAction.text = "Зарегистрироваться"
                binding.etEmail.isVisible = true
                binding.etPassword.isVisible = true
            }

            AuthMode.RESET_PASSWORD -> {
                binding.tvTitle.text = "Восстановление пароля"
                binding.btnAction.text = "Отправить письмо"
                binding.etEmail.isVisible = true
                binding.etPassword.isVisible = false
            }

            AuthMode.SET_NEW_PASSWORD -> {
                binding.tvTitle.text = "Новый пароль"
                binding.btnAction.text = "Установить"
                binding.etEmail.isVisible = false
                binding.etPassword.isVisible = true
            }
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Success -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    dismiss()
                }

                is AuthState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private const val ARG_MODE = "arg_mode"
        private const val ARG_TOKEN = "arg_token"

        fun newInstance(mode: AuthMode, token: String? = null): AuthBottomSheetFragment {
            return AuthBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_MODE, mode)
                    token?.let { putString(ARG_TOKEN, it) }
                }
            }
        }
    }
}
