package za.co.varsitycollege.serversamurais.chronolog.pages

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.Stats
import za.co.varsitycollege.serversamurais.chronolog.TimeSheet
import za.co.varsitycollege.serversamurais.chronolog.databinding.ActivityHomePageBinding

class HomePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Replace the initial fragment with HomePage
        replaceFragment(HomePage())

        // Set up bottom navigation item listener
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeNavID -> replaceFragment(HomePage())
                R.id.timesheetNavID -> replaceFragment(TimeSheet())
                R.id.statsNavID -> replaceFragment(Stats())
            }
            true // Indicate that the item selection event has been handled
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }


}
