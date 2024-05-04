package za.co.varsitycollege.serversamurais.chronolog

import RecyclerAdapter
import SharedViewModel
import DateRangePickerFragment
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.adapters.TaskAdapter
import za.co.varsitycollege.serversamurais.chronolog.model.Category
import za.co.varsitycollege.serversamurais.chronolog.model.NotificationItem
import za.co.varsitycollege.serversamurais.chronolog.model.Task
import za.co.varsitycollege.serversamurais.chronolog.views.DurationPickerDialogFragment
import java.util.Calendar
import java.util.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import kotlinx.coroutines.selects.select
import java.text.SimpleDateFormat
import java.util.Locale

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
    private lateinit var taskDate: Date
    private lateinit var taskDateButton: Button


    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var categoriesAdapter: ArrayAdapter<String>


    private lateinit var createCategoryView: LinearLayout

    private val data = mutableListOf<NotificationItem>()
    private lateinit var adapterNotification: RecyclerAdapter


    private lateinit var timerTextView: TextView
    private var timerRunning = false
    private var timer: CountDownTimer? = null
    private var timeInMilliseconds = 0L

    private lateinit var taskRecyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    private lateinit var dateRangeTextView: TextView



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

        dateRangeTextView = view.findViewById(R.id.dateRangeTextView)

        dateRangeTextView.setOnClickListener{
            showDateRangePicker()
        }

        // Task Details Section
        val closeTaskDetailsBtn: ImageButton = view.findViewById(R.id.closeEnterTaskDetailsBtn)
        addNewTaskButton = view.findViewById(R.id.addNewTaskButton)
        enterTaskDetails = view.findViewById(R.id.enterTaskDetails)
        taskDateButton = view.findViewById(R.id.taskDatePicker)

        taskDateButton.setOnClickListener{
            showDatePicker()
        }

        // Initialize adapter
        adapterNotification = RecyclerAdapter(requireContext(), data)

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
        taskAdapter = TaskAdapter(tasks, firebaseHelper)
        taskRecyclerView.adapter = taskAdapter


        firebaseHelper.fetchTasks(userId, tasks, taskAdapter)


        addTaskButton.setOnClickListener {

            if (timerRunning) {
                stopTimer()
            }
            taskNameEditText = view.findViewById(R.id.taskNameEditText)
            descriptionEditText = view.findViewById(R.id.descriptionEditText)
            durationTextView = view.findViewById(R.id.timerTextView)

            val taskId = firebaseHelper.getTaskId(userId)
            val taskName = taskNameEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val durationText = durationTextView.text.toString()
            val splitDuration = durationText.split(":")
            val hours = splitDuration[0].toInt()
            val minutes = splitDuration[1].toInt()
            val duration: Int = (hours * 60) + minutes

            val date = taskDate



            val newTask = Task(
                taskId, taskName,
                description, null, "Chronolog",
                taskCategory, duration, date, false, 1, 1
            )
            firebaseHelper.addTask(newTask, userId)

            setupNotificationList()

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
        if (timerRunning) {
            stopTimer()
        }
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

    private fun showDateRangePicker() {
        taskRecyclerView.layoutManager = LinearLayoutManager(context)
        taskAdapter = TaskAdapter(tasks, firebaseHelper)
        taskRecyclerView.adapter = taskAdapter

        val userId = firebaseHelper.getUserId()
        firebaseHelper.fetchTasks(userId, tasks, taskAdapter)
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select dates")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selection.first))
            val endDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selection.second))
            dateRangeTextView.text = "$startDate - $endDate"
            taskAdapter.filterByDateRange(startDate, endDate)
        }

        dateRangePicker.show(childFragmentManager, dateRangePicker.toString())
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            taskDate = Date(selection)

        }

        datePicker.show(childFragmentManager, datePicker.toString())
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

    private fun stopTimer() {
        timer?.cancel()
        timerRunning = false
    }

    private fun updateTimer() {
        val hours = timeInMilliseconds / 3600000
        val minutes = (timeInMilliseconds % 3600000) / 60000
        val seconds = (timeInMilliseconds % 60000) / 1000

        val timeString = String.format("%d:%02d:%02d", hours, minutes, seconds)
        timerTextView.text = timeString
    }


    fun setupNotificationList() {
        val model: SharedViewModel by activityViewModels()

        val userId = firebaseHelper.getUserId()
        Log.e("HomePage", "User ID: $userId")
        firebaseHelper.fetchTasks(userId) { tasks ->
            // Clear existing data
            model.data.value?.clear()
            // Add new data
            tasks.forEach { task ->
                val newItem = NotificationItem(task.name, task.duration.toString())
                Log.e("HomePage", "New Item: $newItem")
                // Use newItem here
                model.data.value?.add(newItem)
            }
            model.adapter.value?.notifyDataSetChanged()
        }
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