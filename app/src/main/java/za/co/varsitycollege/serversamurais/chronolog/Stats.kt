package za.co.varsitycollege.serversamurais.chronolog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.adapters.StatsPagerAdapter
import java.text.SimpleDateFormat
import java.util.*

class Stats : Fragment(), FirebaseHelper.FirebaseOperationListener {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var statsDateRangeTextView: TextView
    private lateinit var firebaseHelper: FirebaseHelper
    private var startDate: Long = 0
    private var endDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseHelper = FirebaseHelper(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        statsDateRangeTextView = view.findViewById(R.id.statsDateRangeTextView)
        viewPager = view.findViewById(R.id.viewPager)
        tabLayout = view.findViewById(R.id.tabLayout)

        statsDateRangeTextView.setOnClickListener {
            showDateRangePicker()
        }

        // Set default date range (e.g., last 7 days)
        val calendar = Calendar.getInstance()
        endDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        startDate = calendar.timeInMillis

        updateCharts()

        return view
    }

    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select dates")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = Date(selection.first)
            val endDate = Date(selection.second)
            statsDateRangeTextView.text = "${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(startDate)} - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate)}"

            this.startDate = startDate.time
            this.endDate = endDate.time

            updateCharts()
        }

        dateRangePicker.show(childFragmentManager, dateRangePicker.toString())
    }

    private fun updateCharts() {
        val adapter = StatsPagerAdapter(requireActivity(), startDate, endDate)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.customView = LayoutInflater.from(requireContext()).inflate(R.layout.tab_item, null)
        }.attach()
    }

    override fun onSuccess(user: FirebaseUser?) {
        // Implement as needed
    }

    override fun onFailure(errorMessage: String) {
        // Implement as needed
    }
}
