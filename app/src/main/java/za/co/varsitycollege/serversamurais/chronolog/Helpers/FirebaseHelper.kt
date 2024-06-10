package za.co.varsitycollege.serversamurais.chronolog.Helpers

import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import com.google.firebase.auth.*
import com.google.firebase.database.*
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.adapters.CategoryAdapter
import za.co.varsitycollege.serversamurais.chronolog.adapters.TaskAdapter
import za.co.varsitycollege.serversamurais.chronolog.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseHelper(private val listener: FirebaseOperationListener) {

    private val database =
        FirebaseDatabase.getInstance("https://chronolog-db9b8-default-rtdb.europe-west1.firebasedatabase.app/")
    private val databaseTasksReference = database.getReference("tasks")
    private val databaseCategoriesReference = database.getReference("categories")
    private val databaseGoalsReference = database.getReference("goals")
    private val databaseTeamsReference = database.getReference("teams")
    private val databaseUsersReference = database.getReference("users")
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Interface for Firebase operation callbacks.
     */
    interface FirebaseOperationListener {
        /**
         * Called when a Firebase operation is successful.
         * @param user The current FirebaseUser or null if no user is currently authenticated.
         */
        fun onSuccess(user: FirebaseUser?)

        /**
         * Called when a Firebase operation fails.
         * @param errorMessage The error message associated with the failure.
         */
        fun onFailure(errorMessage: String)
    }

    /**
     * Returns the user ID of the currently authenticated user.
     * @return The user ID as a String.
     */
    fun getUserId(): String {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.uid.toString()
    }

    /**
     * Returns the user ID of the currently authenticated user.
     * @return The user ID as a String.
     */
    fun getEmail(): String {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.email.toString()
    }




    /**
 * Attempts to sign up a new user with the provided email and password.
 * @param email The email of the user.
 * @param password The password of the user.
 */
    fun signUp(email: String, password: String, name: String, bio: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = mAuth.currentUser
                    firebaseUser?.let {
                        // Update display name in authentication
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        it.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileUpdateTask ->
                                if (profileUpdateTask.isSuccessful) {
                                    // Save additional user details in Realtime Database
                                    val user = User(it.uid, email, name, bio)
                                    saveUserDetails(user)
                                    listener.onSuccess(it)
                                } else {
                                    listener.onFailure(profileUpdateTask.exception?.message ?: "Couldn't update display name")
                                }
                            }
                    }
                } else {
                    listener.onFailure(task.exception?.message ?: "Unknown error")
                }
            }
    }

    /**
     * Attempts to sign in a user with the provided email and password.
     * @param email The email of the user.
     * @param password The password of the user.
     */
    fun signIn(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    listener.onSuccess(user)
                } else {
                    listener.onFailure(task.exception?.message ?: "Unknown error")
                }
            }
    }

    /**
     * Saves user details to the Firebase database.
     * @param user The User object containing the user details.
     */
    fun saveUserDetails(user: User) {
        val userId = user.userId
        val databaseUsersReference = database.getReference("users")

        databaseUsersReference.child(userId).setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = mAuth.currentUser
                    listener.onSuccess(firebaseUser)
                } else {
                    listener.onFailure(task.exception?.message ?: "Unknown error")
                }
            }
    }

    /**
     * Signs out the currently authenticated user.
     */
    fun signOut() {
        mAuth.signOut()
        listener.onSuccess(null)
    }

    /**
     * Adds a new task to the Firebase database.
     * @param newTask The task to be added.
     * @param userId The ID of the user who is adding the task.
     */
    fun addTask(newTask: Task, userId: String) {
        databaseTasksReference.child(userId).child(newTask.taskId).setValue(newTask)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    listener.onSuccess(user)
                } else {
                    listener.onFailure(task.exception?.message ?: "Unknown error")
                }
            }
    }

    /**
     * Fetches tasks from the Firebase database and updates the provided list and adapter.
     * @param userId The ID of the user whose tasks are to be fetched.
     * @param tasks The list to be updated with the fetched tasks.
     * @param taskAdapter The adapter to be notified of data changes.
     */
    fun fetchTasks(userId: String, tasks: MutableList<Task>, taskAdapter: TaskAdapter) {
        databaseTasksReference.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tasks.clear()
                dataSnapshot.children.mapNotNullTo(tasks) { it.getValue(Task::class.java) }
                taskAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseHelper", "Failed to load tasks.", databaseError.toException())
            }
        })
    }

    /**
     * Fetches tasks from a specific category from the Firebase database and updates the provided list and adapter.
     * @param userId The ID of the user whose tasks are to be fetched.
     * @param tasks The list to be updated with the fetched tasks.
     * @param categoryAdapter The adapter to be notified of data changes.
     */
    fun fetchCategoryTasks(
        userId: String,
        tasks: MutableList<Task>,
        categoryAdapter: CategoryAdapter
    ) {
        databaseTasksReference.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tasks.clear()
                dataSnapshot.children.mapNotNullTo(tasks) { it.getValue(Task::class.java) }
                categoryAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseHelper", "Failed to load tasks.", databaseError.toException())
            }
        })
    }

    /**
     * Generates a unique ID for a new task.
     * @param userId The ID of the user who is adding the task.
     * @return The generated task ID.
     */
    fun getTaskId(userId: String): String {
        return databaseTasksReference.child(userId).child("tasks").push().key
            ?: throw Exception("Failed to generate unique key for task")
    }

    /**
     * Updates the duration of a specific task in the Firebase database.
     * @param taskId The ID of the task to be updated.
     * @param userId The ID of the user who owns the task.
     * @param updates A map containing the new duration values.
     */
    fun updateTaskDuration(taskId: String, userId: String, updates: Map<String, Int?>) {
        databaseTasksReference.child(userId).child(taskId).updateChildren(updates)
            .addOnSuccessListener {
                Log.d("FirebaseHelper", "Task duration updated successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseHelper", "Failed to update task duration.", e)
            }
    }

    /**
     * Fetches tasks from the Firebase database and passes them to the provided callback.
     * @param userId The ID of the user whose tasks are to be fetched.
     * @param onTasksReceived The callback to be invoked with the fetched tasks.
     */
    fun fetchTasks(userId: String, onTasksReceived: (List<Task>) -> Unit) {
        databaseTasksReference.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = snapshot.children.mapNotNull { it.getValue(Task::class.java) }
                    onTasksReceived(tasks)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseHelper", "Error fetching tasks: ${error.message}")
                }
            })
    }

    /**
     * Fetches the most recent task from the Firebase database and passes it to the provided callback.
     * @param userId The ID of the user whose tasks are to be fetched.
     * @param onTaskReceived The callback to be invoked with the most recent task.
     */
    fun fetchMostRecentTask(userId: String, onTaskReceived: (Task?) -> Unit) {
        databaseTasksReference.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = snapshot.children.mapNotNull { it.getValue(Task::class.java) }
                    val mostRecentTask = tasks.maxByOrNull { it.date?.time ?: Long.MIN_VALUE }
                    onTaskReceived(mostRecentTask)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseHelper", "Error fetching tasks: ${error.message}")
                    onTaskReceived(null) // Handle cancellation or errors by providing null
                }
            })
    }

    /**
     * Adds a new category to the Firebase database.
     * @param newCategory The category to be added.
     * @param userId The ID of the user who is adding the category.
     */
    fun addCategoryToFirebase(newCategory: Category, userId: String) {
        val categoryId = databaseCategoriesReference.push().key
            ?: throw Exception("Failed to generate unique key for category") // Generate a unique key for the category
        databaseCategoriesReference.child(userId).child(categoryId).setValue(newCategory)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    listener.onSuccess(user)
                } else {
                    listener.onFailure(task.exception?.message ?: "Unknown Error")
                }
            }
    }

    /**
     * Fetches categories from the Firebase database and updates the provided list and adapter.
     * @param userId The ID of the user whose categories are to be fetched.
     * @param categories The list to be updated with the fetched categories.
     * @param adapter The adapter to be notified of data changes.
     */
    fun fetchCategories(
        userId: String, categories: MutableList<String>, adapter: ArrayAdapter<String>
    ) {
        databaseCategoriesReference.child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    categories.clear()
                    for (snapshot in dataSnapshot.children) {
                        val category = snapshot.getValue(Category::class.java)
                        category?.let { categories.add(it.categoryName.toString()) }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("MainActivity", "Failed to read categories.", databaseError.toException())
                }
            })
    }

    /**
     * Adds a new team to the Firebase database.
     * @param newTeam The team to be added.
     * @param userId The ID of the user who is adding the team.
     */
    fun addTeamToFirebase(newTeam: Team, userId: String) {
        val teamId = databaseTeamsReference.push().key
            ?: throw Exception("Failed to generate unique key for team") // Generate a unique key for the team

        databaseTeamsReference.child(userId).child(teamId).setValue(newTeam)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    listener.onSuccess(user)
                } else {
                    listener.onFailure(task.exception?.message ?: "Unknown Error")
                }
            }
    }

    /**
     * Fetches teams from the Firebase database and updates the provided list and adapter.
     * @param userId The ID of the user whose teams are to be fetched.
     * @param teams The list to be updated with the fetched teams.
     * @param adapter The adapter to be notified of data changes.
     */
    fun fetchTeams(
        userId: String, teams: MutableList<String>, adapter: ArrayAdapter<String>
    ) {
        databaseTeamsReference.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                teams.clear()
                for (snapshot in dataSnapshot.children) {
                    val team = snapshot.getValue(Team::class.java)
                    team?.let { teams.add(it.teamName.toString()) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MainActivity", "Failed to read teams.", databaseError.toException())
            }
        })
    }

    /**
     * Updates the password of the currently authenticated user.
     * @param oldPassword The current password of the user.
     * @param newPassword The new password.
     */
    fun updateUserPassword(oldPassword: String, newPassword: String) {
        val user = mAuth.currentUser
        val credential = EmailAuthProvider.getCredential(user?.email!!, oldPassword)

        user.reauthenticate(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                listener.onSuccess(user)
                            } else {
                                listener.onFailure(
                                    updateTask.exception?.message ?: "Couldn't update password"
                                )
                            }
                        }
                } else {
                    listener.onFailure(task.exception?.message ?: "Re-authentication failed")
                }
            }
    }

    fun sendEmailVerification(newEmail: String, oldPassword: String) {
        val user = mAuth.currentUser
        if (user == null) {
            listener.onFailure("User is not authenticated.")
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    Log.d("FirebaseHelper", "Re-authentication successful")

                    user.verifyBeforeUpdateEmail(newEmail)
                        .addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                Log.d("FirebaseHelper", "Verification email sent successfully")
                                listener.onSuccess(user)
                            } else {
                                val exceptionMessage = verifyTask.exception?.message ?: "Couldn't send verification email"
                                Log.e("FirebaseHelper", "Verification email failed: $exceptionMessage")
                                listener.onFailure(exceptionMessage)
                            }
                        }
                } else {
                    val exceptionMessage = reauthTask.exception?.message ?: "Re-authentication failed"
                    Log.e("FirebaseHelper", "Re-authentication failed: $exceptionMessage")
                    listener.onFailure(exceptionMessage)
                }
            }
    }


    /**
     * Updates the full name of the currently authenticated user.
     * @param fullName The new full name.
     */
    fun updateFullName(fullName: String) {
        val user = mAuth.currentUser

        user?.let {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()

            it.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        listener.onSuccess(it)
                    } else {
                        listener.onFailure(task.exception?.message ?: "Couldn't update full name")
                    }
                }
        }
    }

    /**
     * Returns the currently authenticated user.
     * @return The current FirebaseUser or null if no user is currently authenticated.
     */
    fun getCurrentUser(): FirebaseUser? {
        return mAuth.currentUser
    }

    /**
     * Returns the full name of the currently authenticated user.
     * @return The full name as a String.
     */
    fun getUserName(): String? {
        return mAuth.currentUser?.displayName
    }

    /**
     * Updates tasks with non-zero goals in the Firebase database.
     * @param userId The ID of the user who owns the tasks.
     * @param newMinGoal The new minimum goal.
     * @param newMaxGoal The new maximum goal.
     */

    fun addNewGoalEntry(userId: String, minGoal: Int, maxGoal: Int) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val userGoalsReference = databaseGoalsReference.child(userId)

        userGoalsReference.orderByChild("date").equalTo(currentDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Goal for the current date exists, update it
                        for (goalSnapshot in snapshot.children) {
                            val goalId = goalSnapshot.key
                            if (goalId != null) {
                                val updatedGoal = Goal(
                                    minGoal = minGoal,
                                    maxGoal = maxGoal,
                                    date = currentDate
                                )
                                userGoalsReference.child(goalId).setValue(updatedGoal)
                                    .addOnSuccessListener {
                                        Log.d("FirebaseHelper", "Goal updated successfully")
                                    }
                                    .addOnFailureListener { error ->
                                        Log.e("FirebaseHelper", "Error updating goal: ${error.message}")
                                    }
                            }
                        }
                    } else {
                        // No goal for the current date, create a new one
                        val goalId = userGoalsReference.push().key
                            ?: throw Exception("Failed to generate unique key for goal")

                        val newGoal = Goal(
                            minGoal = minGoal,
                            maxGoal = maxGoal,
                            date = currentDate
                        )

                        userGoalsReference.child(goalId).setValue(newGoal)
                            .addOnSuccessListener {
                                Log.d("FirebaseHelper", "New goal added successfully")
                            }
                            .addOnFailureListener { error ->
                                Log.e("FirebaseHelper", "Error adding new goal: ${error.message}")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseHelper", "Error checking existing goals: ${error.message}")
                }
            })
    }


    fun getGoalValuesForLastMonth(userId: String, startDate: String, endDate: String, callback: (List<Int>, List<Int>) -> Unit) {
        val goalsRef = databaseGoalsReference.child(userId)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startTimestamp = sdf.parse(startDate)?.time ?: 0
        val endTimestamp = sdf.parse(endDate)?.time ?: Long.MAX_VALUE

        goalsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val minGoals = mutableListOf<Int>()
                val maxGoals = mutableListOf<Int>()
                for (goalSnapshot in snapshot.children) {
                    val goal = goalSnapshot.getValue(Goal::class.java)
                    if (goal != null && goal.date != null) {
                        val goalDateTimestamp = sdf.parse(goal.date)?.time
                        if (goalDateTimestamp != null && goalDateTimestamp in startTimestamp..endTimestamp) {
                            minGoals.add(goal.minGoal)
                            maxGoals.add(goal.maxGoal)
                        }
                    }
                }
                callback(minGoals, maxGoals)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Error fetching goals: ${error.message}")
                callback(emptyList(), emptyList())
            }
        })
    }

    fun getTotalTaskHoursForLastMonth(userId: String, startDate: Long, endDate: Long, callback: (List<Int>) -> Unit) {
        val tasksRef = databaseTasksReference.child(userId)
        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val taskDurations = mutableListOf<Int>()
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    if (task != null && task.date != null) {
                        val taskDateTimestamp = task.date.time
                        if (taskDateTimestamp in startDate..endDate) {
                            taskDurations.add(task.duration)
                        }
                    }
                }
                callback(taskDurations)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Error fetching tasks: ${error.message}")
                callback(emptyList())
            }
        })
    }




    fun getTotalTaskHours(userId: String, currentDate: String, callback: (Int) -> Unit) {
        val tasksRef = databaseTasksReference.child(userId)
        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalDuration = 0
                var count = 0
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    Log.d("FirebaseHelper", "Fetched Task: ${task?.date} ${task?.duration}")
                    if (task != null && isSameDay(task.date, currentDate)) {
                        totalDuration += task.duration ?: 0
                    }
                }
                Log.d("FirebaseHelper", "Total Task Hours for $currentDate: $totalDuration")
                callback(totalDuration)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Error fetching tasks: ${error.message}")
                callback(0)
            }
        })
    }

    private fun isSameDay(date: Date?, dateString: String): Boolean {
        if (date == null) return false
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(date) == dateString
    }


    fun fetchGoals(userId: String, startDate: Long, endDate: Long, callback: (List<Goal>) -> Unit) {
        val databaseGoalsReference = database.getReference("goals")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        databaseGoalsReference.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val goals = mutableListOf<Goal>()
                    for (goalSnapshot in snapshot.children) {
                        val goal = goalSnapshot.getValue(Goal::class.java)
                        goal?.let {
                            val goalDate = dateFormat.parse(it.date)?.time
                            if (goalDate != null && goalDate in startDate..endDate) {
                                goals.add(goal)
                            }
                        }
                    }
                    Log.d("FirebaseHelper", "Fetched goals: $goals")
                    callback(goals)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseHelper", "Error fetching goals: ${error.message}")
                    callback(emptyList())
                }
            })
    }







    /**
     * Fetches the total duration of all tasks from the Firebase database and passes it to the provided callback.
     * @param userId The ID of the user whose tasks are to be fetched.
     * @param onTotalDurationReceived The callback to be invoked with the total duration.
     */
    fun getTotalDuration(userId: String, onTotalDurationReceived: (Int) -> Unit) {
        databaseTasksReference.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val totalDuration = snapshot.children
                        .map { it.getValue(Task::class.java) }
                        .filterNotNull()
                        .sumBy { it.duration }
                    onTotalDurationReceived(totalDuration)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseHelper", "Error fetching tasks: ${error.message}")
                }
            })
    }

    /**
     * Fetches the minimum goal of all tasks from the Firebase database and passes it to the provided callback.
     * @param userId The ID of the user whose tasks are to be fetched.
     * @param onMinGoalReceived The callback to be invoked with the minimum goal.
     */
    fun getMinGoal(userId: String, onMinGoalReceived: (Int) -> Unit) {
        databaseTasksReference.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var minGoal = Int.MAX_VALUE
                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        if (task != null && task.minGoal < minGoal) {
                            minGoal = task.minGoal
                        }
                    }
                    onMinGoalReceived(minGoal)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseHelper", "Error fetching tasks: ${error.message}")
                }
            })
    }

    /**
     * Fetches the maximum goal of all tasks from the Firebase database and passes it to the provided callback.
     * @param userId The ID of the user whose tasks are to be fetched.
     * @param onMaxGoalReceived The callback to be invoked with the maximum goal.
     */
    fun getMaxGoal(userId: String, onMaxGoalReceived: (Int) -> Unit) {
        databaseTasksReference.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var maxGoal = Int.MIN_VALUE
                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        if (task != null && task.maxGoal > maxGoal) {
                            maxGoal = task.maxGoal
                        }
                    }
                    onMaxGoalReceived(maxGoal)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseHelper", "Error fetching tasks: ${error.message}")
                }
            })
    }

    fun getUserBio(userId: String, onBioReceived: (String?) -> Unit) {
        databaseUsersReference.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bio = snapshot.child("bio").getValue(String::class.java)
                    onBioReceived(bio)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseHelper", "Error fetching bio: ${error.message}")
                    onBioReceived(null)
                }
            })
    }
}