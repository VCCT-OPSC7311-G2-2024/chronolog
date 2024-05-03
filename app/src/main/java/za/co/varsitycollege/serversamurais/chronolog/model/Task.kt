package za.co.varsitycollege.serversamurais.chronolog.model

import java.time.LocalDate
import java.util.Date

data class Task(
    val taskId: String = "", // Firebase can generate this ID
    val name: String? = null,
    val description: String? = null,
    val photoUrl: String? = null, // Optional photo URL
    val team: String? = null,
    val category: String? = null,
    var duration: Int? = null,
    val date: Date? = null,
    var isRunning: Boolean = false,
    var minGoal: Int = 0,// Minimum goal in minutes
    var maxGoal: Int = 0 // Maximum goal in minutes

)
