package za.co.varsitycollege.serversamurais.chronolog.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import za.co.varsitycollege.serversamurais.chronolog.views.CategoryChartFragment
import za.co.varsitycollege.serversamurais.chronolog.views.HoursWorkedChartFragment

class StatsPagerAdapter(fragmentActivity: FragmentActivity, private val startDate: Long, private val endDate: Long) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2 // Number of pages
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HoursWorkedChartFragment.newInstance(startDate, endDate)
            1 -> CategoryChartFragment.newInstance(startDate, endDate)
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
