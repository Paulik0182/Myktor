package com.nayya.myktor.utils.input

import android.content.Context
import androidx.emoji2.text.EmojiCompat
import com.nayya.myktor.R

class InputValidator {

    companion object {
        const val MAX_LENGTH = 30
        const val MAX_LENGTH_ABOUT_ME = 150
        private const val MAX_ALLOWED_INITIAL_LENGTH = 5
        private const val MIN_ALLOWED_INITIAL_LENGTH = 1
//        private val INVALID_CHARACTERS = Regex("[^a-zA-Z0-9._\\-]")

        /**
         * разрешение кириллицы: а-яА-ЯёЁ.
         * Добавлены Unicode категории:
         * p{L} — буквы всех языков.
         * p{N} — цифры всех языков.
         * p{P} — пунктуационные символы.
         * p{S} — символы (например, $, %, &, и т.д.).
         * s — пробелы.
         * Regex("[^a-zA-Zа-яА-ЯёЁ0-9._\\-\\p{L}\\p{N}\\p{P}\\p{S}\\s]|[+!]")
         *
         * Запрет:
         * ”+”, “!”
         */
        private val INVALID_CHARACTERS = Regex("[+!]")

        /**
         * [^a-zA-Z0-9._\\-] - Запрещает все символы, кроме латинских букв, цифр, точки, подчеркивания и дефиса.
         * [\uD800-\uDFFF] - Запрещает все суррогатные пары UTF-16, которые используются для представления эмодзи.
         * [\u0080-\uFFFF] - Запрещает все символы Unicode, которые не являются частью ASCII-диапазона (\u0000-\u007F).
         */
        private val ADVANCED_INVALID_CHARACTERS =
            Regex("[^a-zA-Z0-9@#/_\\-.\\s]|[\uD800-\uDFFF]|[\u0080-\uFFFF]")

        private val ONLY_LETTERS_DASH_SPACE_REGEX = Regex("[^a-zA-Zа-яА-ЯёЁ\\-\\s]")

        /**
         * [\n\r] - Запрещает перенос строк
         */
        private val INVALID_LINE_BREAKS_REGEX = Regex("[\\n\\r]")
        private val RESERVED_NAMES = setOf(
            "everyone", "here", "channel", "nomeera", "noomera", "noomeera", "nomera"
        )
        private val PROHIBITED_WORDS = setOf(
            "данунахуй",
            ".идарюги!",
            "@бнутая",
            "@лять",
            "@хуевшей",
            "*банцой",
            "*баться",
            "*уяк",
            "#допизды",
            "#изди",
            "#мозгупи@да",
            "#beпохуй",
            "᙭уё-моё",
            "᙭уёвина"
        )

        fun validateName(context: Context, name: String): String? {
            // Проверка на запрещённые символы (например, + и !)
            if (INVALID_CHARACTERS.containsMatchIn(name)) {
                return context.getString(R.string.error_invalid_characters)
            }

            // Надёжная проверка на Emoji через суррогатные пары
            val containsEmoji = name.any { char ->
                Character.getType(char) == Character.SURROGATE.toInt()
            }

            return if (containsEmoji) {
                context.getString(R.string.error_emoji_not_allowed)
            } else {
                null
            }
        }

        // Проверка на максимальное количество символов
        fun validateLength(context: Context, name: String, lengthText: Int = MAX_LENGTH): String? {
            return if (name.length > lengthText) {
                context.getString(R.string.error_max_length, lengthText)
            } else null
        }

        // Проверка на пустоту
        fun validateEmpty(context: Context, name: String): String? {
            return if (name.isBlank()) {
                context.getString(R.string.error_empty_field)
            } else null
        }

        // Проверка на максимально разрешонную начальную длинну текста
        fun validateMaxAllowedInitialLength(context: Context, name: String): String? {
            return if (name.length < MAX_ALLOWED_INITIAL_LENGTH) {
                context.getString(R.string.error_min_length, MAX_ALLOWED_INITIAL_LENGTH)
            } else null
        }

        // Проверка на минимальное количество символов
        fun validateMinAllowedInitialLength(
            context: Context,
            name: String,
            lengthText: Int = MIN_ALLOWED_INITIAL_LENGTH,
        ): String? {
            return if (name.length < lengthText) {
                context.getString(R.string.error_min_length, lengthText)
            } else null
        }

        // Проверка на зарезервированное имя
        fun validateReservedName(context: Context, name: String): String? {
            return if (RESERVED_NAMES.contains(name.lowercase())) {
                context.getString(R.string.error_reserved_name)
            } else null
        }

        // Проверка по Справочнику “Антимат словарь”
        fun validateProhibitedWords(context: Context, name: String): String? {
            val normalizedInput = name.lowercase() // Приведение строки к нижнему регистру
            return if (PROHIBITED_WORDS.contains(normalizedInput)) {
                context.getString(R.string.error_prohibited_words)
            } else null
        }

        // Проверка на недопустимые символы
        fun validateCharacters(context: Context, name: String): String? {
            return if (ADVANCED_INVALID_CHARACTERS.containsMatchIn(name)) {
                context.getString(R.string.error_invalid_characters_for_unique_name)
            } else null
        }

        fun validateOnlyLettersDashAndSpace(context: Context, name: String): String? {
            return if (ONLY_LETTERS_DASH_SPACE_REGEX.containsMatchIn(name)) {
                context.getString(R.string.error_only_letters_allowed)
            } else null
        }

        // Проверка на перенос строки
        fun validateLineBreaksAndCharacters(context: Context, name: String): String? {
            return if (INVALID_LINE_BREAKS_REGEX.containsMatchIn(name)) {
                context.getString(R.string.error_invalid_characters_for_unique_name)
            } else null
        }

        // Проверка на начальную или конечную точку
        fun validateStartingOrEndingDot(context: Context, name: String): String? {
            return if (name.startsWith(".") || name.endsWith(".")) {
                context.getString(R.string.error_starting_or_ending_dot)
            } else null
        }

        fun validateStartingDot(context: Context, name: String): String? {
            return if (name.startsWith(".")) {
                context.getString(R.string.error_starting_dot)
            } else null
        }

        fun validateEndDot(context: Context, name: String): String? {
            return if (name.endsWith(".")) {
                context.getString(R.string.error_end_dot)
            } else null
        }


        // Проверка первого или последнего пробела
        fun validateStartingOrEndingSpace(context: Context, name: String): String? {
            return if (name.startsWith(" ") || name.endsWith(" ")) {
                context.getString(R.string.error_invalid_characters_for_unique_name)
            } else null
        }
    }
}