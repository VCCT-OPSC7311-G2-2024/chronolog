package za.co.varsitycollege.serversamurais.chronolog

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.RelativeLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TimeSheet.newInstance] factory method to
 * create an instance of this fragment.
 */
class TimeSheet : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var enterTaskDetails: RelativeLayout
    private lateinit var addNewTaskButton: LinearLayout
    private lateinit var navBar: BottomNavigationView
    private  lateinit var categoryList: LinearLayout
    private lateinit var startTime: LinearLayout
    private lateinit var chooseTeam: LinearLayout
    private  lateinit var endTime: LinearLayout

    private lateinit var numberPickerHour: NumberPicker
    private lateinit var numberPickerMinute: NumberPicker
    private lateinit var radioGroupAmPm: RadioGroup

    private lateinit var endTimeNumberPickerHour: NumberPicker
    private lateinit var endTimeNumberPickerMinute: NumberPicker
    private lateinit var endTimeRadioGroupAmPm: RadioGroup


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
        val view = inflater.inflate(R.layout.fragment_time_sheet, container, false)
        val activity = activity as Activity
        // Task Details Section
        val closeTaskDetailsBtn : ImageButton = view.findViewById(R.id.closeEnterTaskDetailsBtn)
        addNewTaskButton = view.findViewById(R.id.addTaskButton)
        enterTaskDetails = view.findViewById(R.id.enterTaskDetails)


        // Category Section
        val categoryButton: Button = view.findViewById(R.id.categoryButton)
        categoryList = view.findViewById(R.id.chooseCategoryList)

        categoryButton.setOnClickListener{
            toggleCategory()
        }


        // start time widget
        startTime = view.findViewById(R.id.chooseStartTime)
        val startTimeBtn: Button = view.findViewById(R.id.startTimeButton)

        startTimeBtn.setOnClickListener{
            toggleStartTime()
        }

        // Clock logic
        numberPickerHour = view.findViewById(R.id.numberPickerHour)
        numberPickerMinute = view.findViewById(R.id.numberPickerMinute)
        radioGroupAmPm = view.findViewById(R.id.radioGroupAmPm)

        numberPickerHour.minValue = 1
        numberPickerHour.maxValue = 12
        numberPickerMinute.minValue = 0
        numberPickerMinute.maxValue = 59
        numberPickerMinute.setFormatter { String.format("%02d", it) }

        radioGroupAmPm.check(R.id.radioButtonAM) // Default to AM

        // end time clock logic
        endTimeNumberPickerHour = view.findViewById(R.id.endTimeNumberPickerHour)
        endTimeNumberPickerMinute = view.findViewById(R.id.endTimeNumberPickerMinute)
        endTimeRadioGroupAmPm = view.findViewById(R.id.endTimeRadioGroupAmPm)

        endTimeNumberPickerHour.minValue = 1
        endTimeNumberPickerHour.maxValue = 12
        endTimeNumberPickerMinute.minValue = 0
        endTimeNumberPickerMinute.maxValue = 59
        endTimeNumberPickerMinute.setFormatter { String.format("%02d", it) }

        endTimeRadioGroupAmPm.check(R.id.endTimeRadioButtonAM) // Default to AM

        // Choose Team Widget
        chooseTeam = view.findViewById(R.id.chooseTeamList)
        val teamBtn: Button = view.findViewById(R.id.teamButton)

        teamBtn.setOnClickListener{
            toggleTeamWidget()
        }


        // end time widget
        endTime = view.findViewById(R.id.chooseEndTime)
        val endTimeBtn: Button = view.findViewById(R.id.endTimeButton)

        endTimeBtn.setOnClickListener{
            toggleEndTimeWidget()
        }

        // Make navbar disappear
        navBar = activity.findViewById(R.id.bottomNavigationView)


        addNewTaskButton.setOnClickListener {
            toggleVisibility()
        }


        closeTaskDetailsBtn.setOnClickListener {
            toggleVisibility()
        }

        // Inflate the layout for this fragment
        return view
    }

    private fun toggleVisibility() {
        if (enterTaskDetails.visibility == View.GONE) {

            enterTaskDetails.visibility = View.VISIBLE
            addNewTaskButton.visibility = View.GONE
            navBar.visibility = View.GONE


        } else {
            enterTaskDetails.visibility = View.GONE
            addNewTaskButton.visibility = View.VISIBLE
            navBar.visibility = View.VISIBLE
        }
    }

    private fun toggleCategory() {
        if (categoryList.visibility == View.GONE) {

            categoryList.visibility = View.VISIBLE


        } else {
            categoryList.visibility = View.GONE

        }
    }

    private fun toggleStartTime() {
        if (startTime.visibility == View.GONE) {

            startTime.visibility = View.VISIBLE


        } else {
            startTime.visibility = View.GONE

        }
    }

    private fun toggleEndTimeWidget() {
        if (endTime.visibility == View.GONE) {

            endTime.visibility = View.VISIBLE


        } else {
            endTime.visibility = View.GONE

        }
    }

    private fun toggleTeamWidget() {
        if (chooseTeam.visibility == View.GONE) {

            chooseTeam.visibility = View.VISIBLE


        } else {
            chooseTeam.visibility = View.GONE

        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TimeSheet.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TimeSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}