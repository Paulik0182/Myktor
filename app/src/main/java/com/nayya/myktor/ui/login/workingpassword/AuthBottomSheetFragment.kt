package com.nayya.myktor.ui.login.workingpassword

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nayya.myktor.R
import com.nayya.myktor.databinding.BottomSheetAuthBinding
import com.nayya.myktor.databinding.LayoutLegalEntityBinding
import com.nayya.myktor.databinding.PersonNameFieldsBinding
import com.nayya.myktor.ui.login.DefaultAuthRepository
import com.nayya.myktor.ui.profile.detailscounterparty.CounterpartyDetailsViewModel
import com.nayya.myktor.ui.profile.detailscounterparty.CounterpartyValidationDelegate
import com.nayya.myktor.utils.showSnackbarBottomSheetDialog

// TODO Проблема скролинга на шторке. Не получается заставить контекст скролится при раскрытой клавиатурой!
//  Как вариант доработки: Сделать дополнительное поле для подтверждения введенного пароля. Сделать
//  после регистрации сразу автоматическую авторизацию, или как вариант, после закрытия шторки с регистрации
//  заполнять поля на экране входа.
//  Дополнительно сделать запоминание пользователя с паролем, но это должно быть только если пользователь этого хочет.
//  Попробовать прикрутить метрику (отпечатки пальцев)
class AuthBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetAuthBinding
    private val viewModel by viewModels<AuthViewModel> {
        AuthViewModelFactory(DefaultAuthRepository())
    }
    private lateinit var counterpartyDetailsViewModel: CounterpartyDetailsViewModel

    private lateinit var legalEntityBinding: LayoutLegalEntityBinding
    private lateinit var personNameRegisterBinding: PersonNameFieldsBinding

    private lateinit var validator: CounterpartyValidationDelegate

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

        counterpartyDetailsViewModel =
            ViewModelProvider(this).get(CounterpartyDetailsViewModel::class.java)

        legalEntityBinding = LayoutLegalEntityBinding.bind(binding.includeLegalEntityRegister.root)
        personNameRegisterBinding =
            PersonNameFieldsBinding.bind(binding.includePersonNameRegister.root)

        counterpartyDetailsViewModel.setEditMode(true)

        validator = CounterpartyValidationDelegate(
            context = requireContext(),
            viewModel = counterpartyDetailsViewModel,
            binding = personNameRegisterBinding,
            legalEntityBinding = legalEntityBinding
        )

        setupUiByMode()
        setupObservers()


        // TODO Этот код деактивирует поля для внесения более полной информации о пользователе
        //  который регистрируется. Также мы скрываем группу элементов (layoutPersonNameFields)
        //  участвующую в более широком учете информации.
        //  Решино при регистрации нового пользователя, требовать от пользователя минимум данных,
        //  далее будем ограничивать пользователя в действиях если нехватает какихто данных.
        //  В случае решения, требовать на старте заполнять больше информации, нужно перевести BottomSheet
        //  на фрагмет и сделать постраничное заполнение данных с возможностью пропустить некоторые данные к заполнению.
