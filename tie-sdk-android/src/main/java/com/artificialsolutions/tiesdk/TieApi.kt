package com.artificialsolutions.tiesdk

import com.artificialsolutions.tiesdk.jsonconverter.TieApiModelJsonConverter
import com.artificialsolutions.tiesdk.model.TieCloseSessionResponse
import com.artificialsolutions.tiesdk.model.TieResponse
import io.reactivex.Observable
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.Retrofit
import retrofit2.http.*

internal interface TieApi {

    @POST()
    @FormUrlEncoded
    fun sendInput(@Url() url : String,
                  @Header("cookie") sessionId: String?,
                  @Field("userinput") userInput: String,
                  @FieldMap() parameters: Map<String, String>,
                  @Field("viewtype") viewType: String):
            Observable<TieResponse>

    @POST()
    fun close(@Url() url : String,
                @Header("cookie") sessionId: String): Observable<TieCloseSessionResponse>

    companion object {
        fun create(engineUrl : String): TieApi {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(
                            RxJava2CallAdapterFactory.create())
                    .addConverterFactory(
                            TieApiModelJsonConverter.buildGsonConverter())
                    .baseUrl(engineUrl)
                    .build()

            return retrofit.create(TieApi::class.java)
        }
    }
}
