package za.co.varsitycollege.serversamurais.chronolog

import RecyclerAdapter
import SharedViewModel
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.adapters.NotiAdapter
import za.co.varsitycollege.serversamurais.chronolog.model.NotificationItem


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NotificationPage.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationPage : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var dbRef: DatabaseReference
    private lateinit var notificationRecyclerView: RecyclerView
    private lateinit var notiArrayList: ArrayList<NotificationItem>
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }


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
    val view = inflater.inflate(R.layout.fragment_notification_page, container, false)
    notificationRecyclerView = view.findViewById(R.id.recyclerView)
    notificationRecyclerView.layoutManager = LinearLayoutManager(context)
    notificationRecyclerView.setHasFixedSize(true)

    notiArrayList = arrayListOf<NotificationItem>()
    getUserNotification()

    return view
}

    private val database =
        FirebaseDatabase.getInstance("https://chronolog-db9b8-default-rtdb.europe-west1.firebasedatabase.app/")
    private var databaseNotificationRef = database.getReference("notifications")
    fun getUserNotification() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            Log.d("NotificationPage", "User ID: $userId")
            databaseNotificationRef =
                FirebaseDatabase.getInstance().getReference("notifications/$userId")
            databaseNotificationRef.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.d("NotificationPage", "Snapshot exists")
                        notiArrayList.clear()
                        for (userSnapshot in snapshot.children) {
                            val notification = userSnapshot.getValue(NotificationItem::class.java)
                            notiArrayList.add(notification!!)
                        }
                        notificationRecyclerView.adapter = NotiAdapter(notiArrayList)
                    } else {
                        Log.d("NotificationPage", "Snapshot does not exist")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotificationPage", "Database error: ${error.message}")
                }
            })
        } else {
            Log.d("NotificationPage", "User ID is null")
        }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NotificationPage.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NotificationPage().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}