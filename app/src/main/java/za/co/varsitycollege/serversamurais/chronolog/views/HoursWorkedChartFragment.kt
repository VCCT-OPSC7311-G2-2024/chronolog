package za.co.varsitycollege.serversamurais.chronolog.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.model.Goal
import za.co.varsitycollege.serversamurais.chronolog.model.Task
import java.text.SimpleDateFormat
import java.util.*

class HoursWorkedChartFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var firebaseHelper: FirebaseHelper
    private var startDate: Long = 0
    private var endDate: Long = 0

    companion object {
        fun newInstance(startDate: Long, endDate: Long): HoursWorkedChartFragment {
            val fragment = HoursWorkedChartFragment()
            val args = Bundle()
            args.putLong("startDate", startDate)
            args.putLong("endDate", endDate)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseHelper = FirebaseHelper(object : FirebaseHelper.FirebaseOperationListener {
            override fun onSuccess(user: FirebaseUser?) {}
            override fun onFailure(errorMessage: String) {}
        })
        arguments?.let {
            startDate = it.getLong("startDate")
            endDate = it.getLong("endDate")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chart, container, false)
        barChart = view.findViewById(R.id.barChart)
        barChart.visibility = View.VISIBLE // Ensure bar chart is visible
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBarChart()
        loadChartData()
    }

    private fun setupBarChart() {
        barChart.apply {
            setTouchEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            description = Description().apply {
                text = "Hours Worked"
                textSize = 16f
                textColor = android.graphics.Color.WHITE
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = android.graphics.Color.WHITE
                textSize = 14f
                valueFormatter = IndexAxisValueFormatter(listOf()) // Placeholder, will be updated
                labelRotationAngle = -45f // Rotate labels for better visibility
                setAvoidFirstLastClipping(false) // Allow first and last labels to be clipped
            }

            axisLeft.apply {
                setDrawGridLines(true)
                granularity = 1f
                textColor = android.graphics.Color.WHITE
                textSize = 14f
                axisMinimum = 0f
                labelCount = 6 // Adjust number of labels on the Y-axis
            }
            axisRight.isEnabled = false

            legend.apply {
                form = Legend.LegendForm.CIRCLE
                textSize = 14f
                textColor = android.graphics.Color.WHITE
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
        }
    }

    private fun loadChartData() {
        val userId = firebaseHelper.getUserId()
        CoroutineScope(Dispatchers.IO).launch {
            firebaseHelper.fetchTasks(userId) { tasks ->
                firebaseHelper.fetchGoals(userId, startDate, endDate) { goals ->
                    val minEntries = mutableListOf<BarEntry>()
                    val actualEntries = mutableListOf<BarEntry>()
                    val maxEntries = mutableListOf<BarEntry>()
                    val dates = mutableListOf<String>()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    val groupedTasks = tasks.filter { it.date?.time in startDate..endDate }
                        .groupBy { dateFormat.format(it.date) }

                    groupedTasks.entries.forEachIndexed { index, (date, tasks) ->
                        val totalMinutes = tasks.sumBy { it.duration / 60 }
                        val totalHours = totalMinutes / 60
                        val remainingMinutes = totalMinutes % 60
                        val totalFormattedHours = totalHours + remainingMinutes / 60f

                        val goal = goals.find { it.date == date }
                        val minGoal = goal?.minGoal?.toFloat() ?: 0f
                        val maxGoal = goal?.maxGoal?.toFloat() ?: 0f

                        Log.d("HoursWorkedChartFragment", "Date: $date, Total Hours: $totalFormattedHours, Min Goal: $minGoal, Max Goal: $maxGoal")

                        minEntries.add(BarEntry(index.toFloat(), minGoal))
                        actualEntries.add(BarEntry(index.toFloat(), totalFormattedHours))
                        maxEntries.add(BarEntry(index.toFloat(), maxGoal))

                        dates.add(date)
                    }

                    val minDataSet = BarDataSet(minEntries, "Min Hours").apply {
                        color = android.graphics.Color.WHITE
                        valueTextSize = 14f
                        valueTextColor = android.graphics.Color.WHITE
                    }

                    val actualDataSet = BarDataSet(actualEntries, "Hours Worked").apply {
                        color = android.graphics.Color.rgb(139, 203, 254)
                        valueTextSize = 14f
                        valueTextColor = android.graphics.Color.WHITE
                    }

                    val maxDataSet = BarDataSet(maxEntries, "Max Hours").apply {
                        color = android.graphics.Color.rgb(8, 60, 101)
                        valueTextSize = 14f
                        valueTextColor = android.graphics.Color.WHITE
                    }

                    val barData = BarData(minDataSet, actualDataSet, maxDataSet).apply {
                        barWidth = 0.2f // set custom bar width
                    }

                    val groupSpace = 0.4f
                    val barSpace = 0.03f

                    barData.setValueFormatter(object : ValueFormatter() {
                        override fun getBarLabel(barEntry: BarEntry?): String {
                            val value = barEntry?.y?.toFloat() ?: 0f
                            if(value == 0.toFloat()){
                                return ""
                            }

                            val hours = value.toInt()
                            val minutes = ((value - hours) * 60).toInt()
                            return "${hours}h ${minutes}m"
                        }
                    })

                    CoroutineScope(Dispatchers.Main).launch {
                        barChart.data = barData
                        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
                        barChart.groupBars(0f, groupSpace, barSpace) // group the bars
                        barChart.setFitBars(true) // make the x-axis fit exactly all bars

                        // Set the axis maximum to add space after the last bar
                        barChart.xAxis.axisMinimum = 0f
                        barChart.xAxis.axisMaximum = 0f + barChart.barData.getGroupWidth(groupSpace, barSpace) * dates.size

                        barChart.invalidate()
                    }
                }
            }
        }
    }
}
