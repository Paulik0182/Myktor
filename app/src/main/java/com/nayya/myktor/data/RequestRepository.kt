package com.nayya.myktor.data

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class RequestRepository {

    /**
     * HTTP-клиент с использованием движка CIO (Coroutines I/O),
     * который оптимизирован для асинхронных операций в корутинах.
     *
     * CIO (Coroutines I/O) — это один из движков (engine) для HttpClient, разработанный специально
     * для асинхронных операций с использованием Kotlin Coroutines. Он основан на низкоуровневых
     * API java.nio и предоставляет высокую производительность, не блокируя основной поток.
     *
     * CIO обладает следующими преимуществами:
     * - Асинхронность:
     * Использует Kotlin Coroutines для эффективной работы с I/O-операциями, что позволяет выполнять несколько сетевых запросов одновременно без блокировки потока.
     * - Легковесность:
     * Не использует тяжелых зависимостей, таких как Apache HTTP Client или OkHttp, что делает его подходящим для мобильных приложений и микросервисов.
     * - Эффективность на сервере и клиенте:
     * Отлично работает как в Android-приложениях, так и на стороне серверов, поддерживая асинхронные соединения.
     * - Работа с потоками:
     * Использует неблокирующую модель I/O, которая уменьшает накладные расходы на потоки, что особенно важно для высоконагруженных приложений.
     *
     * Доступные движки:
     * CIO	Асинхронный движок на основе Kotlin Coroutines	Универсальный, быстрый
     * OkHttp	Использует OkHttp (Android)	Совместимость с Android
     * Apache	Использует Apache HttpClient	Подходит для JVM
     * Jetty	Использует Jetty (в основном для серверов)	Серверные приложения
     * Darwin	Используется на iOS	iOS-приложения
     *
     * При инициализации HttpClient(CIO) можно задать дополнительные параметры для настройки клиента:
     * private val client = HttpClient(CIO) {
     *     engine {
     *         requestTimeout = 10_000 // Тайм-аут на запрос (10 секунд)
     *         endpoint {
     *             connectTimeout = 10_000  // Время на подключение
     *             keepAliveTime = 5_000   // Время ожидания перед закрытием соединения
     *             maxConnectionsCount = 100 // Максимальное количество подключений
     *         }
     *     }
     * }
     *
     * Простой GET-запрос:
     * suspend fun getExample(): String {
     *     return client.get("http://example.com").body()
     * }
     * POST-запрос с JSON-телом:
     * suspend fun sendData(data: DataClass): String {
     *     return client.post("http://example.com") {
     *         contentType(ContentType.Application.Json)
     *         setBody(data)
     *     }.body()
     * }
     *
     * Возможные проблемы и решения
     * Ошибка "Connection refused" (Отказ в соединении)
     * Причина: Сервер не запущен или указан неправильный IP-адрес.
     * Решение: Использовать 10.0.2.2 вместо localhost в эмуляторе.
     *
     * Ошибка "Cleartext HTTP traffic not permitted"
     * Причина: Запрос на HTTP-сервер без HTTPS.
     * Решение: Добавить в AndroidManifest.xml:
     * <application android:usesCleartextTraffic="true"/>
     *
     * Ошибка таймаута (Timeout Exception)
     * Причина: Сервер долго обрабатывает запрос или сеть нестабильна.
     * Решение: Увеличить тайм-ауты в конфигурации HttpClient.
     *
     * Когда использовать CIO движок?
     * -Использовать CIO, когда:
     * Нужна легковесность и минимальная зависимость от сторонних библиотек.
     * Приложение требует асинхронной работы с сетью без блокировки потоков.
     * Важно сократить потребление ресурсов устройства (актуально для мобильных приложений).
     * -Когда НЕ использовать:
     * Если нужно работать с прокси, кэшированием и другими возможностями OkHttp (в таком случае лучше использовать OkHttp).
     * Если важно использование блокирующих вызовов на сервере (например, для синхронных операций).
     *
     * install(ContentNegotiation) подключает плагин для преобразования форматов данных (например, JSON).
     * плагин позволяет автоматически сериализовать и десериализовать данные между JSON и Kotlin-объектами.
     */
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // позволяет игнорировать неизвестные ключи в JSON-ответе
                prettyPrint = true // форматирует JSON в читаемом виде (удобно для отладки).
                isLenient = true // позволяет парсить JSON, даже если он не полностью соответствует спецификации.
            })
        }
    }

    /**
     * client.get — GET-запрос к серверу по адресу
     * .body<String>() — десериализует ответ сервера в строку.
     */
    suspend fun getSum(a: Double, b: Double): String {
        Log.d("@@@", "getSum() called with: a = $a, b = $b")
        return try {
            val response: String = client.get("http://10.0.2.2:8080/sum?a=$a&b=$b").body()
            Log.d("@@@", "getSum() response: $response")
            response
        } catch (e: Exception) {
            Log.e("@@@", "Error fetching sum", e)
            "Error: ${e.message}"
        }
    }

    suspend fun getSum2(a: Double, b: Double): String {
        /**
         * url {} — используется для построения URL с параметрами запроса.
         * parameters.append("a", a.toString()) — добавляет параметр a в URL.
         * parameters.append("b", b.toString()) — добавляет параметр b в URL.
         */
        return try {
            val response: String = client.get("http://10.0.2.2:8080/sum") {
                url {
                    parameters.append("a", a.toString())
                    parameters.append("b", b.toString())
                }
            }.body()
            response
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun getIP(): String {
        return try {
            client.get("http://10.0.2.2:8080/ip").body()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun getTextMessage(): String {
        return try {
            client.get("http://10.0.2.2:8080/").body()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
