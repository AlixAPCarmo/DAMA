package pt.ipt.DAMA.retrofit.service

import pt.ipt.DAMA.model.API.ChatGptRequest
import pt.ipt.DAMA.model.API.ChatGptResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAiApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    fun generateCompletion(@Body request: ChatGptRequest): Call<ChatGptResponse>
}
