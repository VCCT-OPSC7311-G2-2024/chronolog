package za.co.varsitycollege.serversamurais.chronolog.pages

import RecyclerAdapter
import SharedViewModel
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
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.NotificationPage
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.SettingsPage
import za.co.varsitycollege.serversamurais.chronolog.model.NotificationItem
import za.co.varsitycollege.serversamurais.chronolog.model.Task
import java.util.Calendar

class HomePage : Fragment() {

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

    private lateinit var firebaseHelper: FirebaseHelper

    // Define and initialize adapter
    private val data = mutableListOf<NotificationItem>()
    private lateinit var adapter: RecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home_page, container, false)

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



        // Initialize adapter
        adapter = RecyclerAdapter(requireContext(), data)

        firebaseHelper = FirebaseHelper(object : FirebaseHelper.FirebaseOperationListener {
            override fun onSuccess(user: FirebaseUser?) {
                Toast.makeText(context, "Operation successful!", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(errorMessage: String) {
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        })

        // Call the method to update the greeting message based on the time of the day
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
            showUserProgress()
            showProgressCard()
        }

        view.findViewById<ImageButton>(R.id.profileBtn).setOnClickListener {
            showProfileCard()
        }

        view.findViewById<ImageButton>(R.id.musicBtn).setOnClickListener {
            showMusicCard()
        }

        view.findViewById<Button>(R.id.saveBtn).setOnClickListener {
            updateGoals()
        }
        return view
    }

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



    private fun animateCardView(cardView: CardView) {
        val alphaAnimator = ObjectAnimator.ofFloat(cardView, "alpha", 0f, 1f)
        alphaAnimator.duration = 500
        alphaAnimator.start()
    }

    private fun showUserProgress() {
        val userId = firebaseHelper.getUserId()

        organizeDurationData(userId)
        organizeMinGoalData(userId)
        organizeMaxGoalData(userId)
    }

    private fun calculateProgressWidth(goal: Int): Int {
        val maxProgressWidth = 250 // Maximum width of the progress bar in dp
        val percentage = (goal.toFloat() / 100) * 100
        return (maxProgressWidth * (percentage / 100)).toInt()
    }

    /**
     * Convert dp to pixels.
     */
    private fun dpToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
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

            progressBar2Txt.text = totalHours.toInt().toString()
        }
    }
    private fun organizeMinGoalData(userId: String) {
        firebaseHelper.getMinGoal(userId) { minGoal ->
            // Calculate progress for progressBar1
            val minProgressWidth = calculateProgressWidth(minGoal)

            // Set the width of progressBar1 dynamically
            val params = line1View.layoutParams
            params.width = minProgressWidth.coerceAtMost(dpToPx(250))
            line1View.layoutParams = params

            progressBar1Txt.text = minGoal.toString()
        }
    }

    private fun organizeMaxGoalData(userId: String) {
        firebaseHelper.getMaxGoal(userId) { maxGoal ->
            val maxProgressWidth = calculateProgressWidth(maxGoal)

            // Set the width of progressBar3 dynamically
            val params = line3View.layoutParams
            params.width = maxProgressWidth.coerceAtMost(dpToPx(250))
            line3View.layoutParams = params

            progressBar3Txt.text = maxGoal.toString()
        }
    }

    private fun updateGoals()
    {
        val minGoal = minGoalEdit.text.toString().toInt()
        val maxGoal = maxGoalEdit.text.toString().toInt()
        val userid = firebaseHelper.getUserId()

        firebaseHelper.updateTasksWithNonZeroGoals(userid, minGoal, maxGoal)

        Toast.makeText(context, "Goals updated successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun showDailyGoalsCard() {
        dailyGoalsCardView.visibility = View.VISIBLE
        progressCardView.visibility = View.GONE
        profileCardView.visibility = View.GONE
        musicCardView.visibility = View.GONE
        animateCardView(dailyGoalsCardView)
    }

    private fun showProgressCard() {
        dailyGoalsCardView.visibility = View.GONE
        progressCardView.visibility = View.VISIBLE
        profileCardView.visibility = View.GONE
        musicCardView.visibility = View.GONE
        animateCardView(progressCardView)
    }

    private fun showProfileCard() {
        dailyGoalsCardView.visibility = View.GONE
        progressCardView.visibility = View.GONE
        profileCardView.visibility = View.VISIBLE
        musicCardView.visibility = View.GONE
        animateCardView(profileCardView)
    }

    private fun showMusicCard() {
        dailyGoalsCardView.visibility = View.GONE
        progressCardView.visibility = View.GONE
        profileCardView.visibility = View.GONE
        musicCardView.visibility = View.VISIBLE
        animateCardView(musicCardView)
    }
}