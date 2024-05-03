package za.co.varsitycollege.serversamurais.chronolog.model

import java.time.LocalDate
import java.util.Date

data class Task(
    val taskId: String? = null, // Firebase can generate this ID
    val name: String? = null,
    val description: String? = null,
    val photoUrl: String? = null, // Optional photo URL
    val team: String? = null,
    val category: String? = null,
    val duration: Int? = null,
    val date: Date? = null// Duration in minutes
)
