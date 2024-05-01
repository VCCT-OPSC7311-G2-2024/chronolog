package za.co.varsitycollege.serversamurais.chronolog.Helpers

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class FirebaseHelper(private val listener: FirebaseOperationListener) {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    interface FirebaseOperationListener {
        fun onSuccess(user: FirebaseUser?)
        fun onFailure(errorMessage: String)
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


}