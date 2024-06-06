package za.co.varsitycollege.serversamurais.chronolog.pages

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.Helpers.openIntent
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.databinding.ActivitySignUpBinding

/**
 * Activity for handling user sign up.
 */
class SignUpActivity : AppCompatActivity(), View.OnClickListener, FirebaseHelper.FirebaseOperationListener {
    private lateinit var firebaseHelper: FirebaseHelper

    /**
     * Called when the activity is starting.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySignUpBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.alreadyHaveAnAccBtn.setOnClickListener(this)

        firebaseHelper = FirebaseHelper(this)

        // Set click listener for sign up button
        binding.signupBtn.setOnClickListener {
            if(binding.editPassword.text.toString().trim() == binding.editConfirmPasword.text.toString().trim()){
                val email = binding.editEmail.text.toString().trim()
                val password = binding.editPassword.text.toString().trim()
                firebaseHelper.signUp(email, password)
            }
        }
    }

    /**
     * Called when a Firebase operation is successful.
     * @param user The current FirebaseUser or null if no user is currently authenticated.
     */
    override fun onSuccess(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, "Register successful", Toast.LENGTH_SHORT).show()
            // Navigate to main activity or dashboard
            openIntent(this, LoginActivity::class.java)
        } else {
            // Handle sign out scenario if needed
            Toast.makeText(this, "Register unsuccessful", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Called when a Firebase operation fails.
     * @param errorMessage The error message associated with the failure.
     */
    override fun onFailure(errorMessage: String) {
        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    /**
     * Called when a view has been clicked.
     * @param v The view that was clicked.
     */
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.alreadyHaveAnAccBtn -> openIntent(this, LoginActivity::class.java)
        }
    }
}