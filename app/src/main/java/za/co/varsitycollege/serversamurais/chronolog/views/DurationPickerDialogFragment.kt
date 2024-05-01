package za.co.varsitycollege.serversamurais.chronolog.views
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import za.co.varsitycollege.serversamurais.chronolog.R

class DurationPickerDialogFragment : DialogFragment() {

    interface DurationPickerListener {
        fun onDurationSet(hours: Int, minutes: Int)
        fun onCancel()
    }

    var listener: DurationPickerListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = android.app.AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val dialogView: View = inflater!!.inflate(R.layout.duration_view, null)

        val numberPickerHours = dialogView.findViewById<NumberPicker>(R.id.numberPickerHours)
        numberPickerHours.minValue = 0
        numberPickerHours.maxValue = 23

        val numberPickerMinutes = dialogView.findViewById<NumberPicker>(R.id.numberPickerMinutes)
        numberPickerMinutes.minValue = 0
        numberPickerMinutes.maxValue = 59

        builder.setView(dialogView)
            .setPositiveButton("OK") { dialog, id ->
                listener?.onDurationSet(numberPickerHours.value, numberPickerMinutes.value)
            }
            .setNegativeButton("CANCEL") { dialog, id ->
                listener?.onCancel()
                dialog.cancel()
            }

        return builder.create()
    }
}
