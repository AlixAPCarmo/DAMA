package pt.ipt.DAMA.model.API

data class ChatGptMessage(
    val role: String,
    val content: String
)

data class ChatGptRequest(
    val model: String,
    val messages: List<ChatGptMessage>
)

data class ChatGptResponse(
    val choices: List<Choice>
) {
    data class Choice(
        val message: ChatGptMessage
    )
}
