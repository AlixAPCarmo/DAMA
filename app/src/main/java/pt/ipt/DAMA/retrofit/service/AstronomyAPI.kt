package pt.ipt.DAMA.retrofit.service

import pt.ipt.DAMA.model.astronomyAPI.AstronomyPositionResponseDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

/**
 * Interface to represent the Astronomy API
 */
interface AstronomyAPI {
    @GET("bodies/positions")
    fun getBodyPositions(
        @QueryMap params: Map<String, String>
    ): Call<AstronomyPositionResponseDTO>
}