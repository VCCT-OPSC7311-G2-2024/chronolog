package za.co.varsitycollege.serversamurais.chronolog.pages

import RecyclerAdapter
import android.animation.ObjectAnimator
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
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.databinding.ActivityHomeQuickActionButtonsViewBinding
import za.co.varsitycollege.serversamurais.chronolog.model.NotificationItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


/**
 * HomePage is a Fragment that represents the home page of the application.
 * It contains methods for displaying and updating user progress, daily goals, and recent activities.
 */
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

    // Firebase helper
    private lateinit var firebaseHelper: FirebaseHelper

    // Adapter for the RecyclerView
    private val data = mutableListOf<NotificationItem>()
    private lateinit var adapter: RecyclerAdapter

    private lateinit var binding: ActivityHomeQuickActionButtonsViewBinding
    private lateinit var db: DatabaseReference

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
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
       // fetchUserName()

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
            showUserProgress()
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

        return view
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
     * Shows the user's progress in the progress CardView.
     */
    private fun showUserProgress() {
        val userId = firebaseHelper.getUserId()

        organizeDurationData(userId)
        organizeMinGoalData(userId)
        organizeMaxGoalData(userId)
    }

    /**
     * Calculates the width of the progress bar based on the goal.
     * @param goal The goal to be used for the calculation.
     * @return The width of the progress bar.
     */
    private fun calculateProgressWidth(goal: Int): Int {
        val maxProgressWidth = 250 // Maximum width of the progress bar in dp
        val percentage = (goal.toFloat() / 100) * 100
        return (maxProgressWidth * (percentage / 100)).toInt()
    }

    /**
     * Converts dp to pixels.
     * @param dp The value in dp to be converted to pixels.
     * @return The value in pixels.
     */
    private fun dpToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    /**
     * Organizes the duration data and updates the UI.
     * @param userId The ID of the user whose data is to be organized.
     */
    private fun organizeDurationData(userId: String) {
        firebaseHelper.getTotalDuration(userId) { totalDuration ->
            val totalHours = totalDuration.toFloat()

            // Calculate the percentage of total hours compared to the maximum
            val percentage = (totalHours / 100) * 100
            var progressWidth = (250 * (percentage / 100)).toInt()

            // Constrain the progress width to be at most 250dp
            progressWidth = progressWidth.coerceAtMost(dpToPx(250))

            // Set the width of progressBar2 dynamically
            val params = line2View.layoutParams
            params.width = progressWidth
            line2View.layoutParams = params

            progressBar2Txt.text = (totalDuration / 60).toString() + " minutes"
           // bioTxt.text = (totalDuration / 60).toString() + " minutes"
        }
    }

    /**
     * Organizes the minimum goal data and updates the UI.
     * @param userId The ID of the user whose data is to be organized.
     */
    private fun organizeMinGoalData(userId: String) {
        firebaseHelper.getMinGoal(userId) { minGoal ->
            // Calculate progress for progressBar1
            val minProgressWidth = calculateProgressWidth(minGoal)

            // Set the width of progressBar1 dynamically
            val params = line1View.layoutParams
            params.width = minProgressWidth.coerceAtMost(dpToPx(250))
            line1View.layoutParams = params

            progressBar1Txt.text = minGoal.toString() + " minutes"
        }
    }

    /**
     * Organizes the maximum goal data and updates the UI.
     * @param userId The ID of the user whose data is to be organized.
     */
    private fun organizeMaxGoalData(userId: String) {
        firebaseHelper.getMaxGoal(userId) { maxGoal ->
            val maxProgressWidth = calculateProgressWidth(maxGoal)

            // Set the width of progressBar3 dynamically
            val params = line3View.layoutParams
            params.width = maxProgressWidth.coerceAtMost(dpToPx(250))
            line3View.layoutParams = params

            progressBar3Txt.text = maxGoal.toString() + " minutes"
        }
    }

    /**
     * Formats the time in seconds to a string in the format "HH:mm:ss".
     * @param secondsTotal The total time in seconds.
     * @return The formatted time string.
     */
    private fun formatTime(secondsTotal: Int): String {
        val hours = secondsTotal / 3600
        val minutes = (secondsTotal % 3600) / 60
        val seconds = secondsTotal % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


    /**
     * Updates the user's goals in Firebase and displays a success message.
     */
    private fun updateGoals()
    {
        val minGoal = minGoalEdit.text.toString().toInt()
        val maxGoal = maxGoalEdit.text.toString().toInt()
        val userid = firebaseHelper.getUserId()

        firebaseHelper.updateTasksWithNonZeroGoals(userid, minGoal, maxGoal)

        Toast.makeText(context, "Goals updated successfully!", Toast.LENGTH_SHORT).show()
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
        TODO("Not yet implemented")
    }

    /**
     * This method is called when a Firebase operation fails.
     * @param errorMessage The error message associated with the failure.
     */
    override fun onFailure(errorMessage: String) {
        TODO("Not yet implemented")
    }
}