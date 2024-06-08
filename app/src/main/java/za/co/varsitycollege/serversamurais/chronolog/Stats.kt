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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.adapters.CategoryAdapter
import za.co.varsitycollege.serversamurais.chronolog.model.Task
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Calendar


private var ARG_PARAM1 = "ARG1"
private var ARG_PARAM2 = "ARG2"
class Stats : Fragment(), FirebaseHelper.FirebaseOperationListener {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private val tasks = mutableListOf<Task>()
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var statsDateRangeTextView: TextView
    private lateinit var lineChart: LineChart

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
        val view = inflater.inflate(R.layout.fragment_stats, container, false)
        val activity = activity as Activity

        statsDateRangeTextView = view.findViewById(R.id.statsDateRangeTextView)
        lineChart = view.findViewById(R.id.lineChart)

        statsDateRangeTextView.setOnClickListener {
            showDateRangePicker()
        }

        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView)
        categoryRecyclerView.layoutManager = LinearLayoutManager(context)

        firebaseHelper = FirebaseHelper(this)
        var startDate: Date? = null
        var endDate: Date? = null

        categoryAdapter = CategoryAdapter(tasks, startDate, endDate)
        categoryRecyclerView.adapter = categoryAdapter

        firebaseHelper.fetchCategoryTasks(firebaseHelper.getUserId(), tasks, categoryAdapter)

        setupLineChart()

        return view
    }

    private fun setupLineChart() {
        lineChart.apply {
            setTouchEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            description = Description().apply { text = "" }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelRotationAngle = 45f
                valueFormatter = XAxisValueFormatter()
            }

            axisLeft.apply {
                setDrawGridLines(true)
                granularity = 1f
            }
            axisRight.isEnabled = false

            legend.apply {
                form = Legend.LegendForm.LINE
                textSize = 12f
                textColor = android.graphics.Color.WHITE
            }
        }
    }

    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select dates")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = Date(selection.first)
            val endDate = Date(selection.second)
            statsDateRangeTextView.text = "${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(startDate)} - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate)}"

            categoryAdapter.filterByDateRange(startDate.toString(), endDate.toString())
            updateChartData(startDate, endDate)
        }

        dateRangePicker.show(childFragmentManager, dateRangePicker.toString())
    }

    private fun updateChartData(startDate: Date, endDate: Date) {
        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
        val entries = tasks.mapNotNull { task ->
            try {
                val taskDate: Date? = task.date?.let { dateFormat.parse(it.toString()) }
                if (taskDate != null && !taskDate.before(startDate) && !taskDate.after(endDate)) {
                    Entry(taskDate.time.toFloat(), task.duration.toFloat() / 3600) // Convert duration to hours
                } else {
                    null
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                null
            }
        }

        val lineDataSet = LineDataSet(entries, "Hours Worked").apply {
            color = android.graphics.Color.WHITE
            valueTextColor = android.graphics.Color.WHITE
            lineWidth = 2f
            setDrawCircles(true)
            setCircleColor(android.graphics.Color.WHITE)
            setDrawFilled(true)
            fillColor = android.graphics.Color.rgb(26, 69, 96)
            valueTextSize = 14f
        }

        lineChart.data = LineData(lineDataSet)

        val xAxis: XAxis = lineChart.xAxis
        xAxis.textColor = android.graphics.Color.WHITE

        val leftAxis: YAxis = lineChart.axisLeft
        leftAxis.textColor = android.graphics.Color.WHITE

        val rightAxis: YAxis = lineChart.axisRight
        rightAxis.textColor = android.graphics.Color.WHITE

        lineChart.invalidate() // Refresh the chart
    }


    companion object {
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
        // Implement as needed
    }

    override fun onFailure(errorMessage: String) {
        // Implement as needed
    }

    private inner class XAxisValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val date = Date(value.toLong())
            val format = SimpleDateFormat("dd/MM", Locale.getDefault())
            return format.format(date)
        }
    }
}