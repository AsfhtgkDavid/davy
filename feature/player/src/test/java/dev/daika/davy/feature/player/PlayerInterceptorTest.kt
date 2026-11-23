package dev.daika.davy.feature.player

import dev.daika.davy.feature.player.network.PlayerInterceptor
import dev.daika.davyparsers.MediaStream
import dev.daika.davyparsers.PlayerData
import dev.daika.davyparsers.SubtitleTrack
import dev.daika.davyparsers.Translation
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class PlayerInterceptorTest {
    private fun createSamplePlayerData(
        videoMirrors: List<String> = listOf(
            "https://mirror1.com/stream/index.m3u8",
            "https://mirror2.com/stream/index.m3u8"
        ),
        subtitleUrl: String = "https://subs.example.com/mk/sub.vtt",
        headers: Map<String, String> = mapOf(
            "X-Initial" to "InitValue",
            "Token" to "old",
            "Authorization" to "Bearer token"
        )
    ): PlayerData {
        return PlayerData(
            headers = headers,
            translations = listOf(
                Translation(
                    id = "Dub",
                    name = "Name",
                    isDefault = true,
                    streams = listOf(
                        MediaStream(
                            quality = "1080",
                            urls = videoMirrors
                        )
                    )
                )
            ),
            subtitles = listOf(
                SubtitleTrack(
                    label = "Mockс",
                    src = subtitleUrl,
                    isDefault = true,
                    kind = "subtitle",
                    language = "mk"
                )
            )
        )
    }

    private fun successfulResponse(request: Request): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("OK".toResponseBody("text/plain".toMediaType()))
            .build()
    }

    private fun errorResponse(request: Request, code: Int = 500): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("Internal Server Error")
            .body("Error".toResponseBody("text/plain".toMediaType()))
            .build()
    }

    @Test
    fun `video request redirects to active mirror with appended segment and headers`() {
        val updateData = mockk<() -> PlayerData>()
        val interceptor = PlayerInterceptor(updateData).apply {
            setPlayerData(createSamplePlayerData())
        }

        val chain = mockk<Interceptor.Chain>()
        val requestSlot = slot<Request>()
        val origRequest = Request.Builder()
            .url("https://davy/video/1080/index.m3u8")
            .build()

        every { chain.request() } returns origRequest
        every { chain.proceed(capture(requestSlot)) } answers {
            successfulResponse(requestSlot.captured)
        }

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        val rewrittenUrl = requestSlot.captured.url.toString()
        assertEquals("https://mirror1.com/stream/index.m3u8", rewrittenUrl)
        assertEquals("Bearer token", requestSlot.captured.header("Authorization"))
        assertEquals("InitValue", requestSlot.captured.header("X-Initial"))
        verify(exactly = 0) { updateData.invoke() }
    }

    @Test
    fun `fetches initial playerData via updateData when playerData is null`() {
        val updateData = mockk<() -> PlayerData>()
        every { updateData.invoke() } returns createSamplePlayerData()

        val interceptor = PlayerInterceptor(updateData)
        val chain = mockk<Interceptor.Chain>()
        val requestSlot = slot<Request>()
        val origRequest = Request.Builder()
            .url("https://davy/video/1080/index.m3u8")
            .build()

        every { chain.request() } returns origRequest
        every { chain.proceed(capture(requestSlot)) } answers {
            successfulResponse(requestSlot.captured)
        }

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals("https://mirror1.com/stream/index.m3u8", requestSlot.captured.url.toString())
        verify(exactly = 1) { updateData.invoke() }
    }

    @Test
    fun `video request falls back to second mirror when first mirror fails with HTTP error`() {
        val updateData = mockk<() -> PlayerData>()
        val interceptor = PlayerInterceptor(updateData).apply {
            setPlayerData(createSamplePlayerData())
        }

        val chain = mockk<Interceptor.Chain>()
        val requests = mutableListOf<Request>()
        val origRequest = Request.Builder()
            .url("https://davy/video/1080/index.m3u8")
            .build()

        every { chain.request() } returns origRequest
        every { chain.proceed(any()) } answers {
            val req = firstArg<Request>()
            requests.add(req)
            if (req.url.host == "mirror1.com") {
                errorResponse(req, 503)
            } else {
                successfulResponse(req)
            }
        }

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals(2, requests.size)
        assertEquals("mirror1.com", requests[0].url.host)
        assertEquals("mirror2.com", requests[1].url.host)
        verify(exactly = 0) { updateData.invoke() }
    }

    @Test
    fun `calls updateData and retries on fresh mirrors when all initial mirrors fail with HTTP error`() {
        val initialData = createSamplePlayerData(
            videoMirrors = listOf(
                "https://stale1.com/stream/index.m3u8",
                "https://stale2.com/stream/index.m3u8"
            )
        )
        val freshData = createSamplePlayerData(
            videoMirrors = listOf("https://fresh1.com/stream/index.m3u8")
        )

        val updateData = mockk<() -> PlayerData>()
        every { updateData.invoke() } returns freshData

        val interceptor = PlayerInterceptor(updateData).apply {
            setPlayerData(initialData)
        }

        val chain = mockk<Interceptor.Chain>()
        val requests = mutableListOf<Request>()
        val origRequest = Request.Builder()
            .url("https://davy/video/1080/index.m3u8")
            .build()

        every { chain.request() } returns origRequest
        every { chain.proceed(any()) } answers {
            val req = firstArg<Request>()
            requests.add(req)
            if (req.url.host == "fresh1.com") {
                successfulResponse(req)
            } else {
                errorResponse(req, 403)
            }
        }

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals(
            listOf("stale1.com", "stale2.com", "fresh1.com"),
            requests.map { it.url.host }
        )
        verify(exactly = 1) { updateData.invoke() }
    }

    @Test
    fun `calls updateData and retries on fresh mirrors when all initial mirrors throw IOException`() {
        val initialData = createSamplePlayerData(
            videoMirrors = listOf("https://stale1.com/stream/index.m3u8")
        )
        val freshData = createSamplePlayerData(
            videoMirrors = listOf("https://fresh1.com/stream/index.m3u8")
        )

        val updateData = mockk<() -> PlayerData>()
        every { updateData.invoke() } returns freshData

        val interceptor = PlayerInterceptor(updateData).apply {
            setPlayerData(initialData)
        }

        val chain = mockk<Interceptor.Chain>()
        val origRequest = Request.Builder()
            .url("https://davy/video/1080/index.m3u8")
            .build()

        every { chain.request() } returns origRequest
        every { chain.proceed(any()) } answers {
            val req = firstArg<Request>()
            if (req.url.host == "fresh1.com") {
                successfulResponse(req)
            } else {
                throw IOException("Connection reset")
            }
        }

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        verify(exactly = 1) { updateData.invoke() }
    }

    @Test(expected = IOException::class)
    fun `video request throws IOException when both initial and refreshed mirrors fail with network exception`() {
        val updateData = mockk<() -> PlayerData>()
        every { updateData.invoke() } returns createSamplePlayerData(
            videoMirrors = listOf("https://fresh1.com/stream/index.m3u8")
        )

        val interceptor = PlayerInterceptor(updateData).apply {
            setPlayerData(createSamplePlayerData())
        }

        val chain = mockk<Interceptor.Chain>()
        val origRequest = Request.Builder()
            .url("https://davy/video/1080/index.m3u8")
            .build()

        every { chain.request() } returns origRequest
        every { chain.proceed(any()) } throws IOException("Connection timed out")

        interceptor.intercept(chain)
    }

    @Test
    fun `subtitles request correctly decodes label and rewrites destination URL`() {
        val interceptor = PlayerInterceptor { createSamplePlayerData() }.apply {
            setPlayerData(createSamplePlayerData())
        }

        val chain = mockk<Interceptor.Chain>()
        val requestSlot = slot<Request>()
        val origRequest = Request.Builder()
            .url("https://davy/sub/Mock%D1%81/sub.vtt")
            .build()

        every { chain.request() } returns origRequest
        every { chain.proceed(capture(requestSlot)) } answers {
            successfulResponse(requestSlot.captured)
        }

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals("https://subs.example.com/mk/sub.vtt", requestSlot.captured.url.toString())
    }

    @Test
    fun `setPlayerData not overrides existing headers`() {
        val interceptor = PlayerInterceptor { createSamplePlayerData() }.apply {
            updateHeaders(mapOf("Token" to "new"))
            setPlayerData(createSamplePlayerData())
        }

        val chain = mockk<Interceptor.Chain>()
        val requestSlot = slot<Request>()
        val origRequest = Request.Builder()
            .url("https://davy/video/1080/index.m3u8")
            .build()

        every { chain.request() } returns origRequest
        every { chain.proceed(capture(requestSlot)) } answers {
            successfulResponse(requestSlot.captured)
        }

        interceptor.intercept(chain)

        assertEquals("new", requestSlot.captured.header("Token"))
    }

    @Test
    fun `updateHeaders overrides existing headers`() {
        val interceptor = PlayerInterceptor { createSamplePlayerData() }.apply {
            setPlayerData(createSamplePlayerData())
            updateHeaders(mapOf("Token" to "new"))
        }

        val chain = mockk<Interceptor.Chain>()
        val requestSlot = slot<Request>()
        val origRequest = Request.Builder()
            .url("https://davy/video/1080/index.m3u8")
            .build()

        every { chain.request() } returns origRequest
        every { chain.proceed(capture(requestSlot)) } answers {
            successfulResponse(requestSlot.captured)
        }

        interceptor.intercept(chain)

        assertEquals("new", requestSlot.captured.header("Token"))
    }
}