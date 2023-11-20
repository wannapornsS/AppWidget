package wns.freewill.appwidget

import android.annotation.SuppressLint
import androidx.core.os.BuildCompat
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException

class APIService {


    fun getService(): Service {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }


        val okHttp = getUnsafeOkHttpClient().let {
            it.addInterceptor(logging)
            it.connectTimeout(60L, TimeUnit.SECONDS)
            it.readTimeout(60L, TimeUnit.SECONDS)
            it.build()
        }

        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
                .client(okHttp)
                .baseUrl("https://us-central1-service-finder-27584.cloudfunctions.net")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(Service::class.java)
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient.Builder {

        try {
            val trustAllCert = arrayOf<TrustManager>(object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }

            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCert, java.security.SecureRandom())

            val sslSocketFactory = sslContext.socketFactory

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val builder = OkHttpClient.Builder()
            builder.addInterceptor(logging)

            builder.sslSocketFactory(sslSocketFactory, trustAllCert[0] as X509TrustManager)
            builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
            return builder
        }catch (e: Exception){
            throw RuntimeException(e)
        }
    }

    private fun getClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .readTimeout(60L, TimeUnit.SECONDS)
            .connectTimeout(60L, TimeUnit.SECONDS)
            .pingInterval(30L, TimeUnit.SECONDS)
            .sslSocketFactory(getSSLSocketFactory(), object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }).build()
    }

    @Throws(CertificateException::class)
    private fun getSSLSocketFactory(): SSLSocketFactory {
        SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<X509TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOfNulls(0)
            }), SecureRandom())
            return socketFactory
        }
    }
}