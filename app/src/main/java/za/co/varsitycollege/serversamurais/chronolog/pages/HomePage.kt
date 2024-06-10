package za.co.varsitycollege.serversamurais.chronolog.pages

import RecyclerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import android.media.MediaPlayer
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.databinding.ActivityHomeQuickActionButtonsViewBinding
import za.co.varsitycollege.serversamurais.chronolog.model.NotificationItem
import java.text.SimpleDateFormat
import java.util.*

class HomePage : Fragment(), FirebaseHelper.FirebaseOperationListener {

    // UI components
    private lateinit var textView2: TextView
    private lateinit var dailyGoalsCardView: CardView
    private lateinit var progressCardView: CardView
    private lateinit var profileCardView: CardView
    private lateinit var musicCardView: CardView
    private lateinit var minGoalEdit: EditText
    private lateinit var maxGoalEdit: EditText
    private lateinit var progressBar1Txt: TextView
    private lateinit var progressBar2Txt: TextView
    private lateinit var progressBar3Txt: TextView
    private lateinit var line1View: View
    private lateinit var line2View: View
    private lateinit var line3View: View
    private lateinit var recentActivityTaskName: TextView
    private lateinit var recentActivityTaskDate: TextView
    private lateinit var recentActivityTaskDuration: TextView
    private lateinit var recentActivityTaskDescription: TextView
    private lateinit var fullNameTxt: TextView
    private lateinit var emailTxt: TextView
    private lateinit var bioTxt: TextView
    private lateinit var musicTxt: TextView
    private lateinit var stop_startBtn: Button
    private lateinit var shuffleBtn: ImageButton

    // Firebase helper
    private lateinit var firebaseHelper: FirebaseHelper

    // Adapter for the RecyclerView
    private val data = mutableListOf<NotificationItem>()
    private lateinit var adapter: RecyclerAdapter

    private lateinit var binding: ActivityHomeQuickActionButtonsViewBinding
    private lateinit var db: DatabaseReference

