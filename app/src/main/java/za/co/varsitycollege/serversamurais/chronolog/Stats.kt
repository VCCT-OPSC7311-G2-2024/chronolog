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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_stats, container, false)
        val activity = activity as Activity

        statsDateRangeTextView = view.findViewById(R.id.statsDateRangeTextView)

        statsDateRangeTextView.setOnClickListener{
            showDateRangePicker()
        }

        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView)
        categoryRecyclerView.layoutManager = LinearLayoutManager(context)
        firebaseHelper = FirebaseHelper(this)
        var startDate: Date? = null
        var endDate: Date? = null
        categoryAdapter = CategoryAdapter(tasks,startDate, endDate)

        categoryRecyclerView.adapter = categoryAdapter


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

    private fun showDateRangePicker() {
        var startDate: Date? = null
        var endDate: Date? = null
        categoryRecyclerView.layoutManager = LinearLayoutManager(context)
        categoryAdapter = CategoryAdapter(tasks, startDate, endDate)
        categoryRecyclerView.adapter = categoryAdapter

        val userId = firebaseHelper.getUserId()
        firebaseHelper.fetchCategoryTasks(userId, tasks, categoryAdapter)

        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select dates")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selection.first))
            val endDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selection.second))
            statsDateRangeTextView.text = "$startDate - $endDate"
            categoryAdapter.filterByDateRange(startDate, endDate)
        }

        dateRangePicker.show(childFragmentManager, dateRangePicker.toString())
    }
}