/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.realworld.android.petsave.core.data.di

import com.appmattus.certificatetransparency.certificateTransparencyInterceptor
import com.realworld.android.petsave.core.data.api.ApiConstants
import com.realworld.android.petsave.core.data.api.PetFinderApi
import com.realworld.android.petsave.core.data.api.interceptors.AuthenticationInterceptor
import com.realworld.android.petsave.core.data.api.interceptors.LoggingInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

  @Provides
  @Singleton
  fun provideApi(okHttpClient: OkHttpClient): PetFinderApi {
    return Retrofit.Builder()
        .baseUrl(ApiConstants.BASE_ENDPOINT)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(PetFinderApi::class.java)
  }

  @Provides
  fun provideOkHttpClient(
      httpLoggingInterceptor: HttpLoggingInterceptor,
      authenticationInterceptor: AuthenticationInterceptor
  ): OkHttpClient {

    //TODO: Add pinning for versions lower than M
    val hostname = "**.petfinder.com" //Two asterisks matches any number of subdomains
    val certificatePinner = CertificatePinner.Builder()
        .add(hostname, "sha256/U8zLlKBQLcRpbcte+Y0kpfoe0pMz+ABQqhAdPlPtf7M=")
        .add(hostname, "sha256/JSMzqOOrtyOT1kmau6zKhgT676hGgczD5VMdRMyJZFA=")
        .build()

    //TODO: Add certificate transparency here
    val ctInterceptor = certificateTransparencyInterceptor {
      // Enable for the provided hosts
      +"*.petfinder.com" //1 For subdomains
      +"petfinder.com" //2 asterisk does not cover base domain
      //+"*.*" - this will add all hosts
      //-"legacy.petfinder.com" //3 Exclude specific hosts
    }

    return OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .addNetworkInterceptor(ctInterceptor)
        .addInterceptor(authenticationInterceptor)
        .addInterceptor(httpLoggingInterceptor)
        .cache(null)
        .build()
  }

  @Provides
  fun provideHttpLoggingInterceptor(loggingInterceptor: LoggingInterceptor): HttpLoggingInterceptor {
    val interceptor = HttpLoggingInterceptor(loggingInterceptor)

    interceptor.level = HttpLoggingInterceptor.Level.BODY

    return interceptor
  }
}