package za.co.varsitycollege.serversamurais.chronolog.pages

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
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

class SignUpActivity : AppCompatActivity(), View.OnClickListener, FirebaseHelper.FirebaseOperationListener {
    private lateinit var firebaseHelper: FirebaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySignUpBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.alreadyHaveAnAccBtn.setOnClickListener(this)

        firebaseHelper = FirebaseHelper(this)

        binding.signupBtn.setOnClickListener {
            if(binding.editPassword.text.toString().trim() == binding.editConfirmPasword.text.toString().trim()){
                val email = binding.editEmail.text.toString().trim()
                val password = binding.editPassword.text.toString().trim()
                firebaseHelper.signUp(email, password)
            }
        }
    }

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

    override fun onFailure(errorMessage: String) {
        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.alreadyHaveAnAccBtn -> openIntent(this, LoginActivity::class.java)

        }
    }


}