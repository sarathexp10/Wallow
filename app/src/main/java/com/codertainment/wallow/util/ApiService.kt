package com.codertainment.wallow.util

import com.codertainment.wallow.BuildConfig
import com.codertainment.wallow.model.GitHubResponse
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

  companion object {

    val REPO_OWNER = "Wizper99"
    val REPO_NAME = "Wallpapers"

    var minstance: ApiService? = null

    fun getInstance(): ApiService {
      if (minstance == null) {
        minstance = create()
      }
      return minstance!!
    }

    fun create(): ApiService {
      val httpClient = OkHttpClient.Builder()
      if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        httpClient.addInterceptor(loggingInterceptor)
      }
      val client = httpClient.build()

      val retrofit = Retrofit.Builder()
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .addConverterFactory(GsonConverterFactory.create())
          .client(client)
          .baseUrl("https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/")
          .build()

      return retrofit.create(ApiService::class.java)
    }
  }

  @GET("contents/{dir}")
  fun getDirectoryContents(@Path("dir") dir: String = "",
                           @Query("client_id") clientId: String = BuildConfig.CLIENT_ID,
                           @Query("client_secret") clientSecret: String = BuildConfig.CLIENT_SECRET): Observable<List<GitHubResponse>>
}