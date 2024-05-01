package za.co.varsitycollege.serversamurais.chronolog.model

data class Category(
    val categoryId: String,
    val name: String,
    val description: String? = null
)
