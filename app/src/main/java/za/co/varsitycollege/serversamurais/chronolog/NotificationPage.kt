package za.co.varsitycollege.serversamurais.chronolog

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.Helpers.RecyclerAdapter
import za.co.varsitycollege.serversamurais.chronolog.model.Notification

class NotificationPage : Fragment() {

    private lateinit var clearAllBtn: Button
    private lateinit var adapter: RecyclerAdapter
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firebaseHelper = FirebaseHelper(object : FirebaseHelper.FirebaseOperationListener {
            override fun onSuccess(user: FirebaseUser?) {
                // Handle success
            }

            override fun onFailure(errorMessage: String) {
                // Handle failure
                Log.e("NotificationPage", errorMessage)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notification_page, container, false)

        // Initialize RecyclerView and set its layout manager
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the adapter with an empty list
        adapter = RecyclerAdapter(requireContext(), mutableListOf())
        recyclerView.adapter = adapter

        // Initialize UI components
        clearAllBtn = view.findViewById(R.id.clearAllBtn)

        // Set click listener for the clear all button
        clearAllBtn.setOnClickListener {
            clearAllNotifications()
        }

        fetchNotifications()

        return view
    }

    private fun fetchNotifications() {
        firebaseHelper.fetchNotifications(userId) { notifications ->
            adapter.updateData(notifications)
        }
    }

    private fun clearAllNotifications() {
        firebaseHelper.clearAllNotifications(userId) { success ->
            if (success) {
                adapter.clearAllItems()
                Toast.makeText(requireContext(), "All notifications cleared", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to clear notifications", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
