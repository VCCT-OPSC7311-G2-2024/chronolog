package za.co.varsitycollege.serversamurais.chronolog

import RecyclerAdapter
import SharedViewModel
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
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
import za.co.varsitycollege.serversamurais.chronolog.databinding.FragmentNotificationPageBinding
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

    //private lateinit var dbRef: DatabaseReference
    private lateinit var notificationRecyclerView: RecyclerView
    private lateinit var notiArrayList: ArrayList<NotificationItem>
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var adapter: NotiAdapter
    var dbRef:DatabaseReference? = null
    var eventListener: ValueEventListener? = null
    private lateinit var binding: FragmentNotificationPageBinding


    private val database = FirebaseDatabase.getInstance("https://chronolog-db9b8-default-rtdb.europe-west1.firebasedatabase.app/")
    private var databaseNotificationRef = database.getReference("notifications")

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
    binding = FragmentNotificationPageBinding.inflate(inflater, container, false)

    val gridLayoutManager = GridLayoutManager(context, 1, LinearLayoutManager.VERTICAL, false)
    binding.recyclerView.layoutManager = gridLayoutManager

    notiArrayList = arrayListOf()
    adapter = NotiAdapter(requireContext(), notiArrayList)
    binding.recyclerView.adapter = adapter

    val user = FirebaseAuth.getInstance().currentUser
    user?.let {
        dbRef = database.getReference("notifications/${it.uid}")
    }

    var builder = AlertDialog.Builder(requireContext())
    builder.setCancelable(false)
    builder.setView(R.layout.fragment_notification_page)
    var dialog = builder.create()
    dialog.show()

    eventListener = dbRef!!.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            notiArrayList.clear()
            for (snap in snapshot.children) {
                val noti = snap.getValue(String::class.java)?.let { NotificationItem(it) }
                if (noti != null) {
                    notiArrayList.add(noti)
                }
            }
            adapter.notifyDataSetChanged()
            dialog.dismiss()
        }

        override fun onCancelled(error: DatabaseError) {
            dialog.dismiss()
            Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    })

    return binding.root
}


//    private fun fetchUserNotifications(userID: String, newNotification: MutableList<String>, adapter: ArrayAdapter<String> ) {
//        databaseNotificationRef.child(userID)
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(dataSnapshot: DataSnapshot) {
//                    newNotification.clear()
//                    for (postSnapshot in dataSnapshot.children) {
//                        val date = postSnapshot.child("date").getValue(String::class.java)
//                        val title = postSnapshot.child("title").getValue(String::class.java)
//                        if (date != null && title != null) {
//                            val notification = NotificationItem(title, date)
//                            newNotification.add(notification.toString())
//                        }
//                    }
//                    adapter.notifyDataSetChanged()
//                }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        Log.e("NotificationPage", "Database error: ${error.message}")
//                    }
//                })
//        }

//   private fun updateRecyclerView(notifications: List<NotificationItem>) {
//    notificationRecyclerView.adapter = NotiAdapter(ArrayList(notifications))
//}



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