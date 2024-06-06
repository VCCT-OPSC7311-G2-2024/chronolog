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

/**
 * A DialogFragment that allows the user to pick a date range.
 */
class DateRangePickerFragment : DialogFragment() {

    private lateinit var startDatePicker: DatePicker
    private lateinit var endDatePicker: DatePicker
    var listener: DateRangePickerListener? = null

    /**
     * Interface for receiving callbacks from the DateRangePickerFragment.
     */
    interface DateRangePickerListener {
        /**
         * Called when the user sets a date range.
         * @param startYear The start year of the range.
         * @param startMonth The start month of the range.
         * @param startDay The start day of the range.
         * @param endYear The end year of the range.
         * @param endMonth The end month of the range.
         * @param endDay The end day of the range.
         */
        fun onDateRangeSelected(startYear: Int, startMonth: Int, startDay: Int, endYear: Int, endMonth: Int, endDay: Int)
    }

    /**
     * Called when a fragment is first attached to its context.
     * @param context The context the fragment is being attached to.
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? DateRangePickerListener
            ?: throw ClassCastException("$context must implement DateRangePickerListener")
    }

    /**
     * Called to do initial creation of the fragment.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
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