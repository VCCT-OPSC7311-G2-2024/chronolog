package za.co.varsitycollege.serversamurais.chronolog.Helpers

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import za.co.varsitycollege.serversamurais.chronolog.model.Task

class FirebaseHelper(private val listener: FirebaseOperationListener) {

    private val database = Firebase.database("https://chronolog-db9b8-default-rtdb.europe-west1.firebasedatabase.app/")
    private val databaseReference = database.getReference("tasks")
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
        val taskId = databaseReference.child(userId).push().key ?: throw Exception("Failed to generate unique key for task")
        databaseReference.child(userId).child(taskId).setValue(newTask)
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
        databaseReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = snapshot.children.mapNotNull { it.getValue(Task::class.java) }
                onTasksReceived(tasks)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(Exception(error.message))
            }
        })
    }
}