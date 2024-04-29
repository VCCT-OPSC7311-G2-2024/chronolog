package za.co.varsitycollege.serversamurais.chronolog.pages

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import za.co.varsitycollege.serversamurais.chronolog.Helpers.openIntent
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySignUpBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.alreadyHaveAnAccBtn.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.alreadyHaveAnAccBtn -> openIntent(this, LoginActivity::class.java)
        }
    }


}