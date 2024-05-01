package za.co.varsitycollege.serversamurais.chronolog.pages

import RecyclerAdapter
import SharedViewModel
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import za.co.varsitycollege.serversamurais.chronolog.NotificationPage
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.SettingsPage
import za.co.varsitycollege.serversamurais.chronolog.model.NotificationItem
import java.util.Calendar

class HomePage : Fragment() {
    private lateinit var textView2: TextView
    private lateinit var dailyGoalsCardView: CardView
    private lateinit var progressCardView: CardView
    private lateinit var profileCardView: CardView
    private lateinit var musicCardView: CardView

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

        // Initialize adapter
        adapter = RecyclerAdapter(requireContext(), data)

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
            showProgressCard()
        }

        view.findViewById<ImageButton>(R.id.profileBtn).setOnClickListener {
            showProfileCard()
        }

        view.findViewById<ImageButton>(R.id.musicBtn).setOnClickListener {
            showMusicCard()
        }

        val model: SharedViewModel by activityViewModels()

        view.findViewById<ImageButton>(R.id.settingBtn).setOnClickListener{
            val newItem = NotificationItem("New Notification", "This is a new notification")
            model.data.value?.add(newItem)
            model.adapter.value?.notifyDataSetChanged()
        }


        return view
    }

    private fun updateGreeting() {
        val cal = Calendar.getInstance()
        val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
        val username = "John" // Replace with the actual username

        val greeting: String = when (hourOfDay) {
            in 0..11 -> "Good morning, $username"
            in 12..17 -> "Good afternoon, $username"
            else -> "Good evening, $username"
        }

        textView2.text = greeting
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

    private fun animateCardView(cardView: CardView) {
        val alphaAnimator = ObjectAnimator.ofFloat(cardView, "alpha", 0f, 1f)
        alphaAnimator.duration = 500
        alphaAnimator.start()
    }

    private fun navigateToSettingsPage() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout, SettingsPage())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun navigateToNotifcationPage() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout, NotificationPage())
        transaction.addToBackStack(null)
        transaction.commit()
    }

}
