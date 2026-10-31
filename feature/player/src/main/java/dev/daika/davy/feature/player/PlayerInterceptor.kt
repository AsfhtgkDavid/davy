package dev.daika.davy.feature.player

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
        var request = origRequest
        var response: Response? = null
        val pathSegments = request.url.pathSegments.reversed()
        val segment = pathSegments.first()
        val type = pathSegments[2]
        val mirrors = if (type == "video")
            playerData?.translations?.first { it.isDefault }?.streams?.first { it.quality == pathSegments[1] }?.urls?.map { it.toHttpUrl() }
                ?: throw IllegalArgumentException()
        else
            listOf(playerData?.subtitles?.first {
                it.label == URLDecoder.decode(
                    pathSegments[1],
                    "UTF-8"
                )
            }?.src?.toHttpUrl() ?: throw IllegalArgumentException())
        var attempts = 0
        var currentTarget = getActiveMirror(mirrors)
        while (attempts < mirrors.size) {
            attempts++
            request = buildRequest(segment, currentTarget)
            try {
                response = chain.proceed(request)
                response = response.newBuilder().request(origRequest).build()
                if (response.isSuccessful)
                    return response
                response.close()
            } catch (e: IOException) {
                if (attempts >= mirrors.size) throw e
            }
            switchMirror(mirrors, currentTarget)
            currentTarget = getActiveMirror(mirrors)
        }
        return response ?: chain.proceed(origRequest)
    }

    private fun buildRequest(segment: String, url: HttpUrl): Request =
        Request.Builder().url(url.newBuilder().addPathSegment(segment).build()).headers(headers)
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