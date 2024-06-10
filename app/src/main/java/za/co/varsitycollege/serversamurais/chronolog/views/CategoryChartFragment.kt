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
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.model.Task
import java.text.SimpleDateFormat
import java.util.*

class CategoryChartFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var firebaseHelper: FirebaseHelper
    private var startDate: Long = 0
    private var endDate: Long = 0

    companion object {
        fun newInstance(startDate: Long, endDate: Long): CategoryChartFragment {
            val fragment = CategoryChartFragment()
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
        loadCategoryData()
    }

    private fun setupBarChart() {
        barChart.apply {
            setTouchEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            description = Description().apply {
                text = "Hours Per Category"
                textSize = 12f
                textColor = android.graphics.Color.WHITE
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = android.graphics.Color.WHITE
                textSize = 12f
                valueFormatter = IndexAxisValueFormatter(listOf()) // Placeholder, will be updated
                labelRotationAngle = -45f // Rotate labels for better visibility
                setAvoidFirstLastClipping(false) // Allow first and last labels to be clipped
                yOffset = 10f // Add offset to avoid clipping
            }

            axisLeft.apply {
                setDrawGridLines(true)
                granularity = 1f
                textColor = android.graphics.Color.WHITE
                textSize = 12f
                axisMinimum = 0f
                labelCount = 6 // Adjust number of labels on the Y-axis
            }
            axisRight.isEnabled = false

            legend.apply {
                form = Legend.LegendForm.SQUARE
                textSize = 12f
                textColor = android.graphics.Color.WHITE
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }

            setExtraOffsets(0f, 0f, 0f, 100f) // Add extra bottom offset to avoid clipping
        }
    }

    private fun loadCategoryData() {
        val userId = firebaseHelper.getUserId()
        CoroutineScope(Dispatchers.IO).launch {
            firebaseHelper.fetchTasks(userId) { tasks ->
                val entries = mutableListOf<BarEntry>()
                val categories = mutableListOf<String>()
                val categoryMap = mutableMapOf<String, Float>()

                tasks.filter { it.date?.time in startDate..endDate }.forEach { task ->
                    val category = task.category ?: "Uncategorized"
                    val hours = task.duration / 3600f
                    categoryMap[category] = categoryMap.getOrDefault(category, 0f) + hours
                }

                categoryMap.entries.forEachIndexed { index, entry ->
                    entries.add(BarEntry(index.toFloat(), entry.value))
                    categories.add(entry.key)
                }

                val dataSet = BarDataSet(entries, "Hours Per Category").apply {
                    color = android.graphics.Color.rgb(8, 60, 101)
                    valueTextColor = android.graphics.Color.WHITE
                    valueTextSize = 14f
                }

                val barData = BarData(dataSet).apply {
                    barWidth = 0.9f // set custom bar width
                }

                CoroutineScope(Dispatchers.Main).launch {
                    barChart.data = barData
                    barChart.xAxis.valueFormatter = IndexAxisValueFormatter(categories)
                    barChart.invalidate()
                }
            }
        }
    }

}
