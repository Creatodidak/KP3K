package id.creatodidak.kp3k.api
import android.util.Log
import id.creatodidak.kp3k.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

import retrofit2.converter.gson.GsonConverterFactory

object Client {

    private const val BASE_URL = BuildConfig.BASE_URL

    val retrofit: Retrofit by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("API_REQUEST", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
}
