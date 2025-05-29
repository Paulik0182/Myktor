package com.nayya.uicomponents

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.nayya.uicomponents.databinding.LayoutCardActionViewBinding
import androidx.core.widget.doAfterTextChanged

class CustomCardActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutCardActionViewBinding.inflate(LayoutInflater.from(context), this)

    private var inputEllipsize: TextUtils.TruncateAt? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomCardActionView,
            0,
            0
        ).apply {
            try {
                text = getString(R.styleable.CustomCardActionView_inputText)
                isInputEnabled = getBoolean(R.styleable.CustomCardActionView_inputEnabled, true)
                hint = getString(R.styleable.CustomCardActionView_hintText)

                getResourceId(
                    R.styleable.CustomCardActionView_inputBackground,
                    0
                ).takeIf { it != 0 }?.let {
                    setInputBackground(it)
                }

                getResourceId(R.styleable.CustomCardActionView_cardBackground, 0).takeIf { it != 0 }
                    ?.let {
                        setCardBackground(it)
                    }

                description = getString(R.styleable.CustomCardActionView_descriptionText)
                info = getString(R.styleable.CustomCardActionView_infoText)

                leftIconRes = getResourceId(R.styleable.CustomCardActionView_leftIconRes, 0)
                editIconRes = getResourceId(R.styleable.CustomCardActionView_editIconRes, 0)

                showInfoIcon = getBoolean(R.styleable.CustomCardActionView_showInfoIcon, true)
                showInfoText = getBoolean(R.styleable.CustomCardActionView_showInfoText, true)
                showDescriptionIcon =
                    getBoolean(R.styleable.CustomCardActionView_showDescriptionIcon, true)
                showDescriptionText =
                    getBoolean(R.styleable.CustomCardActionView_showDescriptionText, true)

                showEditIcon = getBoolean(R.styleable.CustomCardActionView_showEditIcon, true)
                showLeftIcon = getBoolean(R.styleable.CustomCardActionView_showLeftIcon, true)
                showUnderline = getBoolean(R.styleable.CustomCardActionView_showUnderline, false)

                // Размер текста
                if (hasValue(R.styleable.CustomCardActionView_inputTextSize)) {
                    val textSize = getDimension(R.styleable.CustomCardActionView_inputTextSize, 14f)
                    setInputTextSize(textSize)
                }

                // Стиль текста
                if (hasValue(R.styleable.CustomCardActionView_inputTextStyle)) {
                    val style = getInt(R.styleable.CustomCardActionView_inputTextStyle, 0)
                    setInputTextStyle(style)
                }

                // Appearance (если нужен внешний стиль)
                if (hasValue(R.styleable.CustomCardActionView_inputTextAppearance)) {
                    val appearance =
                        getResourceId(R.styleable.CustomCardActionView_inputTextAppearance, 0)
                    if (appearance != 0) {
                        setInputTextAppearance(appearance)
                    }
                }

                when (getInt(R.styleable.CustomCardActionView_inputType, 0)) {
                    0 -> binding.etInputUser.inputType = InputType.TYPE_CLASS_TEXT
                    1 -> binding.etInputUser.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    2 -> binding.etInputUser.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

                    3 -> binding.etInputUser.inputType = InputType.TYPE_CLASS_PHONE
                    4 -> binding.etInputUser.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

                    5 -> binding.etInputUser.inputType = InputType.TYPE_CLASS_NUMBER
                    6 -> binding.etInputUser.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED

                    7 -> binding.etInputUser.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

                    8 -> binding.etInputUser.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
                }

                if (hasValue(R.styleable.CustomCardActionView_inputFontFamily)) {
                    val font = getString(R.styleable.CustomCardActionView_inputFontFamily)
                    binding.etInputUser.typeface = Typeface.create(font, Typeface.NORMAL)
                    binding.tvInputUserReadOnly.typeface = Typeface.create(font, Typeface.NORMAL)
                }

                val ime = getInt(
                    R.styleable.CustomCardActionView_inputImeOptions,
                    EditorInfo.IME_ACTION_DONE
                )
                binding.etInputUser.imeOptions = ime


                if (hasValue(R.styleable.CustomCardActionView_inputLines)) {
                    val lines = getInt(R.styleable.CustomCardActionView_inputLines, 2)
                    binding.etInputUser.setLines(lines)
                }

                if (hasValue(R.styleable.CustomCardActionView_inputMaxLines)) {
                    val lines = getInt(R.styleable.CustomCardActionView_inputMaxLines, 2)
                    binding.etInputUser.maxLines = lines
                }

                if (hasValue(R.styleable.CustomCardActionView_inputMaxLength)) {
                    val maxLength =
                        getInt(R.styleable.CustomCardActionView_inputMaxLength, Int.MAX_VALUE)
                    binding.etInputUser.filters = arrayOf(InputFilter.LengthFilter(maxLength))
                }

                when (getInt(R.styleable.CustomCardActionView_inputEllipsize, 0)) {
                    1 -> inputEllipsize = TextUtils.TruncateAt.START
                    2 -> inputEllipsize = TextUtils.TruncateAt.MIDDLE
                    3 -> inputEllipsize = TextUtils.TruncateAt.END
                    4 -> inputEllipsize = TextUtils.TruncateAt.MARQUEE
                    else -> inputEllipsize = null
                }
                binding.etInputUser.ellipsize = inputEllipsize
                binding.tvInputUserReadOnly.ellipsize = inputEllipsize

                // Цвет текста
                if (hasValue(R.styleable.CustomCardActionView_inputTextColor)) {
                    val color = getColor(R.styleable.CustomCardActionView_inputTextColor, 0)
                    binding.etInputUser.setTextColor(color)
                }

                // Цвет текста подсказки (hint)
                if (hasValue(R.styleable.CustomCardActionView_inputHintTextColor)) {
                    val color = getColor(R.styleable.CustomCardActionView_inputHintTextColor, 0)
                    binding.etInputUser.setHintTextColor(color)
                }

                isEditIconEnabled =
                    getBoolean(R.styleable.CustomCardActionView_editIconEnabled, true)

                val scrolling = getBoolean(
                    R.styleable.CustomCardActionView_inputHorizontallyScrolling,
                    false
                )
                setInputHorizontallyScrolling(scrolling)

                // ✩ Важно: отключаем singleLine, чтобы \n работал как перенос строки
                // Некоторые inputType (например, textNoSuggestions) включают singleLine по умолчанию
                binding.etInputUser.apply {
                    isSingleLine = false
                }

                syncReadOnlyTextStyle()
            } finally {
                recycle()
            }
        }
    }

    var text: String?
        get() = binding.etInputUser.text?.toString()
        set(value) {
            binding.etInputUser.setText(value)
        }

    /**
     * установка TextWatcher
     */
    fun addTextChangedListener(watcher: TextWatcher) {
        binding.etInputUser.addTextChangedListener(watcher)
    }

    var isInputEnabled: Boolean
        get() = binding.etInputUser.isEnabled
        set(value) {
            binding.etInputUser.isEnabled = value
            binding.etInputUser.isFocusable = value
            binding.etInputUser.isFocusableInTouchMode = value
            binding.etInputUser.isCursorVisible = value
        }

    var hint: String? = null
        set(value) {
            field = value
            binding.etInputUser.hint = value
        }

    /**
     * ✩ Управление текстом описания (без контроля видимости)
     */
    var description: String? = null
        set(value) {
            field = value
            binding.tvDescription.text = value
//            binding.tvDescription.isVisible = !value.isNullOrEmpty() // Контроль видимости
        }

    /**
     * ✩ Управление текстом информации (без контроля видимости)
     */
    var info: String? = null
        set(value) {
            field = value
            binding.tvInformation.text = value
//            binding.tvInformation.isVisible = !value.isNullOrEmpty() // Контроль видимости
        }

    /**
     * ✩ Установка ресурса левой иконки + показ
     */
    var leftIconRes: Int = 0
        set(value) {
            field = value
            if (value != 0) {
                binding.ivActionIcon.setImageResource(value)
                binding.ivActionIcon.isVisible = true // автоотображение
            } else {
                binding.ivActionIcon.isVisible = false
            }
        }

    /**
     * ✩ Управление отображением иконки слева
     */
    var showLeftIcon: Boolean = true
        set(value) {
            field = value
            binding.ivActionIcon.isVisible = value

            val marginStart = if (value) {
                context.resources.getDimensionPixelSize(R.dimen.etInputUser_marginStart_with_icon)
            } else {
                0
            }

            // Установка marginStart для EditText
            (binding.etInputUser.layoutParams as ConstraintLayout.LayoutParams).apply {
                this.marginStart = marginStart
                binding.etInputUser.layoutParams = this
            }

            // Установка marginStart для TextView
            (binding.tvInputUserReadOnly.layoutParams as ConstraintLayout.LayoutParams).apply {
                this.marginStart = marginStart
                binding.tvInputUserReadOnly.layoutParams = this
            }
        }

    /**
     * ✩ Управление отображением нижней линии под вводом
     */
    var showUnderline: Boolean = true
        set(value) {
            field = value
            binding.vUnderline.isVisible = value
        }

    var leftIconRes2: Int = 0
        set(value) {
            field = value
            if (value != 0) {
                binding.ivActionIcon.setImageResource(value)
            }
        }

    /**
     * ✩ Установка ресурса иконки редактирования + показ
     */
    var editIconRes: Int = 0
        set(value) {
            field = value
            if (value != 0) {
                binding.btnEdit.setImageResource(value)
                binding.btnEdit.isVisible = true // автоотображение
            } else {
                binding.btnEdit.isVisible = false
            }
        }

    var editIconRes2: Int = 0
        set(value) {
            field = value
            if (value != 0) {
                binding.btnEdit.setImageResource(value)
            }
        }

    /**
     * ✩ Управление отображением иконки информации
     */
    var showInfoIcon: Boolean = true
        set(value) {
            field = value
            binding.ivInformationIcon.isVisible = value
        }

    /**
     * ✩ Управление отображением текста информации
     */
    var showInfoText: Boolean = true
        set(value) {
            field = value
            binding.tvInformation.isVisible = value
        }

    /**
     * ✩ Управление отображением иконки описания
     */
    var showDescriptionIcon: Boolean = true
        set(value) {
            field = value
            binding.ivDescriptionIcon.isVisible = value
        }

    /**
     * ✩ Управление отображением текста описания
     */
    var showDescriptionText: Boolean = true
        set(value) {
            field = value
            binding.tvDescription.isVisible = value
        }

    /**
     * ✩ Установка слушателя на кнопку редактирования
     */
    fun setEditClickListener(listener: () -> Unit) {
        binding.btnEdit.setOnClickListener { listener.invoke() }
    }

    var showEditIcon: Boolean = true
        set(value) {
            field = value
            binding.btnEdit.isVisible = value
        }

    // отключение клика на кнопке
    var isEditIconEnabled: Boolean
        get() = binding.btnEdit.isEnabled
        set(value) {
            binding.btnEdit.isEnabled = value
        }

    /**
     * ✩ Показать карточку
     */
    fun show() {
        binding.cardActionContainer.isVisible = true
    }

    /**
     * ✩ Скрыть карточку
     */
    fun hide() {
        binding.cardActionContainer.isVisible = false
    }

    /**
     * ✩ Задать фон ввода
     */
    fun setInputBackground(resourceId: Int) {
        binding.vgInput.setBackgroundResource(resourceId)
    }

    /**
     * ✩ Задать фон карточки
     */
    fun setCardBackground(resourceId: Int) {
        binding.cardActionContainer.setCardBackgroundColor(
            ContextCompat.getColor(context, resourceId)
        )
    }

    fun setInputTextSize(sizePx: Float) {
        binding.etInputUser.textSize = sizePx / resources.displayMetrics.scaledDensity
        binding.tvInputUserReadOnly.textSize = sizePx / resources.displayMetrics.scaledDensity
        syncReadOnlyTextStyle()
    }

    fun setInputTextStyle(style: Int) {
        binding.etInputUser.setTypeface(null, style)
        binding.tvInputUserReadOnly.setTypeface(null, style)
        syncReadOnlyTextStyle()
    }

    fun setInputTextAppearance(@StyleRes appearanceRes: Int) {
        binding.etInputUser.setTextAppearance(appearanceRes)
        binding.tvInputUserReadOnly.setTextAppearance(appearanceRes)
        syncReadOnlyTextStyle()
    }

    /**
     * ✩ Установить цвет текста
     */
    fun setInputTextColor(color: Int) {
        binding.etInputUser.setTextColor(color)
        binding.tvInputUserReadOnly.setTextColor(color)
        syncReadOnlyTextStyle()
    }

    /**
     * ✩ Установить цвет текста подсказки (hint)
     */
    fun setInputHintTextColor(color: Int) {
        binding.etInputUser.setHintTextColor(color)
        binding.tvInputUserReadOnly.setHintTextColor(color)
        syncReadOnlyTextStyle()
    }

    fun setInputLines(lines: Int) {
        binding.etInputUser.setLines(lines)
    }

    fun setInputMaxLines(lines: Int) {
        binding.etInputUser.maxLines = lines
    }

    fun setInputMaxLength(length: Int) {
        binding.etInputUser.filters = arrayOf(InputFilter.LengthFilter(length))
    }

    fun setInputHorizontallyScrolling(enabled: Boolean) {
        binding.etInputUser.setHorizontallyScrolling(enabled)
    }

    private var _isHorizontallyScrolling = false

    var isHorizontallyScrolling: Boolean
        get() = _isHorizontallyScrolling
        set(value) {
            _isHorizontallyScrolling = value
            binding.etInputUser.setHorizontallyScrolling(value)
        }

    fun setSelection(position: Int) {
        binding.etInputUser.setSelection(position)
    }

    fun getSelection(): Int {
        return binding.etInputUser.selectionStart
    }

    fun setBottomTextState(state: BottomTextState) {
        when (state) {
            is BottomTextState.Description -> {
                showDescriptionText = state.showDescriptionText
                showDescriptionIcon = state.showDescriptionIcon
                description = state.descriptionText
                setDescriptionColor(isError = false)
            }

            is BottomTextState.Error -> {
                showDescriptionText = state.showErrorText
                showDescriptionIcon = state.showErrorIcon
                description = state.errorText
                setDescriptionColor(isError = true)
            }

            is BottomTextState.Empty -> {
                showDescriptionText = false
                showDescriptionIcon = false
                description = null
            }
        }
    }

    private fun setDescriptionColor(isError: Boolean) {
        val color = if (isError) {
            ContextCompat.getColor(context, R.color.input_bottom_error)
        } else {
            ContextCompat.getColor(context, R.color.input_bottom_color)
        }
        binding.tvDescription.setTextColor(color)
        binding.ivDescriptionIcon.setColorFilter(color)
    }

    fun setReadOnlyMode(enabled: Boolean) {
        if (enabled) {
            val currentText = binding.etInputUser.text?.toString().orEmpty()
            binding.tvInputUserReadOnly.text = currentText
            binding.etInputUser.visibility = View.GONE
            binding.tvInputUserReadOnly.visibility = View.VISIBLE
        } else {
            binding.etInputUser.visibility = View.VISIBLE
            binding.tvInputUserReadOnly.visibility = View.GONE
        }
    }

    fun setTextAndMode(text: String, readOnly: Boolean) {
        this.text = text // Установит в EditText
        if (readOnly) {
            binding.tvInputUserReadOnly.text = text
        }
        setReadOnlyMode(readOnly)
    }

    private fun syncReadOnlyTextStyle() {
        binding.tvInputUserReadOnly.apply {
            textSize = binding.etInputUser.textSize / resources.displayMetrics.scaledDensity
            setTypeface(
                binding.etInputUser.typeface,
                binding.etInputUser.typeface?.style ?: Typeface.NORMAL
            )
            setTextColor(binding.etInputUser.currentTextColor)
            gravity = binding.etInputUser.gravity
            ellipsize = inputEllipsize
            setPadding(
                binding.etInputUser.paddingLeft,
                binding.etInputUser.paddingTop,
                binding.etInputUser.paddingRight,
                binding.etInputUser.paddingBottom
            )
        }
    }

    fun setInputEllipsize(mode: TextUtils.TruncateAt?) {
        inputEllipsize = mode
        binding.etInputUser.ellipsize = mode
        binding.tvInputUserReadOnly.ellipsize = mode
    }

    /**
     * Позволяет использовать doAfterTextChanged { ... }
     */
    fun doAfterTextChanged(action: (text: Editable?) -> Unit): TextWatcher {
        return binding.etInputUser.doAfterTextChanged(action)
    }
//    fun getText(): String {
//        return binding.etInputUser.text?.toString().orEmpty()
//    }
}
