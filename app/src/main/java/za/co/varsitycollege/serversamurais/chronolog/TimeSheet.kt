package za.co.varsitycollege.serversamurais.chronolog

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.model.Task
import za.co.varsitycollege.serversamurais.chronolog.views.DurationPickerDialogFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TimeSheet.newInstance] factory method to
 * create an instance of this fragment.
 */
class TimeSheet : Fragment(), FirebaseHelper.FirebaseOperationListener,
    DurationPickerDialogFragment.DurationPickerListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var firebaseHelper: FirebaseHelper


    private lateinit var enterTaskDetails: RelativeLayout
    private lateinit var addNewTaskButton: LinearLayout
    private lateinit var navBar: BottomNavigationView
    private lateinit var categoryList: LinearLayout
    private lateinit var chooseTeam: LinearLayout





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
        addNewTaskButton = view.findViewById(R.id.addNewTaskButton)
        enterTaskDetails = view.findViewById(R.id.enterTaskDetails)


        // Category Section
        val categoryButton: Button = view.findViewById(R.id.categoryButton)
        categoryList = view.findViewById(R.id.chooseCategoryList)

        categoryButton.setOnClickListener{
            toggleCategory()
        }

        // Duration Widget
        val showPickerButton: ImageButton = view.findViewById(R.id.editDurationButton)

        showPickerButton.setOnClickListener {
            val picker = DurationPickerDialogFragment()
            picker.listener = this
            picker.show(parentFragmentManager, "durationPicker")
        }


        // Choose Team Widget
        chooseTeam = view.findViewById(R.id.chooseTeamList)
        val teamBtn: Button = view.findViewById(R.id.teamButton)

        teamBtn.setOnClickListener{
            toggleTeamWidget()
        }



        // Make navbar disappear
        navBar = activity.findViewById(R.id.bottomNavigationView)


        addNewTaskButton.setOnClickListener {
            toggleVisibility()
        }


        closeTaskDetailsBtn.setOnClickListener {
            toggleVisibility()
        }


        // add task functionality
        firebaseHelper = FirebaseHelper(this)
        val addTaskButton: Button = view.findViewById(R.id.addTaskButton)

        val userId = firebaseHelper.getUserId()

        addTaskButton.setOnClickListener{


            val newTask = Task(null, "App Research",
                "Research Toggl Track App", null, "Chronolog",
                "OPSC POE", 60)
            firebaseHelper.addTask(newTask, userId )
        }


        // Inflate the layout for this fragment
        return view
    }

    override fun onDurationSet(hours: Int, minutes: Int) {
        // Handle the picked duration
        val currentDurationTV: TextView = view?.findViewById(R.id.currentDurationTV) ?:
        throw IllegalStateException("View cannot be null")
        val duration = (hours * 60) + minutes
        currentDurationTV.text = "$hours:$minutes:00"
        Toast.makeText(context, "Duration: $hours Hours, $minutes Minutes", Toast.LENGTH_LONG).show()
    }

    override fun onCancel() {
        // Handle cancellation
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

    override fun onSuccess(user: FirebaseUser?) {
        Toast.makeText(context, "Task added successfully!", Toast.LENGTH_SHORT).show()
    }

    override fun onFailure(errorMessage: String) {
        Toast.makeText(context, "Failed to add task: $errorMessage", Toast.LENGTH_LONG).show()
    }

}