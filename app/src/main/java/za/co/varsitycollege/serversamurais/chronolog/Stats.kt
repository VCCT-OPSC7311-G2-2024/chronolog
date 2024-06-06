package za.co.varsitycollege.serversamurais.chronolog

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.adapters.CategoryAdapter
import za.co.varsitycollege.serversamurais.chronolog.adapters.TaskAdapter
import za.co.varsitycollege.serversamurais.chronolog.model.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Stats.newInstance] factory method to
 * create an instance of this fragment.
 */
class Stats : Fragment(), FirebaseHelper.FirebaseOperationListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private val tasks = mutableListOf<Task>()
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var statsDateRangeTextView: TextView

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
    val view = inflater.inflate(R.layout.fragment_stats, container, false)
    val activity = activity as Activity

    // Initialize the TextView for displaying the selected date range
    statsDateRangeTextView = view.findViewById(R.id.statsDateRangeTextView)

    // Set a click listener on the TextView to show the date range picker when clicked
    statsDateRangeTextView.setOnClickListener{
        showDateRangePicker()
    }

    // Initialize the RecyclerView for displaying the tasks
    categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView)
    categoryRecyclerView.layoutManager = LinearLayoutManager(context)

    // Initialize the Firebase helper for fetching tasks
    firebaseHelper = FirebaseHelper(this)

    // Initialize start and end dates to null
    var startDate: Date? = null
    var endDate: Date? = null

    // Initialize the adapter for the RecyclerView and set it
    categoryAdapter = CategoryAdapter(tasks,startDate, endDate)
    categoryRecyclerView.adapter = categoryAdapter

    // Fetch the user ID and fetch tasks for this user
    firebaseHelper.fetchCategoryTasks(firebaseHelper.getUserId(), tasks, categoryAdapter)

    return view
}

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Stats.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Stats().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onSuccess(user: FirebaseUser?) {
        TODO("Not yet implemented")
    }

    override fun onFailure(errorMessage: String) {
        TODO("Not yet implemented")
    }

    /**
 * This function is used to show a date range picker to the user.
 * It initializes the RecyclerView and its adapter, fetches tasks for the current user,
 * and sets up the date range picker.
 */
private fun showDateRangePicker() {
    // Initialize start and end dates to null
    var startDate: Date? = null
    var endDate: Date? = null

    // Set the layout manager for the RecyclerView
    categoryRecyclerView.layoutManager = LinearLayoutManager(context)

    // Initialize the adapter for the RecyclerView and set it
    categoryAdapter = CategoryAdapter(tasks, startDate, endDate)
    categoryRecyclerView.adapter = categoryAdapter

    // Fetch the user ID and fetch tasks for this user
    val userId = firebaseHelper.getUserId()
    firebaseHelper.fetchCategoryTasks(userId, tasks, categoryAdapter)

    // Build the date range picker
    val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
        .setTitleText("Select dates")
        .build()

    // Set a listener for when the user confirms their date range selection
    dateRangePicker.addOnPositiveButtonClickListener { selection ->
        // Format the selected start and end dates and set them as the text of the TextView
        val startDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selection.first))
        val endDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selection.second))
        statsDateRangeTextView.text = "$startDate - $endDate"

        // Filter the tasks in the adapter by the selected date range
        categoryAdapter.filterByDateRange(startDate, endDate)
    }

    // Show the date range picker
    dateRangePicker.show(childFragmentManager, dateRangePicker.toString())
}
}