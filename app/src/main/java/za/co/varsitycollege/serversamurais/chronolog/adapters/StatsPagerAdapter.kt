package za.co.varsitycollege.serversamurais.chronolog.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import za.co.varsitycollege.serversamurais.chronolog.views.HoursWorkedChartFragment
import za.co.varsitycollege.serversamurais.chronolog.views.CategoryChartFragment

class StatsPagerAdapter(
    activity: FragmentActivity,
    private val startDate: Long,
    private val endDate: Long
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HoursWorkedChartFragment.newInstance(startDate, endDate)
            else -> CategoryChartFragment.newInstance(startDate, endDate)
        }
    }
}
