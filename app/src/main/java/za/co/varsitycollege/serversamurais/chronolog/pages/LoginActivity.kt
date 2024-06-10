package za.co.varsitycollege.serversamurais.chronolog.pages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.Helpers.openIntent
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity(), View.OnClickListener, FirebaseHelper.FirebaseOperationListener {

    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUpForAccBtn.setOnClickListener(this)

        firebaseHelper = FirebaseHelper(this)
        firebaseAuth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set click listener for login button
        binding.loginBtn.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()
            firebaseHelper.signIn(email, password)
        }

        // Set click listener for Google Sign-In button
        binding.signInWithGoogleBtn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("LoginActivity", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("LoginActivity", "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LoginActivity", "signInWithCredential:success")
                    val user = firebaseAuth.currentUser
                    onSuccess(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    onFailure("Authentication Failed.")
                }
            }
    }

    override fun onSuccess(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
            // Navigate to main activity or dashboard
            openIntent(this, HomePageActivity::class.java)
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
