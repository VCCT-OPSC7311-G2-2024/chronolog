package za.co.varsitycollege.serversamurais.chronolog.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.model.Task

class sharedTotoalHoursViewModel : ViewModel() {
    private val firebaseHelper: FirebaseHelper = FirebaseHelper(object :
        FirebaseHelper.FirebaseOperationListener {
        override fun onSuccess(user: FirebaseUser?) {
            Log.d("SharedViewModel", "Firebase operation successful")
        }

        override fun onFailure(errorMessage: String) {
            Log.e("SharedViewModel", "Firebase operation failed: $errorMessage")
        }
    })

    private val _tasks: MutableLiveData<List<Task>> = MutableLiveData(emptyList())
    val tasks: LiveData<List<Task>>
        get() = _tasks

    init {
        insertDummyData()
        //loadTasks()
    }

    private fun loadTasks() {
        val userId = firebaseHelper.getUserId()
        Log.e("HomePage", "User ID: $userId")
        firebaseHelper.getTotalHoursPerCategory(userId) { totalHoursPerCategory ->
            val taskList = mutableListOf<Task>()
            totalHoursPerCategory.forEach { (category, totalHours) ->
                val newItem = Task(category, totalHours.toString())
                Log.e("HomePage", "New Item: $newItem")
                taskList.add(newItem)
            }
            _tasks.postValue(taskList)
        }
    }

    fun insertDummyData() {
        val dummyData = listOf(
            Task("Dummy Task 1", "1"),
            Task("Dummy Task 2", "2"),
            Task("Dummy Task 3", "3"),
            // Add more dummy tasks as needed
        )

        _tasks.postValue(dummyData)
    }
}
