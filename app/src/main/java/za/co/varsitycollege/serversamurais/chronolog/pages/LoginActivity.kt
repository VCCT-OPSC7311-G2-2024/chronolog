package za.co.varsitycollege.serversamurais.chronolog.pages

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.Helpers.openIntent
import za.co.varsitycollege.serversamurais.chronolog.Helpers.replaceFragment
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity(), View.OnClickListener,
    FirebaseHelper.FirebaseOperationListener {

    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUpForAccBtn.setOnClickListener(this)

        firebaseHelper = FirebaseHelper(this)


        binding.loginBtn.setOnClickListener {

            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()
            firebaseHelper.signIn(email, password)

        }


    }

    override fun onSuccess(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
            // Navigate to main activity or dashboard
            replaceFragment(this, HomePage())

        } else {
            // Handle sign out scenario if needed
        }
    }

    override fun onFailure(errorMessage: String) {
        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
    }


    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.signUpForAccBtn -> openIntent(this, SignUpActivity::class.java)
        }
    }

}