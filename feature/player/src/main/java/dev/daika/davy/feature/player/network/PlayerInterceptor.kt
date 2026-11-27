package dev.daika.davy.feature.player.network

import android.util.Log
import dev.daika.davyparsers.PlayerData
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class PlayerInterceptor(private val updateData: () -> PlayerData) :
    Interceptor {
    @Volatile
    private var playerData: PlayerData? = null

    @Volatile
    private var headers = Headers.EMPTY

    private val activeIndex = AtomicInteger(0)

    private fun getActiveMirror(mirrors: List<HttpUrl>): HttpUrl {
        val idx = (activeIndex.get() and Int.MAX_VALUE) % mirrors.size
        return mirrors[idx]
    }

    private fun switchMirror(mirrors: List<HttpUrl>, failedMirror: HttpUrl) {
        if (mirrors.isEmpty()) return

        val active = getActiveMirror(mirrors)
        if (active == failedMirror) {
            activeIndex.incrementAndGet()
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val origRequest = chain.request()
        if (origRequest.url.host != "davy") {
            Log.w("PlayerInterceptor", "Request to ${origRequest.url.host}")
            return chain.proceed(origRequest.newBuilder().headers(headers).build())
        }

        val pathSegments = origRequest.url.pathSegments
        if (pathSegments.size < 2) {
            throw IOException("Invalid internal davy URL: ${origRequest.url}")
        }
        val type = pathSegments[0]
        val qualityOrLabel = pathSegments[1]
        val segments = pathSegments.drop(2)

        val currentData = playerData ?: refreshPlayerData(null)
        val initialMirrors = resolveMirrors(currentData, type, qualityOrLabel)
        if (initialMirrors.isNotEmpty()) {
            val firstAttempt = tryMirrors(chain, origRequest, segments, initialMirrors)
            if (firstAttempt.response?.isSuccessful == true) {
                return firstAttempt.response
            }

            firstAttempt.response?.close()
        }

        val updatedData = refreshPlayerData(currentData)
        val updatedMirrors = resolveMirrors(updatedData, type, qualityOrLabel)
        if (updatedMirrors.isEmpty()) {
            throw IOException("No mirrors found for $type / $qualityOrLabel after refresh")
        }

        val fallbackAttempt = tryMirrors(chain, origRequest, segments, updatedMirrors)
        if (fallbackAttempt.response != null) {
            return fallbackAttempt.response
        }

        throw fallbackAttempt.lastException ?: IOException("All mirrors failed after data update")
    }

    private fun resolveMirrors(
        data: PlayerData,
        type: String,
        qualityOrLabel: String
    ): List<HttpUrl> {
        return if (type == "video") {
            val translation = data.translations.firstOrNull { it.isDefault }
                ?: data.translations.firstOrNull()
                ?: return emptyList()
            val stream = translation.streams.firstOrNull { it.quality == qualityOrLabel }
                ?: translation.streams.firstOrNull()
                ?: return emptyList()
            stream.urls.map { it.toHttpUrl() }
        } else {
            val subtitle = data.subtitles.firstOrNull { it.label == qualityOrLabel }
                ?: return emptyList()
            listOf(subtitle.src.toHttpUrl())
        }
    }

    private fun tryMirrors(
        chain: Interceptor.Chain,
        origRequest: Request,
        segments: List<String>,
        mirrors: List<HttpUrl>
    ): AttemptResult {
        if (mirrors.isEmpty()) return AttemptResult(null, IOException("Mirrors list is empty"))
        var attempts = 0
        var currentTarget = getActiveMirror(mirrors)
        var lastResponse: Response? = null
        var lastException: IOException? = null

        while (attempts < mirrors.size) {
            attempts++
            val request = buildRequest(segments, currentTarget)
            try {
                lastResponse?.close()
                val response = chain.proceed(request)
                lastResponse = response.newBuilder().request(origRequest).build()
                if (lastResponse.isSuccessful) {
                    return AttemptResult(lastResponse, null)
                }
            } catch (e: IOException) {
                lastException = e
                lastResponse = null
            }
            switchMirror(mirrors, currentTarget)
            currentTarget = getActiveMirror(mirrors)
        }

        return AttemptResult(lastResponse, lastException)
    }

    private fun refreshPlayerData(staleData: PlayerData?): PlayerData {
        synchronized(this) {
            val current = playerData
            if (current != null && current !== staleData) {
                return current
            }
            val newData = updateData()
            setPlayerData(newData)
            activeIndex.set(0)
            return newData
        }
    }

    private fun buildRequest(segments: List<String>, url: HttpUrl): Request =
        Request.Builder().url(
            url.newBuilder().removePathSegment(url.pathSegments.size - 1)
                .apply { segments.forEach { addPathSegment(it) } }
                .build()
        ).headers(headers)
            .get().build()

    fun updateHeaders(headers: Map<String, String>) {
        synchronized(this) {
            val builder = this.headers.newBuilder()
            headers.forEach { (name, value) ->
                builder[name] = value
            }
            this.headers = builder.build()
        }
    }

    fun setPlayerData(playerData: PlayerData) {
        synchronized(this) {
            this.playerData = playerData
            val builder = headers.newBuilder()
            playerData.headers.forEach { (name, value) ->
                if (builder[name] == null)
                    builder[name] = value
            }
            headers = builder.build()
        }
    }

    private data class AttemptResult(
        val response: Response?,
        val lastException: IOException?
    )
}