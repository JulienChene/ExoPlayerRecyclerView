package io.snaker.app.video.helper

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.HttpHeaders
import okio.Buffer
import timber.log.Timber
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class LoggingInterceptor(val level: Level) : Interceptor{
    enum class Level {
        /** No logs.  */
        NONE,
        /**
         * Logs request and response lines.
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
        `</pre> *
         */
        BASIC,
        /**
         * Logs request and response lines and their respective headers.
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
        `</pre> *
         */
        HEADERS,
        /**
         * Logs request and response lines and their respective bodies (if present).
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
        `</pre> *
         */
        BODY,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
        `</pre> *
         */
        BODY_AND_HEADERS
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        if (level == Level.NONE) {
            return chain.proceed(request)
        }

        val logBody = level == Level.BODY || level == Level.BODY_AND_HEADERS
        val logHeaders = level == Level.HEADERS || level == Level.BODY_AND_HEADERS

        val requestBody = request.body()
        val requestMethod = request.method()
        val hasRequestBody = requestBody != null
        val requestContentLength = requestBody?.contentLength() ?: -1L

        val connection = chain.connection()
        val requestUrl = request.url().toString()
        val urlProtocol = if (requestUrl.contains("://")) requestUrl.substringBefore("://") else "http"
        val connectionProtocol = connection?.protocol()?.toString()
        val protocol = (connectionProtocol ?: urlProtocol).toUpperCase()
        var requestStartMessage = "--> $protocol $requestMethod $requestUrl"
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " ($requestContentLength-byte body)"
        }
        Timber.tag(REQUEST).d(requestStartMessage)

        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody?.contentType() != null) {
                    Timber.tag(REQUEST_HEADER).d("Content-Type: ${requestBody.contentType()}")
                }
                if (requestContentLength != -1L) {
                    Timber.tag(REQUEST_HEADER).d("Content-Length: $requestContentLength")
                }
            }

            val headers = request.headers()
            var i = 0
            val count = headers.size()
            while (i < count) {
                val name = headers.name(i)
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equals(name, ignoreCase = true) && !"Content-Length".equals(name, ignoreCase = true)) {
                    Timber.tag(REQUEST_HEADER).d("$name: ${headers.value(i)}")
                }
                i++
            }
        }

        if (hasRequestBody && logBody && !bodyHasUnknownEncoding(request.headers())) {
            val buffer = Buffer()
            requestBody!!.writeTo(buffer)

            val contentType = requestBody.contentType()
            val charset: Charset? = if (contentType != null) {
                contentType.charset(UTF8)
            } else {
                UTF8
            }

            Timber.tag(REQUEST_BODY).d("")
            if (isPlaintext(buffer)) {
                Timber.tag(REQUEST_BODY).d(buffer.readString(charset!!))
            }
        }

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Timber.tag(RESPONSE).e("<-- HTTP FAILED: ${e.message}")
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body()
        val responseContentLength = responseBody?.contentLength() ?: -1L
        val bodySize = if (responseContentLength != -1L) responseContentLength.toString() + "-byte" else null
        val responseCode = response.code()
        val responseLog = "$requestUrl (${tookMs}ms${if (!logHeaders && bodySize != null) ", $bodySize body" else ""})"
        when (responseCode) {
            in 500..599 -> Timber.tag(RESPONSE).e("<-- $protocol $responseCode $responseLog")
            in 400..499 -> Timber.tag(RESPONSE).e("<-- $protocol $responseCode $responseLog")
            else -> Timber.tag(RESPONSE).d("<-- $protocol $responseCode $responseLog")
        }

        val responseHeaders = response.headers()
        if (logHeaders) {
            var i = 0
            val count = responseHeaders.size()
            while (i < count) {
                val responseHeaderLine = "${responseHeaders.name(i)}: ${responseHeaders.value(i)}"
                when (responseCode) {
                    in 500..599 -> Timber.tag(RESPONSE_HEADER).e(responseHeaderLine)
                    in 400..499 -> Timber.tag(RESPONSE_HEADER).e(responseHeaderLine)
                    else -> Timber.tag(RESPONSE_HEADER).d(responseHeaderLine)
                }
                i++
            }
        }

        if (responseBody != null && HttpHeaders.hasBody(response) && logBody && !bodyHasUnknownEncoding(responseHeaders)) {
            val source = responseBody.source()
            source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer()

            val contentType = responseBody.contentType()
            val charset: Charset? = if (contentType != null) {
                contentType.charset(UTF8)
            } else {
                UTF8
            }

            if (isPlaintext(buffer) && responseContentLength != 0L) {
                charset?.let {
                    val responseBodyText = buffer.clone().readString(it)
                    when (responseCode) {
                        in 500..599 -> Timber.tag(RESPONSE_BODY).e(responseBodyText)
                        in 400..499 -> Timber.tag(RESPONSE_BODY).e(responseBodyText)
                        else -> Timber.tag(RESPONSE_BODY).d(responseBodyText)
                    }
                }
            }
        }

        return response
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers.get("Content-Encoding")
        return (contentEncoding != null
                && !contentEncoding.equals("identity", ignoreCase = true)
                && !contentEncoding.equals("gzip", ignoreCase = true))
    }

    companion object {

        private const val REQUEST = "Request"
        private const val RESPONSE = "Response"
        private const val REQUEST_BODY = "RequestBody"
        private const val REQUEST_HEADER = "RequestHeader"
        private const val RESPONSE_BODY = "ResponseBody"
        private const val RESPONSE_HEADER = "ResponseHeader"

        private val UTF8 = Charset.forName("UTF-8")

        /**
         * Returns true if the body in question probably contains human readable text. Uses a small sample
         * of code points to detect unicode control characters commonly used in binary file signatures.
         */
        internal fun isPlaintext(buffer: Buffer): Boolean {
            try {
                val prefix = Buffer()
                val byteCount = if (buffer.size() < 64) buffer.size() else 64
                buffer.copyTo(prefix, 0, byteCount)
                for (i in 0..15) {
                    if (prefix.exhausted()) {
                        break
                    }
                    val codePoint = prefix.readUtf8CodePoint()
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false
                    }
                }
                return true
            } catch (e: EOFException) {
                return false // Truncated UTF-8 sequence.
            }

        }
    }
}
