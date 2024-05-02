package za.co.varsitycollege.serversamurais.chronolog

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.adapters.TaskAdapter
import za.co.varsitycollege.serversamurais.chronolog.model.Category
import za.co.varsitycollege.serversamurais.chronolog.model.Task
import za.co.varsitycollege.serversamurais.chronolog.views.DurationPickerDialogFragment
import java.util.Calendar
import java.util.Date

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

    private lateinit var taskNameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var durationTextView: TextView
    private var taskCategory : String = "Default Category"
    private lateinit var taskRecyclerView: RecyclerView

    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var categoriesAdapter: ArrayAdapter<String>


    private lateinit var createCategoryView: LinearLayout

    private lateinit var timerTextView: TextView
    private var timerRunning = false
    private var timer: CountDownTimer? = null
    private var timeInMilliseconds = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_time_sheet, container, false)
        val activity = activity as Activity

        // Task Details Section
        val closeTaskDetailsBtn: ImageButton = view.findViewById(R.id.closeEnterTaskDetailsBtn)
        addNewTaskButton = view.findViewById(R.id.addNewTaskButton)
        enterTaskDetails = view.findViewById(R.id.enterTaskDetails)

        // Category Section
        val categoryButton: Button = view.findViewById(R.id.categoryButton)
        categoryList = view.findViewById(R.id.chooseCategoryList)

        categoryButton.setOnClickListener {
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

        teamBtn.setOnClickListener {
            toggleTeamWidget()
        }


        // Make navbar disappear
        navBar = activity.findViewById(R.id.bottomNavigationView)

        timerTextView = view.findViewById(R.id.timerTextView)

        addNewTaskButton.setOnClickListener {

            if (!timerRunning) {
                startTimer()
            }
            toggleVisibility()
        }


        closeTaskDetailsBtn.setOnClickListener {
            toggleVisibility()
        }


        // add task functionality
        firebaseHelper = FirebaseHelper(this)
        val addTaskButton: Button = view.findViewById(R.id.addTaskButton)

        val userId = firebaseHelper.getUserId()

        taskRecyclerView = view.findViewById(R.id.recentTasksRecyclerView)
        taskRecyclerView.layoutManager = LinearLayoutManager(context)

        firebaseHelper.fetchTasks(
            onResult = { tasks ->
                taskRecyclerView.adapter = TaskAdapter(tasks)
            },
            onError = { exception ->
                Log.e("FetchTasksError", "Error fetching tasks", exception)
            }
        )




        addTaskButton.setOnClickListener {

            taskNameEditText = view.findViewById(R.id.taskNameEditText)
            descriptionEditText = view.findViewById(R.id.descriptionEditText)
            durationTextView = view.findViewById(R.id.timerTextView)

            val taskName = taskNameEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val durationText = durationTextView.text.toString()
            val splitDuration = durationText.split(":")
            val hours = splitDuration[0].toInt()
            val minutes = splitDuration[1].toInt()
            val duration: Int = (hours * 60) + minutes

            val calendar: Calendar = Calendar.getInstance()
            val today: Date = calendar.time

            val newTask = Task(
                null, taskName,
                description, null, "Chronolog",
                taskCategory, duration, today.toString()
            )
            firebaseHelper.addTask(newTask, userId)

            toggleVisibility()
        }


        // Setup Categories

        var addNewCategoryButton: Button = view.findViewById(R.id.addNewCategory)
        createCategoryView = view.findViewById(R.id.createNewCategory)

        addNewCategoryButton.setOnClickListener {
            toggleAddCategory()
        }

        view.findViewById<Button>(R.id.buttonSaveNewCategory).setOnClickListener {
            val name = view.findViewById<EditText>(R.id.editTextCategoryName).text.toString().trim()
            val isActive = view.findViewById<CheckBox>(R.id.checkBoxIsActive).isChecked

            val newCategory = Category(null, name, isActive)

            if (name.isNotEmpty()) {
                firebaseHelper.addCategoryToFirebase(newCategory, userId)
            } else {
                Toast.makeText(context, "Please enter a category name.", Toast.LENGTH_SHORT).show()
            }
            toggleAddCategory()

        }

        view.findViewById<Button>(R.id.buttonCancelNewCategory).setOnClickListener{
            toggleAddCategory()
        }




        // Retrieve Categories
        autoCompleteTextView = view.findViewById(R.id.autoCompleteTextViewCategory)

        val categories = mutableListOf<String>()
        categoriesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        autoCompleteTextView.setAdapter(categoriesAdapter)


        firebaseHelper.fetchCategories(userId, categories, categoriesAdapter)

        // set autocomplete
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }

        autoCompleteTextView.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) autoCompleteTextView.showDropDown()
        }

        autoCompleteTextView.setOnItemClickListener { adapterView, view, position, id ->
            val selectedCategory = adapterView.getItemAtPosition(position) as String
            // Handle the selected category
        }

        // Done with Categories

        view.findViewById<Button>(R.id.buttonSaveCategory).setOnClickListener {
            val selectedCategory = autoCompleteTextView.text.toString()
            if (selectedCategory.isNotEmpty()) {
                taskCategory = selectedCategory
            } else {
                Toast.makeText(context, "Please select a category.", Toast.LENGTH_SHORT).show()
            }
            toggleCategory()

        }

        view.findViewById<Button>(R.id.buttonCancelCategory).setOnClickListener{
            toggleCategory()
        }

        // Inflate the layout for this fragment
        return view
    }

    override fun onDurationSet(hours: Int, minutes: Int) {
        // Handle the picked duration
        val currentDurationTV: TextView = view?.findViewById(R.id.timerTextView)
            ?: throw IllegalStateException("View cannot be null")
        currentDurationTV.text = "$hours:$minutes:00"
        Toast.makeText(context, "Duration: $hours Hours, $minutes Minutes", Toast.LENGTH_LONG)
            .show()
        val duration = (hours * 60) + minutes
    }


    override fun onCancel() {
        // Handle cancellation
    }

    private fun startTimer() {
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeInMilliseconds += 1000
                updateTimer()
            }

            override fun onFinish() {}
        }.start()
        timerRunning = true
    }

    private fun updateTimer() {
        val hours = timeInMilliseconds / 3600000
        val minutes = (timeInMilliseconds % 3600000) / 60000
        val seconds = (timeInMilliseconds % 60000) / 1000

        val timeString = String.format("%d:%02d:%02d", hours, minutes, seconds)
        timerTextView.text = timeString
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
            hideKeyboard()
        }
    }

    private fun toggleCategory() {
        if (categoryList.visibility == View.GONE) {

            categoryList.visibility = View.VISIBLE


        } else {
            categoryList.visibility = View.GONE

        }
    }

    private fun toggleAddCategory(){
        if (createCategoryView.visibility == View.GONE) {

            createCategoryView.visibility = View.VISIBLE
            categoryList.visibility = View.GONE

        } else {
            createCategoryView.visibility = View.GONE
            categoryList.visibility = View.VISIBLE
        }
    }



    private fun toggleTeamWidget() {
        if (chooseTeam.visibility == View.GONE) {

            chooseTeam.visibility = View.VISIBLE


        } else {
            chooseTeam.visibility = View.GONE

        }
    }

    private fun Fragment.hideKeyboard() {
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = requireActivity().currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
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