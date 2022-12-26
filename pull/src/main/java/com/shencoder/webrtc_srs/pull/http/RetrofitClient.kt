package com.shencoder.webrtc_srs.pull.http

import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.http.BaseRetrofitClient
import com.shencoder.webrtc_srs.pull.BuildConfig
import com.shencoder.webrtc_srs.pull.constant.Constant
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.regex.Pattern
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 *
 * @author  ShenBen
 * @date    2021/8/19 12:38
 * @email   714081644@qq.com
 */

class RetrofitClient : BaseRetrofitClient() {

    private companion object {
//        private const val BASE_URL =
//            "https://${Constant.SRS_SERVER_HTTPS}"
        private const val BASE_URL =
            "http://${Constant.SRS_SERVER_HTTP}"
    }

//    val apiService by lazy {
//        getApiService(
//            ApiService::class.java,
//            BASE_URL
//        )
//    }

    fun getApiService(url:String):ApiService{
        return getApiService(ApiService::class.java,"http://${getIps(url)[0]}:${Constant.SRS_SERVER_HTTP_PORT}")
    }

    /**
     * 正则提前字符串中的IP地址
     *
     * @param ipString
     * @return
     */
    private fun getIps(ipString: String?): List<String> {
        val regEx = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)"
        val ips: MutableList<String> = ArrayList()
        val p = Pattern.compile(regEx)
        val m = p.matcher(ipString)
        while (m.find()) {
            val result = m.group()
            ips.add(result)
        }
        return ips
    }

    override fun generateOkHttpBuilder(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        val xtm: X509TrustManager = object : X509TrustManager {

            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

        }

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf<TrustManager>(xtm), SecureRandom())
        val hostnameVerifier = HostnameVerifier { hostname, session -> true }

        val interceptor = HttpLoggingInterceptor { message -> XLog.i(message) }
        interceptor.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        return builder.addInterceptor(interceptor)
            .sslSocketFactory(sslContext.socketFactory)
            .hostnameVerifier(hostnameVerifier)
    }

    override fun generateRetrofitBuilder(builder: Retrofit.Builder): Retrofit.Builder {
        return builder.apply {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            addConverterFactory(MoshiConverterFactory.create(moshi))
        }

    }
}