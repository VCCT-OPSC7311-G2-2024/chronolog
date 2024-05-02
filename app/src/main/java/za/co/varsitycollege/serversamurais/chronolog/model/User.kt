package za.co.varsitycollege.serversamurais.chronolog.model

data class User (
    val userId: String,
    val email: String,
    var bio: String,
)