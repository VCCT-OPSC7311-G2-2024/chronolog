package za.co.varsitycollege.serversamurais.chronolog.views

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import za.co.varsitycollege.serversamurais.chronolog.R

class home_quickActionButtons_view : AppCompatActivity() {

    private lateinit var closeDailyGoalCardView: ImageButton
    private lateinit var openDailygoalCardview: ImageButton
    private lateinit var dailyGoalsCardView: CardView

    private lateinit var closeProgressCardView: ImageButton
    private lateinit var openProgressCardview: ImageButton
    private lateinit var progressCardView: CardView

    private lateinit var closeProfileCardView: ImageButton
    private lateinit var openProfileCardview: ImageButton
    private lateinit var profileCardView: CardView

    private lateinit var closeMusicCardView: ImageButton
    private lateinit var openMusicCardview: ImageButton
    private lateinit var musicCardView: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_quick_action_buttons_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize daily goals CardView and buttons
        openDailygoalCardview = findViewById(R.id.goalsBtn)
        dailyGoalsCardView = findViewById(R.id.goalsCardView)
        // Initialize other buttons and CardViews here



        // Initialize progress CardView and buttons
        openProgressCardview = findViewById(R.id.progressBtn)
        progressCardView = findViewById(R.id.progressCardView)
        // Initialize other buttons and CardViews here



        // Initialize profile CardView and buttons
        openProfileCardview = findViewById(R.id.profileBtn)
        profileCardView = findViewById(R.id.profilecardview)
        // Initialize other buttons and CardViews here



        // Initialize profile CardView and buttons
        openMusicCardview = findViewById(R.id.musicBtn)
        musicCardView = findViewById(R.id.musicCardView)
        // Initialize other buttons and CardViews here

        // Set visibility of all CardViews to GONE
        dailyGoalsCardView.visibility = View.GONE
        progressCardView.visibility = View.GONE
        profileCardView.visibility = View.GONE
        musicCardView.visibility = View.GONE



    }
}