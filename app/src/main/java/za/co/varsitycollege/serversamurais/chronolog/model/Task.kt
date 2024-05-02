package za.co.varsitycollege.serversamurais.chronolog.model

import java.util.Date

data class Task(
    val taskId: String? = "", // Firebase can generate this ID
    val name: String = "",
    val description: String = "",
    val photoUrl: String? = "", // Optional photo URL
    val team: String = "",
    val category: String = "",
    val duration: Int = 0,
    val date: String? = ""// Duration in minutes
)
