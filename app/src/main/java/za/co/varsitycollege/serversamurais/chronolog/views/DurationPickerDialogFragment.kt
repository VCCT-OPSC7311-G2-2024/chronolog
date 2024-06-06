package za.co.varsitycollege.serversamurais.chronolog.views

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import za.co.varsitycollege.serversamurais.chronolog.R

/**
 * A DialogFragment that allows the user to pick a duration in hours and minutes.
 */
class DurationPickerDialogFragment : DialogFragment() {

    /**
     * Interface for receiving callbacks from the DurationPickerDialogFragment.
     */
    interface DurationPickerListener {
        /**
         * Called when the user sets a duration.
         * @param hours The number of hours in the duration.
         * @param minutes The number of minutes in the duration.
         */
        fun onDurationSet(hours: Int, minutes: Int)

        /**
         * Called when the user cancels the dialog.
         */
        fun onCancel()
    }

    /**
     * The listener that will receive callbacks from this DurationPickerDialogFragment.
     */
    var listener: DurationPickerListener? = null

    /**
     * Called to do initial creation of the fragment.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
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