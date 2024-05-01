package za.co.varsitycollege.serversamurais.chronolog.model

data class Team(
    val teamId: String,
    val name: String,
    val members: List<String> // List of user IDs
)