    // Define constants
    private val MAX_PROGRESS_WIDTH_DP = 250 // Maximum width of the progress bar in dp
    private val MAX_GOAL_LIMIT = 200000 // Example maximum goal, adjust as needed

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_page, container, false)

        // Initialize UI components
        textView2 = view.findViewById(R.id.textView2)
        dailyGoalsCardView = view.findViewById(R.id.goalsCardView)
        progressCardView = view.findViewById(R.id.progressCardView)
        profileCardView = view.findViewById(R.id.profilecardview)
        musicCardView = view.findViewById(R.id.musicCardView)
        minGoalEdit = view.findViewById(R.id.minGoalEditText)
        maxGoalEdit = view.findViewById(R.id.maxgoalEditText)
        progressBar1Txt = view.findViewById(R.id.progress_bar1_text)
        progressBar2Txt = view.findViewById(R.id.progress_bar2_text)
        progressBar3Txt = view.findViewById(R.id.progress_bar3_text)
        line1View = view.findViewById(R.id.line1)
        line2View = view.findViewById(R.id.line2)
        line3View = view.findViewById(R.id.line3)
        recentActivityTaskName = view.findViewById(R.id.recentActivityTaskName)
        recentActivityTaskDate = view.findViewById(R.id.recentActivityTaskDate)
        recentActivityTaskDuration = view.findViewById(R.id.recentActivityTaskDuration)
        recentActivityTaskDescription = view.findViewById(R.id.recentActivityTaskDescription)
        fullNameTxt = view.findViewById(R.id.fullNameTxt)
        emailTxt = view.findViewById(R.id.emaiTxt)
        bioTxt = view.findViewById(R.id.descriptionProfileTxt)
        musicTxt = view.findViewById(R.id.musicTxtView)
        stop_startBtn = view.findViewById(R.id.Start_StopBtn)
        shuffleBtn = view.findViewById(R.id.ShuffleBtn)

        // Initialize Firebase helper
        firebaseHelper = FirebaseHelper(this)
        val userId = firebaseHelper.getUserId()
        firebaseHelper.fetchMostRecentTask(userId) { task ->
            if (task != null) {
                // Update UI with the task details
                recentActivityTaskName.text = task.name
                recentActivityTaskDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(task.date)
                recentActivityTaskDuration.text = formatTime(task.duration ?: 0)
                recentActivityTaskDescription.text = task.description
            }
        }

        // Initialize adapter
        adapter = RecyclerAdapter(requireContext(), data)

        // Initialize Firebase helper with a listener
        firebaseHelper = FirebaseHelper(object : FirebaseHelper.FirebaseOperationListener {
            override fun onSuccess(user: FirebaseUser?) {
                Toast.makeText(context, "Operation successful!", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(errorMessage: String) {
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        })

        // Update the greeting message based on the time of the day
        updateGreeting()

        // Set visibility of all CardViews to GONE
        dailyGoalsCardView.visibility = View.GONE
        progressCardView.visibility = View.GONE
        profileCardView.visibility = View.GONE
        musicCardView.visibility = View.GONE

        // Set click listeners for buttons to show respective CardViews
        view.findViewById<ImageButton>(R.id.goalsBtn).setOnClickListener {
            showDailyGoalsCard()
        }

        view.findViewById<ImageButton>(R.id.progressBtn).setOnClickListener {
            fetchAndUpdateProgressBars()
            showProgressCard()
        }

        view.findViewById<ImageButton>(R.id.profileBtn).setOnClickListener {
            showProfileCard()
            fetchUserDetails()
        }

        view.findViewById<ImageButton>(R.id.musicBtn).setOnClickListener {
            showMusicCard()
        }

        view.findViewById<Button>(R.id.saveBtn).setOnClickListener {
            updateGoals()
        }

        stop_startBtn.setOnClickListener {
            toggleMusicPlay()
        }

        shuffleBtn.setOnClickListener {
            shuffleMusic()
        }

        return view
    }

    private fun fetchAndUpdateProgressBars() {
        val userId = firebaseHelper.getUserId()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        firebaseHelper.getGoalValues(userId, currentDate) { min, max ->
            firebaseHelper.getTotalTaskHours(userId, currentDate) { actual ->
                Log.d("HomePage", "Min: $min, Max: $max, Actual: $actual") // Log the values for debugging

                val maxProgressWidthPx = dpToPx(MAX_PROGRESS_WIDTH_DP)

                // Find the largest value among min, max, and actual
                val largestValue = maxOf(min, max, actual / 60)

                // Calculate proportional widths based on the largest value
                val minWidthPx = (min.toFloat() / largestValue) * maxProgressWidthPx
                val actualWidthPx = (actual.toFloat() / (largestValue * 60)) * maxProgressWidthPx
                val maxWidthPx = (max.toFloat() / largestValue) * maxProgressWidthPx

                updateProgressBar(line1View, minWidthPx.toInt())
                updateProgressBar(line2View, actualWidthPx.toInt())
                updateProgressBar(line3View, maxWidthPx.toInt())

                progressBar1Txt.text = formatTime(min * 60) // Convert min to seconds
                progressBar2Txt.text = formatTime(actual) // Actual is already in seconds
                progressBar3Txt.text = formatTime(max * 60) // Convert max to seconds
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun updateProgressBar(view: View, targetWidth: Int) {
        val animator = ValueAnimator.ofInt(view.layoutParams.width, targetWidth)
        animator.addUpdateListener { valueAnimator ->
            val layoutParams = view.layoutParams
            layoutParams.width = valueAnimator.animatedValue as Int
            view.layoutParams = layoutParams
        }
        animator.duration = 500
        animator.start()
    }

    private fun formatTime(secondsTotal: Int): String {
        val hours = secondsTotal / 3600
        val minutes = (secondsTotal % 3600) / 60
        val seconds = secondsTotal % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


    /**
     * Updates the greeting message based on the time of the day.
     */
    private fun updateGreeting() {
        val cal = Calendar.getInstance()
        val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
        val username = firebaseHelper.getUserName().toString()

        val greeting: String = when (hourOfDay) {
            in 0..11 -> "Good morning, $username"
            in 12..17 -> "Good afternoon, $username"
            else -> "Good evening, $username"
        }

        textView2.text = greeting
    }

    private fun fetchUserDetails() {
        val userId = firebaseHelper.getUserId()

        val userEmail = firebaseHelper.getEmail()
        emailTxt.text = userEmail

        // Fetch and set the full name
        val fullName = firebaseHelper.getUserName()
        fullNameTxt.setText(fullName)

        // Fetch and set the bio
        firebaseHelper.getUserBio(userId) { bio ->
            bio?.let {
                bioTxt.setText(it)
                Log.d("HomePage", "Fetched user bio: $it")
            } ?: Log.d("HomePage", "User bio not found.")
        }
    }

    /**
     * Animates the appearance of a CardView.
     * @param cardView The CardView to be animated.
     */
    private fun animateCardView(cardView: CardView) {
        val alphaAnimator = ObjectAnimator.ofFloat(cardView, "alpha", 0f, 1f)
        alphaAnimator.duration = 500
        alphaAnimator.start()
    }

    /**
     * Updates the user's goals in Firebase and displays a success message.
     */
    private fun updateGoals() {
        val minGoal = minGoalEdit.text.toString().toInt()
        val maxGoal = maxGoalEdit.text.toString().toInt()
        val userid = firebaseHelper.getUserId()

        firebaseHelper.addNewGoalEntry(userid, minGoal, maxGoal)

        Toast.makeText(context, "Goals updated successfully!", Toast.LENGTH_SHORT).show()
    }

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentSongIndex = 0

    private val songs = listOf(
        R.raw.firstsong,
        R.raw.secondsong,
        R.raw.songthrees,
        R.raw.songfour,
        R.raw.songfive
    )

    private fun toggleMusicPlay() {
        if (isPlaying) {
            stopMusic()
        } else {
            startMusic()
        }
        isPlaying = !isPlaying
    }

    private fun startMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, songs[currentSongIndex])
            mediaPlayer?.apply {
                setOnPreparedListener {
                    start()
                    musicTxt.text = "Playing: ${resources.getResourceEntryName(songs[currentSongIndex])}"
                }
                setOnCompletionListener {
                    stopMusic()
                }
                start()
            }
        } else {
            mediaPlayer?.start()
            musicTxt.text = "Playing: ${resources.getResourceEntryName(songs[currentSongIndex])}"
        }
    }

    private fun stopMusic() {
        mediaPlayer?.pause()
        musicTxt.text = "Music stopped"
    }

    private fun shuffleMusic() {
        currentSongIndex = (songs.indices).random()
        if (isPlaying) {
            mediaPlayer?.reset()
            mediaPlayer = MediaPlayer.create(context, songs[currentSongIndex])
            startMusic()
        } else {
            musicTxt.text = "Ready to play: ${resources.getResourceEntryName(songs[currentSongIndex])}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * Shows the daily goals card and hides all other cards.
     */
    private fun showDailyGoalsCard() {
        dailyGoalsCardView.visibility = View.VISIBLE
        progressCardView.visibility = View.GONE
        profileCardView.visibility = View.GONE
        musicCardView.visibility = View.GONE
        animateCardView(dailyGoalsCardView)
    }

    /**
     * Shows the progress card and hides all other cards.
     */
    private fun showProgressCard() {
        dailyGoalsCardView.visibility = View.GONE
        progressCardView.visibility = View.VISIBLE
        profileCardView.visibility = View.GONE
        musicCardView.visibility = View.GONE
        animateCardView(progressCardView)
    }

    /**
     * Shows the profile card and hides all other cards.
     */
    private fun showProfileCard() {
        dailyGoalsCardView.visibility = View.GONE
        progressCardView.visibility = View.GONE
        profileCardView.visibility = View.VISIBLE
        musicCardView.visibility = View.GONE
        animateCardView(profileCardView)
    }

    /**
     * Shows the music card and hides all other cards.
     */
    private fun showMusicCard() {
        dailyGoalsCardView.visibility = View.GONE
        progressCardView.visibility = View.GONE
        profileCardView.visibility = View.GONE
        musicCardView.visibility = View.VISIBLE
        animateCardView(musicCardView)
    }

    /**
     * This method is called when a Firebase operation is successful.
     * @param user The current FirebaseUser or null if no user is currently authenticated.
     */
    override fun onSuccess(user: FirebaseUser?) {
        // Implementation can be added as needed
    }

    /**
     * This method is called when a Firebase operation fails.
     * @param errorMessage The error message associated with the failure.
     */
    override fun onFailure(errorMessage: String) {
        // Implementation can be added as needed
    }
}
