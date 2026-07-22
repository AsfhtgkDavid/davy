package dev.daika.davy

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.daika.davy.data.api.YummyApi
import dev.daika.davyparsers.Parser
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient() =
        OkHttpClient.Builder().addInterceptor(UnwrappingInterceptor()).build()


    @Provides
    @Singleton
    fun provideYummyApi(okHttpClient: OkHttpClient): YummyApi =
        Retrofit.Builder().baseUrl("https://api.yani.tv/").client(okHttpClient)
            .addConverterFactory(Json {
                ignoreUnknownKeys = true
            }.asConverterFactory("application/json; charset=utf-8".toMediaType()))
            .build().create(YummyApi::class.java)

    @Provides
    @Singleton
    fun provideParsers(okHttpClient: OkHttpClient) = Parser.getAllParsers(okHttpClient)
}

private class UnwrappingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) {
            val rawJson = response.peekBody(Long.MAX_VALUE).string()
            try {
                val jsonObject = JSONObject(rawJson)
                if (jsonObject.has("response")) {
                    val unwrappedJson = jsonObject.get("response").toString()
                    val contentType = response.body.contentType()
                    return response.newBuilder().body(unwrappedJson.toResponseBody(contentType))
                        .build()
                }
            } catch (e: Exception) {
                return response
            }
        }
        return response
    }
}