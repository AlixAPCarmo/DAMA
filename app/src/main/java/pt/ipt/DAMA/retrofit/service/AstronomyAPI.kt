package pt.ipt.DAMA.retrofit.service

import pt.vapa.myfirstar.DTO.AstronomyAPI.AstronomyPositionResponseDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface AstronomyAPI {
    @GET("bodies/positions")
    fun getBodyPositions(
        @QueryMap params: Map<String, String>
    ): Call<AstronomyPositionResponseDTO>
}