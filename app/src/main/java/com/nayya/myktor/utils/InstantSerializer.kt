package com.nayya.myktor.utils

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import java.time.Instant
import java.time.format.DateTimeParseException

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString()) // Сериализация в строку
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString()) // Парсим обратно
    }
}

/**
 * Для работы с Instant использовать String.toInstantOrNull() когда нужно (например, сортировка
 * заказов по дате).
 *
 * Никакой зависимости от kotlinx.serialization — всё остаётся на Gson.
 *
 * Модели стабильны, проблем с парсингом не должно быть.
 */
fun String.toInstantOrNull(): Instant? = try {
    Instant.parse(this)
} catch (e: DateTimeParseException) {
    null
}

fun Instant.toIsoString(): String = this.toString()
