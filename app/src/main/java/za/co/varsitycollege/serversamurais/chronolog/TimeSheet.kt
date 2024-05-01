package za.co.varsitycollege.serversamurais.chronolog

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import za.co.varsitycollege.serversamurais.chronolog.adapters.CategoryAdapter
import za.co.varsitycollege.serversamurais.chronolog.model.Category
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

    private lateinit var taskNameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private var duration: Int = 0

    private lateinit var recyclerView: RecyclerView
    private lateinit var categories: ArrayList<Category>
    private lateinit var adapter: CategoryAdapter

    private lateinit var createCategoryView: LinearLayout


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

        addTaskButton.setOnClickListener {

            taskNameEditText = view.findViewById(R.id.taskNameEditText)
            descriptionEditText = view.findViewById(R.id.descriptionEditText)

            val taskName = taskNameEditText.text.toString()
            val description = descriptionEditText.text.toString()

            val newTask = Task(
                null, taskName,
                description, null, "Chronolog",
                "OPSC POE", duration
            )
            firebaseHelper.addTask(newTask, userId)

            toggleVisibility()
        }

        // Setup Categories

        // Create Category
        var addNewCategoryButton: Button = view.findViewById(R.id.addNewCategory)
        createCategoryView = view.findViewById(R.id.createNewCategory)

        addNewCategoryButton.setOnClickListener {
            toggleAddCategory()
        }

        view.findViewById<Button>(R.id.buttonSaveCategory).setOnClickListener {
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

        view.findViewById<Button>(R.id.buttonCancelCategory).setOnClickListener{
            toggleAddCategory()
        }

        // Retrieve Categories
        recyclerView = view.findViewById(R.id.recyclerViewCategories)
        recyclerView.layoutManager = LinearLayoutManager(context)
        categories = ArrayList()
        adapter = CategoryAdapter(categories)
        recyclerView.adapter = adapter


        firebaseHelper.fetchCategories(userId, categories, adapter)

        // Inflate the layout for this fragment
        return view
    }

    override fun onDurationSet(hours: Int, minutes: Int) {
        // Handle the picked duration
        val currentDurationTV: TextView = view?.findViewById(R.id.currentDurationTV)
            ?: throw IllegalStateException("View cannot be null")
        currentDurationTV.text = "$hours:$minutes:00"
        Toast.makeText(context, "Duration: $hours Hours, $minutes Minutes", Toast.LENGTH_LONG)
            .show()
        duration = (hours * 60) + minutes
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