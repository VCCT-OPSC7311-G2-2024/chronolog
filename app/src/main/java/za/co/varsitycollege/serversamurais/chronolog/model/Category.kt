package za.co.varsitycollege.serversamurais.chronolog.model

data class Category(
    val categoryId: String? = null,
    val categoryName: String? = null,
    val isActive: Boolean? = true,
    val totalHours: Int? = null,
)
