import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.serversamurais.chronolog.Helpers.FirebaseHelper
import za.co.varsitycollege.serversamurais.chronolog.TimeSheet
import za.co.varsitycollege.serversamurais.chronolog.model.NotificationItem

class SharedViewModel : ViewModel() {
    val data: MutableLiveData<MutableList<NotificationItem>> = MutableLiveData(mutableListOf())
    val adapter = MutableLiveData<RecyclerAdapter>()
    private lateinit var timeSheet: TimeSheet

    private val firebaseHelper: FirebaseHelper = FirebaseHelper(object : FirebaseHelper.FirebaseOperationListener {
        override fun onSuccess(user: FirebaseUser?) {
            Log.d("SharedViewModel", "Firebase operation successful")
        }

        override fun onFailure(errorMessage: String) {
            Log.e("SharedViewModel", "Firebase operation failed: $errorMessage")
        }
    })

    init {
        loadData()
    }

    private fun loadData() {
        // Your data loading logic here
        setupNotificationList()
    }

    fun setupNotificationList() {
        val userId = firebaseHelper.getUserId()
        Log.e("HomePage", "User ID: $userId")
        firebaseHelper.fetchTasks(userId) { tasks ->
            // Clear existing data
            data.value?.clear()
            // Add new data
            tasks.forEach { task ->
                val newItem = NotificationItem(task.name, task.duration.toString())
                Log.e("HomePage", "New Item: $newItem")
                // Use newItem here
                data.value?.add(newItem)
            }
            adapter.value?.notifyDataSetChanged()
        }
    }
}