//        if (mode == AuthMode.REGISTER) {
//            forceEditMode()
//            setupChangeListeners()
//            validator.setupAll()
//        }
        personNameRegisterBinding.layoutPersonNameFields.isVisible =false

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
//                    AuthBottomSheetFragment.newInstance(AuthMode.SET_NEW_PASSWORD).show(childFragmentManager, "auth")
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)

        dialog.setOnShowListener { dlg ->
            val bottomSheet = (dlg as? BottomSheetDialog)
                ?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                    isHideable = true
                    isFitToContents = false
                    expandedOffset = dpToPx(70) // если нужно отступ — можно dpToPx(100)
                }

                it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                it.requestLayout()
            }
        }

        return dialog
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
                personNameRegisterBinding.layoutPersonNameFields.isVisible = false
                legalEntityBinding.layoutLegalEntity.isVisible = false
            }

            AuthMode.SET_NEW_PASSWORD -> {
                binding.tvTitle.text = "Новый пароль"
                binding.btnAction.text = "Установить"
                binding.etEmail.isVisible = false
                binding.etPassword.isVisible = true
                personNameRegisterBinding.layoutPersonNameFields.isVisible = false
                legalEntityBinding.layoutLegalEntity.isVisible = false
            }
        }
    }

    private fun setupChangeListeners() {
        with(binding.scEntityStatus) {
            // Настройка видимости и состояния
            isVisible = true
            isEnabled = true
            isChecked = false

            updateRegistrationInformationVisibility(false)
            setSwitchState(false)

            // Обработчик изменений
            setOnCheckedChangeListener { _, isChecked ->
                Log.d("@@@", "Switch toggled: $isChecked")
                setSwitchState(isChecked)
                updateRegistrationInformationVisibility(isChecked)
            }
        }
    }

    private fun setSwitchState(isLegalEntity: Boolean) {
        with(binding.scEntityStatus) {
            text = if (isLegalEntity) "Юридическое лицо" else "Физическое лицо"

            // Установка цветов (можно вынести в отдельный метод, если используется в нескольких местах)
            thumbTintList = ContextCompat.getColorStateList(
                requireContext(),
                if (isLegalEntity) R.color.switch_thumb_color else R.color.switch_thumb_color
            )
            trackTintList = ContextCompat.getColorStateList(
                requireContext(),
                if (isLegalEntity) R.color.switch_track_color else R.color.switch_track_color
            )
        }
    }

    private fun updateRegistrationInformationVisibility(isLegalEntity: Boolean) {
        // Всегда видим
        personNameRegisterBinding.ccavShortName.visibility = View.VISIBLE

        // Только для Физлица
        personNameRegisterBinding.ccavFirstName.visibility =
            if (isLegalEntity) View.GONE else View.VISIBLE
        personNameRegisterBinding.ccavLastName.visibility =
            if (isLegalEntity) View.GONE else View.VISIBLE

        // Только для Юрлица
        legalEntityBinding.layoutLegalEntity.visibility =
            if (isLegalEntity) View.VISIBLE else View.GONE
        legalEntityBinding.ccavCompanyName.visibility =
            if (isLegalEntity) View.VISIBLE else View.GONE
        legalEntityBinding.ccavNIP.visibility = if (isLegalEntity) View.VISIBLE else View.GONE

        // Остальные элементы скрываем всегда
        legalEntityBinding.llTypes.isVisible = false
        legalEntityBinding.flCcavType.isVisible = false
        legalEntityBinding.ccavKRS.isVisible = false
    }

    private fun forceEditMode() {
        with(personNameRegisterBinding) {
            ccavShortName.apply {
                setReadOnlyMode(false)
                isInputEnabled = true
            }
            ccavFirstName.apply {
                setReadOnlyMode(false)
                isInputEnabled = true
            }
            ccavLastName.apply {
                setReadOnlyMode(false)
                isInputEnabled = true
            }
        }

        with(legalEntityBinding) {
            ccavCompanyName.apply {
                setReadOnlyMode(false)
                isInputEnabled = true
            }
            ccavNIP.apply {
                setReadOnlyMode(false)
                isInputEnabled = true
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
                    Log.d("@@@", "setupObservers() called with: state = ${state.code}")
                    Log.d("@@@", "setupObservers() called with: state = ${state.message}")

                    when (state.code) {
                        "email_not_found" -> {
                            binding.etEmail.error = state.message
                            showSnackbarBottomSheetDialog(state.message)
                        }

                        "user_blocked_admin" -> {
                            hideKeyboard(binding.etEmail)
                            binding.etEmail.error = state.message
                            showSupportDialog("Внимание!", state.message)
                        }

                        "user_deleted_self" -> {
                            hideKeyboard(binding.etEmail)
                            binding.etEmail.error = state.message
                            showSupportDialog("Внимание!", state.message)
                        }

                        "user_soft_deleted" -> {
                            hideKeyboard(binding.etEmail)
                            binding.etEmail.error = state.message
                            showSupportDialog("Внимание!", state.message)
                        }

                        "email_exists" -> {
                            binding.etEmail.error = state.message
                            showSnackbarBottomSheetDialog(state.message)
                        }

                        "invalid_email" -> binding.etEmail.error = state.message
                        "weak_password" -> binding.etPassword.error = state.message
                        else -> showSnackbarBottomSheetDialog(state.message)
                    }
                }
            }
        }
    }

    private fun showSupportDialog(title: String, message: String) {
        hideKeyboard()

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Отмена", null)
            .setNegativeButton("Написать в поддержку") { _, _ ->
                showSnackbarBottomSheetDialog("Письмо ушло. Доделать реализацию!")
            }
            .show()
    }

    private fun BottomSheetDialogFragment.hideKeyboard(view: View = requireView()) {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
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
