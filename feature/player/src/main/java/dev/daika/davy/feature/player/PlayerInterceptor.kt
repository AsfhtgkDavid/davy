package dev.daika.davy.feature.player

import android.util.Log
import dev.daika.davyparsers.PlayerData
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URI
import java.net.URLDecoder
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.component1
import kotlin.collections.component2

class PlayerInterceptor :
    Interceptor {
    @Volatile
    private var playerData: PlayerData? = null

    @Volatile
    private var headers = Headers.EMPTY

    private val activeIndex = AtomicInteger(0)

    private fun getActiveMirror(mirrors: List<HttpUrl>): HttpUrl {
        val currentMirrors = mirrors
        val idx = (activeIndex.get() and Int.MAX_VALUE) % currentMirrors.size
        return currentMirrors[idx]
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
        var request = origRequest
        var response: Response? = null
        val pathSegments = request.url.pathSegments
        val segments = pathSegments.drop(2)
        val type = pathSegments[0]
        val mirrors = if (type == "video")
            (playerData?.translations?.firstOrNull { it.isDefault }
                ?: playerData?.translations?.first())?.streams?.first { it.quality == pathSegments[1] }?.urls?.map { it.toHttpUrl() }
                ?: throw IllegalArgumentException()
        else
            listOf(playerData?.subtitles?.first {
                it.label == pathSegments[1]
            }?.src?.toHttpUrl() ?: throw IllegalArgumentException())
        var attempts = 0
        var currentTarget = getActiveMirror(mirrors)
        while (attempts < mirrors.size) {
            attempts++
            request = buildRequest(segments, currentTarget)
            try {
                response = chain.proceed(request)
                response = response.newBuilder().request(origRequest).build()
                if (response.isSuccessful)
                    return response
                if (attempts < mirrors.size)
                    response.close()
            } catch (e: IOException) {
                if (attempts >= mirrors.size) throw e
            }
            switchMirror(mirrors, currentTarget)
            currentTarget = getActiveMirror(mirrors)
        }
        return response ?: chain.proceed(origRequest)
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
            val builder = this.headers.newBuilder();
            headers.forEach { (name, value) ->
                builder[name] = value
            }
            this.headers = builder.build()
        }
    }

    fun setPlayerData(playerData: PlayerData) {
        synchronized(this) {
            this.playerData = playerData
            val builder = headers.newBuilder();
            playerData.headers.forEach { (name, value) ->
                if (builder[name] == null)
                    builder[name] = value
            }
            headers = builder.build()
        }
    }
}