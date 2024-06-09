package za.co.varsitycollege.serversamurais.chronolog.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.R
import za.co.varsitycollege.serversamurais.chronolog.model.Task
import java.text.SimpleDateFormat
import java.util.*

class HoursWorkedChartFragment : Fragment() {

    private lateinit var lineChart: LineChart
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
        lineChart = view.findViewById(R.id.lineChart)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLineChart()
        loadChartData()
    }

    private fun setupLineChart() {
        lineChart.apply {
            setTouchEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            description = Description().apply { text = "Hours Worked Over Time" }
            description.textSize = 12f
            description.textColor = android.graphics.Color.WHITE

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = android.graphics.Color.WHITE
                textSize = 12f
                valueFormatter = DateAxisValueFormatter(startDate, endDate)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                granularity = 1f
                textColor = android.graphics.Color.WHITE
                textSize = 12f
            }
            axisRight.isEnabled = false

            legend.apply {
                form = Legend.LegendForm.LINE
                textSize = 12f
                textColor = android.graphics.Color.WHITE
            }
        }
    }

    private fun loadChartData() {
        val userId = firebaseHelper.getUserId()
        CoroutineScope(Dispatchers.IO).launch {
            firebaseHelper.fetchTasks(userId) { tasks ->
                val filteredTasks = tasks.filter { it.date?.time in startDate..endDate }
                val entries = filteredTasks.map { Entry(it.date?.time?.toFloat() ?: 0f, it.duration.toFloat()/3600) }
                val dataSet = LineDataSet(entries, "Hours Worked").apply {
                    color = android.graphics.Color.WHITE
                    valueTextColor = android.graphics.Color.WHITE
                    lineWidth = 2f
                    setDrawCircles(true)
                    setCircleColor(android.graphics.Color.WHITE)
                    setDrawFilled(true)
                    fillColor = android.graphics.Color.rgb(26, 69, 96)
                    valueTextSize = 14f
                }
                val lineData = LineData(dataSet)
                CoroutineScope(Dispatchers.Main).launch {
                    lineChart.data = lineData
                    lineChart.invalidate()
                }
            }
        }
    }

    private inner class DateAxisValueFormatter(private val startDate: Long, private val endDate: Long) : ValueFormatter() {
        private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

        override fun getFormattedValue(value: Float): String {
            return dateFormat.format(Date(value.toLong()))
        }
    }
}
