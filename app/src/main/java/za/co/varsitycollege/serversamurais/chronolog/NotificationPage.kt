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

    private lateinit var clearAllBtn: Button

    private lateinit var adapter: RecyclerAdapter

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
            adapter.clearAllItems()
            Toast.makeText(requireContext(), "All notifications cleared", Toast.LENGTH_SHORT).show()
        }

        // Get a reference to the shared ViewModel
        val model: SharedViewModel by activityViewModels()

        // Log the current data and observe changes
        Log.d("NotificationPage", "Current data: ${model.data.value}")
        model.data.observe(viewLifecycleOwner) { itemList ->
            Log.d("NotificationPage", "Data observed: $itemList")
            // Update the adapter with the new data
            adapter.updateData(itemList)
        }

        return view
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