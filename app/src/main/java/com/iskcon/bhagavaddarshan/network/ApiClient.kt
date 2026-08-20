package com.iskcon.bhagavaddarshan.network

import com.iskcon.bhagavaddarshan.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiClient(
    baseUrl: String = BuildConfig.API_BASE_URL,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    private val baseUrl = baseUrl.trim().trimEnd('/')

    fun get(path: String, token: String? = null): JSONObject =
        execute(
            Request.Builder()
                .url(url(path))
                .apply { bearer(token) }
                .get()
                .build()
        )

    fun post(path: String, json: JSONObject, token: String? = null): JSONObject =
        execute(
            Request.Builder()
                .url(url(path))
                .apply { bearer(token) }
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()
        )

    fun put(path: String, json: JSONObject, token: String? = null): JSONObject =
        execute(
            Request.Builder()
                .url(url(path))
                .apply { bearer(token) }
                .put(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()
        )

    fun delete(path: String, token: String? = null): JSONObject =
        execute(
            Request.Builder()
                .url(url(path))
                .apply { bearer(token) }
                .delete()
                .build()
        )

    fun patch(path: String, json: JSONObject, token: String? = null): JSONObject =
        execute(
            Request.Builder()
                .url(url(path))
                .apply { bearer(token) }
                .patch(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()
        )

    fun parseJsonObject(body: String): JSONObject =
        if (body.isBlank()) JSONObject() else JSONObject(body)

    private fun execute(request: Request): JSONObject =
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching {
                    if (body.contains("<html", ignoreCase = true)) {
                        "Server is temporarily unavailable"
                    } else {
                        val error = parseJsonObject(body)
                        error.optString("message").ifBlank { error.optString("error") }
                    }
                }.getOrDefault("").ifBlank { body.take(180) }
                throw ApiException(response.code, message.ifBlank { response.message })
            }
            parseJsonObject(body)
        }

    private fun url(path: String): String = "$baseUrl/${path.trimStart('/')}"

    private fun Request.Builder.bearer(token: String?) {
        if (!token.isNullOrBlank()) header("Authorization", "Bearer $token")
        header("Accept", "application/json")
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

class ApiException(
    val statusCode: Int,
    override val message: String
) : IOException("HTTP $statusCode: $message")
