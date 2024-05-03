import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import za.co.varsitycollege.serversamurais.chronolog.R

class DateRangePickerFragment : DialogFragment() {

    private lateinit var startDatePicker: DatePicker
    private lateinit var endDatePicker: DatePicker
    var listener: DateRangePickerListener? = null

    interface DateRangePickerListener {
        fun onDateRangeSelected(startYear: Int, startMonth: Int, startDay: Int, endYear: Int, endMonth: Int, endDay: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? DateRangePickerListener
            ?: throw ClassCastException("$context must implement DateRangePickerListener")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.date_range_picker, null)

        startDatePicker = view.findViewById(R.id.startDatePicker)
        endDatePicker = view.findViewById(R.id.endDatePicker)
        val btnSet: Button = view.findViewById(R.id.btnSetDateRange)

        btnSet.setOnClickListener {
            val startYear = startDatePicker.year
            val startMonth = startDatePicker.month
            val startDay = startDatePicker.dayOfMonth
            val endYear = endDatePicker.year
            val endMonth = endDatePicker.month
            val endDay = endDatePicker.dayOfMonth

            listener?.onDateRangeSelected(startYear, startMonth, startDay, endYear, endMonth, endDay)
            dismiss()
        }

        builder.setView(view)
        return builder.create()
    }
}
