package pt.ipt.DAMA.DTO

// data class for Register activity
data class UserRegister(
    val nome: String,
    val email: String,
    val password: String,
)

// data class for Login request activity
data class UserLoginRequest(
    val email: String,
    val password: String
)


// data class to handle user login response
data class UserLoginResponse(
    val token: String,
    val userName: String
)