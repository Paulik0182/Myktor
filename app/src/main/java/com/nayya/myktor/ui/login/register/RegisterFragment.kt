package com.nayya.myktor.ui.login.register

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentRegisterBinding
import com.nayya.myktor.ui.login.DefaultAuthRepository
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.viewBinding

class RegisterFragment : BaseFragment(R.layout.fragment_register) {

    private val binding by viewBinding<FragmentRegisterBinding>()

    private val viewModel by viewModels<RegisterViewModel> {
        RegisterViewModelFactory(DefaultAuthRepository())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Введите email и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(email, password)
        }

        viewModel.status.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterState.Success -> {
                    Toast.makeText(requireContext(), "Успешно зарегистрированы", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                is RegisterState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        fun newInstance() = RegisterFragment()
    }
}
