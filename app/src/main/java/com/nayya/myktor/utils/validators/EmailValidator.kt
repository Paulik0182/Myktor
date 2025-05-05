package com.nayya.myktor.utils.validators

import android.content.Context
import com.nayya.myktor.R

object EmailValidator {

    private val EMAIL_REGEX = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    private val INVALID_CHARACTERS_REGEX = Regex("[^\\p{ASCII}]") // Базово исключаем эмодзи и юникод

    fun validateEmail(context: Context, email: String): String? {
        val trimmed = email.trim()

        return when {
            trimmed.isEmpty() ->
                context.getString(R.string.error_empty_field)

            trimmed.contains(Regex("[\\n\\r]")) ->
                context.getString(R.string.error_line_breaks)

            trimmed.startsWith(".") || trimmed.endsWith(".") ->
                context.getString(R.string.error_starting_or_ending_dot)

            trimmed.startsWith(" ") || trimmed.endsWith(" ") ->
                context.getString(R.string.error_starting_or_ending_space)

            INVALID_CHARACTERS_REGEX.containsMatchIn(trimmed) ->
                context.getString(R.string.error_invalid_characters)

            !EMAIL_REGEX.matches(trimmed) ->
                context.getString(R.string.error_invalid_email_format)

            trimmed.length < 5 ->
                context.getString(R.string.error_min_length, 5)

            trimmed.contains(Regex("[\\n\\r\\u2028\\u2029]")) ->
                context.getString(R.string.error_line_breaks)


            else -> null
        }
    }
}