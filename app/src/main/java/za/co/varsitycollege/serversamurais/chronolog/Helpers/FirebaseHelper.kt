package za.co.varsitycollege.serversamurais.chronolog.Helpers

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import za.co.varsitycollege.serversamurais.chronolog.model.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import za.co.varsitycollege.serversamurais.chronolog.adapters.CategoryAdapter
import za.co.varsitycollege.serversamurais.chronolog.model.Category



class FirebaseHelper(private val listener: FirebaseOperationListener) {

    private val database = Firebase.database("https://chronolog-db9b8-default-rtdb.europe-west1.firebasedatabase.app/")
    private val databaseTasksReference = database.getReference("tasks")
    private val databaseCategoriesReference = database.getReference("categories")
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    interface FirebaseOperationListener {
        fun onSuccess(user: FirebaseUser?)
        fun onFailure(errorMessage: String)
    }

    fun getUserId(): String {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.uid.toString()
    }

    fun signUp(email: String, password: String){
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    val user = mAuth.currentUser
                    listener.onSuccess(user)
                } else {
                    listener.onFailure(task.exception?.message ?: "Unknown error")
                }
            }
    }

    fun signIn(email: String, password: String){
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val user = mAuth.currentUser
                    listener.onSuccess(user)
                } else {
                    listener.onFailure(task.exception?.message ?: "Unknown error")
                }
            }
    }

    fun signOut() {
        mAuth.signOut()
        listener.onSuccess(null)
    }




    fun addTask(newTask: Task, userId: String) {

        val taskId = databaseTasksReference.child(userId).push().key ?: throw Exception("Failed to generate unique key for task")

        databaseTasksReference.child(userId).child(taskId).setValue(newTask)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val user = mAuth.currentUser
                    listener.onSuccess(user)
                } else {
                    listener.onFailure(task.exception?.message ?: "Unknown error")
                }
            }
    }
    fun fetchTasks(userId: String, onTasksReceived: (List<Task>) -> Unit, onError: (Exception) -> Unit) {
        databaseTasksReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = snapshot.children.mapNotNull { it.getValue(Task::class.java) }
                onTasksReceived(tasks)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(Exception(error.message))
            }
        })
    }

    fun fetchTasks(userId: String, onTasksReceived: (List<Task>) -> Unit) {
    databaseTasksReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val tasks = snapshot.children.mapNotNull { it.getValue(Task::class.java) }
            onTasksReceived(tasks)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("FirebaseHelper", "Error fetching tasks: ${error.message}")
        }
    })
}

    fun addCategoryToFirebase(newCategory: Category, userId: String) {

        val categoryId = databaseCategoriesReference.push().key ?: throw Exception("Failed to generate unique key for category") // Generate a unique key for the category

        databaseCategoriesReference.child(userId).child(categoryId).setValue(newCategory)
            .addOnCompleteListener {task ->
                if(task.isSuccessful) {
                    val user = mAuth.currentUser
                    listener.onSuccess(user)
                } else {
                    listener.onFailure(task.exception?.message ?: "Unknown Error")
                }
            }
    }

    fun fetchCategories(userId: String, categories: ArrayList<Category>, adapter: CategoryAdapter) {
        // Firebase reference

        databaseCategoriesReference.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                categories.clear()
                for (snapshot in dataSnapshot.children) {
                    val category = snapshot.getValue(Category::class.java)
                    category?.let { categories.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MainActivity", "Failed to read categories.", databaseError.toException())
            }

        })
    }

    fun updateUserPassword(newPassword: String) {
        mAuth.currentUser?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    listener.onSuccess(user)
                } else {
                    listener.onFailure(task.exception?.message ?: "Couldn't update password")
                }
            }
    }

    fun updateFullName(fullName: String) {
        val user = mAuth.currentUser

        user?.let {
            // Log user details before the update operation
            Log.d("FirebaseHelper", "User details before update: Email - ${it.email}, Full Name - ${it.displayName}")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()

            it.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Log user details after the update operation
                        Log.d("FirebaseHelper", "User details after update: Email - ${it.email}, Full Name - ${it.displayName}")
                        listener.onSuccess(it)
                    } else {
                        Log.e("FirebaseHelper", "Failed to update full name: ${task.exception?.message ?: "Unknown error"}")
                        listener.onFailure(task.exception?.message ?: "Couldn't update full name")
                    }
                }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return mAuth.currentUser
    }

    fun getUserName(): String? {
        return mAuth.currentUser?.displayName
    }

    fun updateTasksWithNonZeroGoals(userId: String, newMinGoal: Int, newMaxGoal: Int) {
        databaseTasksReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    if (task != null && task.minGoal > 0 && task.maxGoal > 0) {
                        task.minGoal = newMinGoal
                        task.maxGoal = newMaxGoal
                        taskSnapshot.ref.setValue(task)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Error updating tasks: ${error.message}")
            }
        })
    }

    fun getTotalDuration(userId: String, onTotalDurationReceived: (Int) -> Unit) {
        databaseTasksReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
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


    fun getMinGoal(userId: String, onMinGoalReceived: (Int) -> Unit) {
    databaseTasksReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
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

fun getMaxGoal(userId: String, onMaxGoalReceived: (Int) -> Unit) {
    databaseTasksReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
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






}
