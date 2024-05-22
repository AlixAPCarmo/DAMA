package pt.ipt.DAMA.retrofit.service

import pt.ipt.DAMA.model.pexelsImageAPI.ImageResponseDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ImageAPI {
    @Headers("Authorization: Jasdk0aFfBv15F2mgUqm4iGkUzYcOA6dGcBbSd9jCFzSF9TXvng57AdR")
    @GET("search")
    fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 1
    ): Call<ImageResponseDTO>
}