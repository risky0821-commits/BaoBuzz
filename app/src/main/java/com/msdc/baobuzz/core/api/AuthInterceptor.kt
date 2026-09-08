package com.msdc.baobuzz.core.api

import com.msdc.baobuzz.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Interceptor that adds authentication headers to direct API-Sports requests. */
class AuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header("x-apisports-key", BuildConfig.FOOTBALL_API_KEY)
            .build()

        return chain.proceed(request)
    }
}
