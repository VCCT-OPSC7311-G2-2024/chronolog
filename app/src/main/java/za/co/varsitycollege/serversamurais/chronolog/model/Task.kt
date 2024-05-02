package za.co.varsitycollege.serversamurais.chronolog.model

data class Task(
    val taskId: String? = null, // Firebase can generate this ID
    val name: String = "",
    val description: String = "",
    val photoUrl: String? = null, // Optional photo URL
    val team: String = "",
    val category: String = "",
    val duration: Int = 0, // Duration in minutes
    var minGoal: Int = 0,// Minimum goal in minutes
    var maxGoal: Int = 0 // Maximum goal in minutes
)
