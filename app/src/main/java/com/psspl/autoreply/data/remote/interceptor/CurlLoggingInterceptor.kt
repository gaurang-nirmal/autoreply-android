package com.psspl.autoreply.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Logs every outgoing REST request as a copy-ready curl command.
 *
 * Register this interceptor on the shared OkHttpClient so all Retrofit services
 * use the same logging behavior.
 */
class CurlLoggingInterceptor(
    private val tag: String = DEFAULT_TAG,
    private val redactSensitiveHeaders: Boolean = true,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Log.d(tag, request.toCurlCommand(redactSensitiveHeaders))
        return chain.proceed(request)
    }

    private fun Request.toCurlCommand(redactSensitiveHeaders: Boolean): String {
        val command = mutableListOf("curl")

        command += "-X"
        command += method.shellQuote()
        command += url.toString().shellQuote()

        headers.forEach { header ->
            val value = if (redactSensitiveHeaders && header.first.isSensitiveHeader()) {
                REDACTED_VALUE
            } else {
                header.second
            }
            command += "-H"
            command += "${header.first}: $value".shellQuote()
        }

        val body = body ?: return command.joinToString(" ")
        if (body.isPlainTextBody(headers["Content-Encoding"])) {
            val buffer = Buffer()
            body.writeTo(buffer)
            val charset = body.contentType()?.charset(DEFAULT_CHARSET) ?: DEFAULT_CHARSET
            val bodyText = buffer.readString(charset)

            if (bodyText.isNotEmpty()) {
                command += "--data-raw"
                command += bodyText.shellQuote()
            }
        } else {
            command += "--data-binary"
            command += BINARY_BODY_PLACEHOLDER.shellQuote()
        }

        return command.joinToString(" ")
    }

    private fun okhttp3.RequestBody.isPlainTextBody(contentEncoding: String?): Boolean {
        if (contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)) {
            return false
        }

        val mediaType = contentType() ?: return true
        val subtype = mediaType.subtype.lowercase()
        return mediaType.type == "text" ||
                subtype.contains("json") ||
                subtype.contains("xml") ||
                subtype.contains("form")
    }

    private fun String.isSensitiveHeader(): Boolean =
        equals("Authorization", ignoreCase = true) ||
                equals("Cookie", ignoreCase = true) ||
                equals("Set-Cookie", ignoreCase = true)

    private fun String.shellQuote(): String =
        "'${replace("'", "'\\''")}'"

    private companion object {
        const val DEFAULT_TAG = "ApiCurl"
        const val REDACTED_VALUE = "<redacted>"
        const val BINARY_BODY_PLACEHOLDER = "<binary body omitted>"
        val DEFAULT_CHARSET: Charset = StandardCharsets.UTF_8
    }
}
