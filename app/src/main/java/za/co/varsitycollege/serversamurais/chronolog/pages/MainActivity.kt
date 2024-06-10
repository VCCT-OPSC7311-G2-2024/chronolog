package za.co.varsitycollege.serversamurais.chronolog.pages

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.navigation.NavigationView
import za.co.varsitycollege.serversamurais.chronolog.Helpers.openIntent
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.databinding.ActivityMainBinding

/**
 * MainActivity is the main entry point of the application.
 * It implements View.OnClickListener to handle click events.
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {

    /**
     * This function is called when the activity is starting.
     * It is where most initialization happens.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set click listeners for the signup and login buttons
        binding.signupNavBtn.setOnClickListener(this)
        binding.loginBtn.setOnClickListener(this)
        binding.homeSignInWithGoogleBtn.setOnClickListener(this)
    }

    /**
     * Called when a view has been clicked.
     * @param v The view that was clicked.
     */
    override fun onClick(v: View?) {
        // Determine which button was clicked and navigate to the appropriate activity
        when(v?.id) {
            R.id.homeSignInWithGoogleBtn -> openIntent(this, LoginActivity::class.java)
            R.id.signupNavBtn -> openIntent(this, SignUpActivity::class.java)
            R.id.loginBtn -> openIntent(this, LoginActivity::class.java)
        }
    }
}